package org.certis.siem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfService {
    private final String FONT_STREAM_READY_MESSAGE = "Font stream is available";
    private final String INVALID_FONT_STREAM_ERROR_MESSAGE = "Font stream is not available.";
    private final String PDF_CONVERT_ERROR_MESSAGE = "PDF 생성중 오류가 발생했습니다.";

    private final String MaruBuriFontDirectory = "static/fonts/MaruBuri-Regular.ttf";
    private final String MaruBuriFont = "MaruBuri-Regular";


    private final TemplateEngine templateEngine;


    public Mono<byte[]> generatePdf(String templateName, Context context) {
        return Mono.fromCallable(() -> {
            String html = templateEngine.process(templateName, context);
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

                ClassPathResource fontResource = new ClassPathResource(MaruBuriFontDirectory);

                try (InputStream fontStream = fontResource.getInputStream()) {
                    PdfRendererBuilder builder = new PdfRendererBuilder();
                    builder.withHtmlContent(html, "/");

                    builder.useFont(() -> fontStream, MaruBuriFont);
                    builder.toStream(os);
                    builder.run();

                    String message = (fontStream.available() > 0) ? FONT_STREAM_READY_MESSAGE : INVALID_FONT_STREAM_ERROR_MESSAGE;
                    log.info(message);
                }

                return os.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException(PDF_CONVERT_ERROR_MESSAGE, e);
            }
        });
    }


}
