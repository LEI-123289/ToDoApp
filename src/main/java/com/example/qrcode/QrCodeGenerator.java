package com.example.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QrCodeGenerator {

    public static void gerarQRCode(String texto, String caminhoFicheiro) {
        int largura = 300;
        int altura = 300;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, largura, altura);
            Path caminho = FileSystems.getDefault().getPath(caminhoFicheiro);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", caminho);
            System.out.println("QR Code gerado com sucesso: " + caminhoFicheiro);
        } catch (WriterException | IOException e) {
            System.err.println("Erro ao gerar QR Code: " + e.getMessage());
        }
    }
}
