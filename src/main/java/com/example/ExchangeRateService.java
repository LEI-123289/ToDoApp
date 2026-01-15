// Substitua o conteúdo de ExchangeRateService.java por isto:

package com.example; // (Mantenha o seu nome de pacote, ex: com.example)

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;

import java.io.IOException;
import java.util.Map; // <--- IMPORTANTE: MUDÁMOS PARA UM MAP

public class ExchangeRateService {

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    // --- Constante para a NOVA API (gratuita e sem chave) ---
    private static final String API_URL = "https://api.frankfurter.app/latest";

    // --- Classes para "apanhar" a nova resposta JSON ---
    // A estrutura da resposta desta API é diferente
    private static class FrankfurterResponse {
        double amount;
        String base; // A moeda "from"
        String date;
        // O JSON "rates" é um mapa de Moeda->Valor (ex: "USD": 1.08)
        Map<String, Double> rates;
    }

    public double convert(String from, String to, double amount) {

        // 1. Construir o URL para a API frankfurter.app
        HttpUrl url = HttpUrl.parse(API_URL).newBuilder()
                .addQueryParameter("from", from)
                .addQueryParameter("to", to)
                .addQueryParameter("amount", String.valueOf(amount))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Erro HTTP na API de câmbio: " + response.message());
                return -1.0;
            }

            String jsonBody = response.body().string();
            FrankfurterResponse apiResponse = gson.fromJson(jsonBody, FrankfurterResponse.class);

            // --- Verificação de Erro para a nova API ---
            if (apiResponse == null || apiResponse.rates == null || !apiResponse.rates.containsKey(to)) {
                System.err.println("Erro lógico da API ou moeda de destino não encontrada. JSON: " + jsonBody);
                return -1.0;
            }

            // 4. Obter o valor convertido do Mapa "rates"
            // A API já nos dá o valor final convertido
            double convertedValue = apiResponse.rates.get(to);

            // Opcional: Calcular a taxa (rate)
            double rate = convertedValue / amount;
            System.out.println("Taxa de câmbio (" + from + "->" + to + "): " + rate);

            return convertedValue;

        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado ao processar a resposta da API:");
            e.printStackTrace();
            return -1.0;
        }
    }
}