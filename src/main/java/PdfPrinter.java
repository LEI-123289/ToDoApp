import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;

/**
 * Classe PdfPrinter
 * Gera um ficheiro PDF simples com texto e imagem (opcional).
 */
public class PdfPrinter {

    public static void main(String[] args) {
        String outputPath = "saida.pdf"; // nome do ficheiro PDF
        String imagePath = "logo.png";   // opcional: caminho de uma imagem existente

        try {
            gerarPDF(outputPath, imagePath);
            System.out.println("✅ PDF criado com sucesso: " + outputPath);
        } catch (IOException e) {
            System.err.println("Erro ao criar o PDF: " + e.getMessage());
        }
    }

    public static void gerarPDF(String outputPath, String imagePath) throws IOException {
        // Cria um novo documento PDF
        try (PDDocument document = new PDDocument()) {
            // Adiciona uma página
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {

                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 18);
                content.newLineAtOffset(70, 780);
                content.showText("Exemplo de PDF criado com Java (PDFBox)");
                content.endText();

                // Texto normal
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(70, 750);
                content.showText("Este documento foi gerado automaticamente pela classe PdfPrinter.");
                content.endText();

                // Adiciona uma imagem, se existir
                try {
                    PDImageXObject image = PDImageXObject.createFromFile(imagePath, document);
                    float largura = 150;
                    float altura = image.getHeight() * (largura / image.getWidth());
                    content.drawImage(image, 70, 600, largura, altura);
                } catch (IOException e) {
                    System.out.println("⚠️ Imagem não encontrada: " + imagePath + " (a ignorar)");
                }

                // Rodapé
                content.beginText();
                content.setFont(PDType1Font.COURIER_OBLIQUE, 10);
                content.newLineAtOffset(70, 100);
                content.showText("Gerado automaticamente por PdfPrinter.java");
                content.endText();
            }

            // Guarda o ficheiro PDF
            document.save(outputPath);
        }
    }
}
