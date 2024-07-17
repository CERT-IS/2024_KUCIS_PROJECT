package org.certis.siem.controller;

import org.certis.siem.PdfRequest;
import org.certis.siem.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/pdf")
public class PdfController {

    @Autowired
    private PdfService pdfService;
    private static final String TEMPLATE_DIR = "src/main/resources/templates/pdf/";

    @PostMapping("/template")
    public Mono<ResponseEntity<ByteArrayResource>> previewPdf(@RequestBody PdfRequest request) {
        String templateName = request.getTemplateName();
        String title = request.getTitle();
        String content = request.getContent();

        if (templateName == null || templateName.isEmpty())
            templateName = "template";

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("content", content);

        return pdfService.generatePdf("pdf/" + templateName + ".html", context)
                .flatMap(pdfBytes -> pdfService.convertPdfToImage(pdfBytes))
                .map(imageBytes -> {
                    ByteArrayResource resource = new ByteArrayResource(imageBytes);
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_PNG)
                            .body(resource);
                });
    }

    @GetMapping("/download/{filename}")
    public Mono<ResponseEntity<byte[]>> downloadPdf(@PathVariable("filename") String filename) {
        File file = new File(TEMPLATE_DIR + filename);
        if (file.exists()) {
            return Mono.fromCallable(() -> {
                try {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    return ResponseEntity.ok()
                            .header("Content-Disposition", "attachment; filename=" + file.getName())
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(bytes);
                } catch (IOException e) {
                    throw new RuntimeException("PDF 파일을 다운로드하는 중 오류 발생", e);
                }
            });
        } else {
            return Mono.just(ResponseEntity.notFound().build());
        }
    }


    @PostMapping("/upload")
    public Mono<String> uploadTemplate(@RequestParam("file") MultipartFile file) throws IOException {
        if (!file.isEmpty() && file.getOriginalFilename().endsWith(".html")) {
            byte[] bytes = file.getBytes();
            String uniqueFileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path path = Paths.get(TEMPLATE_DIR + uniqueFileName);
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);

            System.out.println("[register] " + path + " template이 등록 되었습니다.");
        }
        return Mono.just("redirect:/");
    }

    @PostMapping("/delete")
    public Mono<String> deleteTemplate(@RequestParam("filename") String filename) {
        File folder = new File(TEMPLATE_DIR);
        File[] filesToDelete = folder.listFiles((dir, name) -> name.startsWith(filename + "-"));

        if (filesToDelete != null && filesToDelete.length > 0) {
            for (File file : filesToDelete) {
                if (file.delete())
                    System.out.println("[delete] " + file.getPath() + " template이 삭제 되었습니다.");
                else
                    System.out.println("[delete] " + file.getPath() + " template 삭제에 실패했습니다.");
            }
        } else
            System.out.println("[delete] " + filename + " template이 존재하지 않습니다.");

        return Mono.just("redirect:/");
    }

    @GetMapping("/list")
    public Mono<String> listTemplates(Model model) {
        File folder = new File(TEMPLATE_DIR);
        String[] templates = folder.list((dir, name) -> name.endsWith(".html"));
        model.addAttribute("templates", templates);
        return Mono.just("form");
    }
}
