package org.certis.siem.controller;

import lombok.extern.slf4j.Slf4j;
import org.certis.siem.entity.dto.ReportRequest;
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
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/pdf")
public class PdfController {

    private static final String TEMPLATE_DIR = "classpath:templates/pdf";

    @Autowired
    private PdfService pdfService;


    @PostMapping("/report")
    public Mono<ResponseEntity<byte[]>> createReport(@RequestBody ReportRequest reportRequest) {
        String templateName = reportRequest.getTemplate();

        if (templateName == null || templateName.isEmpty())
            templateName = "template";

        Context context = new Context();
        setContextVariables(context, reportRequest);

        return pdfService.generatePdf("pdf/" + templateName + ".html", context)
                .map(bytes -> ResponseEntity.ok()
                            .header("Content-Disposition", "attachment; filename=\"" + "file.pdf\"")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(bytes));
    }

    @PostMapping("/upload")
    public Mono<String> uploadTemplate(@RequestParam("file") MultipartFile file) throws IOException {
        if (!file.isEmpty() && file.getOriginalFilename().endsWith(".html")) {
            byte[] bytes = file.getBytes();
            String uniqueFileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path path = Paths.get(TEMPLATE_DIR + uniqueFileName);
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);

            log.info("[register] " + path + " template이 등록 되었습니다.");
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
                    log.info("[delete] " + file.getPath() + " template이 삭제 되었습니다.");
                else
                    log.info("[delete] " + file.getPath() + " template 삭제에 실패했습니다.");
            }
        } else
            log.info("[delete] " + filename + " template이 존재하지 않습니다.");

        return Mono.just("redirect:/");
    }

    @GetMapping("/list")
    public Mono<Map<String, Object>> listTemplates() {
        File folder = new File(TEMPLATE_DIR);
        String[] templates = folder.list((dir, name) -> name.endsWith(".html"));
        Map<String, Object> response = new HashMap<>();
        response.put("templates", templates != null ? Arrays.asList(templates) : new ArrayList<>());
        return Mono.just(response);
    }

    private void setContextVariables(Context context, ReportRequest reportRequest) {
        context.setVariable("name", reportRequest.getName());
        context.setVariable("description", reportRequest.getDescription());
        context.setVariable("reportSource", reportRequest.getReportSource());
        context.setVariable("notebook", reportRequest.getNotebook());
        context.setVariable("fileFormat", reportRequest.getFileFormat());
        context.setVariable("reportTrigger", reportRequest.getReportTrigger());
        context.setVariable("requestTime", reportRequest.getRequestTime());
        context.setVariable("frequency", reportRequest.getFrequency());
        context.setVariable("every", reportRequest.getEvery());
        context.setVariable("timeUnit", reportRequest.getTimeUnit());
        context.setVariable("startTime", reportRequest.getStartTime());
        context.setVariable("template", reportRequest.getTemplate());
    }
}
