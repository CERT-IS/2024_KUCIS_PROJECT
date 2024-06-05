package org.certis.siem.controller;

import org.certis.siem.entity.AwsS3;
import org.certis.siem.service.AwsS3Service;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import java.io.IOException;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    @PostMapping("/resource")
    public Mono<AwsS3> upload(@RequestPart("file") MultipartFile multipartFile) throws IOException {
        return awsS3Service.upload(multipartFile, "upload");
    }

    @DeleteMapping("/resource")
    public Mono<Void> remove(@RequestBody AwsS3 awsS3) {
        return awsS3Service.remove(awsS3);
    }
}

