package iscte.ista.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.printing.PDFPageable;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PdfPrinter {

    /**
     * Cria um PDF simples com uma lista de estudantes (um por linha).
     *
     * @param students       lista de Student
     * @param outputFilePath caminho do ficheiro PDF para criar
     * @throws IOException em caso de erro de I/O
     */
    public static void createPdf(List<Student> students, String outputFilePath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            // configurar página
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // configurações de escrita
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float leading = 14f;

                // título
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 16);
                content.newLineAtOffset(margin, yStart);
                content.showText("Lista de Estudantes");
                content.endText();

                // espaço
                yStart -= 30;

                // cabeçalho da tabela
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                content.newLineAtOffset(margin, yStart);
                content.showText(String.format("%-10s %-60s", "Número", "Nome"));
                content.endText();

                yStart -= leading;

                // conteúdo (linhas dos estudantes)
                content.setFont(PDType1Font.HELVETICA, 11);
                for (Student s : students) {
                    if (yStart < margin + 40) {
                        // se faltar espaço, adicionar nova página
                        content.close();
                        PDPage newPage = new PDPage(PDRectangle.A4);
                        document.addPage(newPage);
                        yStart = newPage.getMediaBox().getHeight() - margin;
                        // cria novo content stream para a nova página
                        // (fecha o anterior e reabre abaixo)
                        // mas como já fechámos content, abrimos um novo:
                        try (PDPageContentStream ncontent = new PDPageContentStream(document, newPage)) {
                            ncontent.beginText();
                            ncontent.setFont(PDType1Font.HELVETICA, 11);
                            ncontent.newLineAtOffset(margin, yStart);
                            ncontent.showText(String.format("%-10d %s", s.getNumber(), s.getName()));
                            ncontent.endText();
                        }
                        yStart -= leading;
                        continue;
                    }

                    content.beginText();
                    content.newLineAtOffset(margin, yStart);
                    // alinhamento simples: imprimir número e depois o nome
                    content.showText(String.format("%-10d %s", s.getNumber(), s.getName()));
                    content.endText();
                    yStart -= leading;
                }
            }

            // salvar ficheiro
            document.save(outputFilePath);
            System.out.println("PDF criado em: " + outputFilePath);
        }
    }

    /**
     * Imprime o PDF para a impressora padrão (ou abre diálogo para escolher).
     * Usa PDFPageable do PDFBox para enviar páginas corretamente.
     *
     * @param pdfFilePath caminho do ficheiro PDF a imprimir
     * @throws Exception em caso de erro de impressão ou I/O
     */
    public static void printPdf(String pdfFilePath) throws Exception {
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            PrinterJob job = PrinterJob.getPrinterJob();
            // Define o conteúdo a imprimir usando PDFPageable
            job.setPageable(new PDFPageable(document));

            // abre diálogo para o utilizador escolher impressora (pode ser comentado para impressão silenciosa)
            if (job.printDialog()) {
                job.print();
                System.out.println("Trabalho de impressão enviado.");
            } else {
                System.out.println("Impressão cancelada pelo utilizador.");
            }
        }
    }

    /**
     * Exemplo de execução: cria um PDF com alguns estudantes e pergunta se quer imprimir.
     */
    public static void main(String[] args) {
        List<Student> students = Arrays.asList(
                new Student(21001, "John Doe"),
                new Student(21002, "Paul Smith"),
                new Student(21003, "Ana Costa"),
                new Student(21004, "Maria Silva")
        );

        String outputPath = "data/students.pdf";
        // Garante que a diretoria existe
        new File("data").mkdirs();

        try {
            createPdf(students, outputPath);

            // Se quiseres testar impressão, descomenta a próxima linha.
            // Atenção: em ambientes headless (ex: alguns servidores) a impressão poderá falhar.
            // printPdf(outputPath);

            System.out.println("Concluído - ficheiro PDF gerado.");
        } catch (Exception e) {
            System.err.println("Erro ao gerar/imprimir PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}