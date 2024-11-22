package org.certis.siem.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final TemplateEngine templateEngine;

    public Mono<byte[]> generatePdf(String templateName, Context context) {
        return Mono.fromCallable(() -> {
            String html = templateEngine.process(templateName, context);
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

                ClassPathResource fontResource = new ClassPathResource("static/fonts/MaruBuri-Regular.ttf");

                System.out.println("Font resource path: "+fontResource.getPath());
                System.out.println("Font exists: "+fontResource.exists());

                try (InputStream fontStream = fontResource.getInputStream()) {
                    PdfRendererBuilder builder = new PdfRendererBuilder();
                    builder.withHtmlContent(html, "/");

                    builder.useFont(() -> fontStream, "MaruBuri-Regular");
                    builder.toStream(os);
                    builder.run();

                    if (fontStream.available() > 0) {
                        System.out.println("Font stream is available and ready to read.");
                    }
                    System.out.println("empty or not available.");
                }

                return os.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("PDF 생성 중 오류 발생", e);
            }
        });
    }

    public Mono<byte[]> convertPdfToImage(byte[] pdfBytes) {
        return Mono.fromCallable(() -> {
            try (PDDocument document = PDDocument.load(pdfBytes)) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                return baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("PDF를 이미지로 변환하는 중 오류 발생", e);
            }
        });
    }


}
