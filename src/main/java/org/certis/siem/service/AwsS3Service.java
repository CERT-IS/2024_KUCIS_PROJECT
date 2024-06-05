package org.certis.siem.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.AwsS3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.bucket}")
    private String bucket;

    public Mono<AwsS3> upload(MultipartFile multipartFile, String dirName) {
        return convertMultipartFileToFile(multipartFile)
                .flatMap(file -> upload(file, dirName));
    }

    private Mono<AwsS3> upload(File file, String dirName) {
        String key = randomFileName(file, dirName);
        return putS3(file, key)
                .doOnSuccess(path -> removeFile(file))
                .map(path -> AwsS3.builder().key(key).path(path).build());
    }

    private String randomFileName(File file, String dirName) {
        return dirName + "/" + UUID.randomUUID() + file.getName();
    }

    private Mono<String> putS3(File uploadFile, String fileName) {
        return Mono.fromCallable(() -> {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, uploadFile));
            return getS3(bucket, fileName);
        });
    }

    private String getS3(String bucket, String fileName) {
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    private void removeFile(File file) {
        file.delete();
    }

    public Mono<File> convertMultipartFileToFile(MultipartFile multipartFile) {
        return Mono.fromCallable(() -> {
            File file = new File(System.getProperty("user.dir") + "/" + multipartFile.getOriginalFilename());
            if (file.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(multipartFile.getBytes());
                }
                return file;
            } else {
                throw new IOException("File creation failed");
            }
        });
    }

    public Mono<Void> remove(AwsS3 awsS3) {
        return Mono.fromRunnable(() -> {
            if (!amazonS3.doesObjectExist(bucket, awsS3.getKey())) {
                throw new AmazonS3Exception("Object " + awsS3.getKey() + " does not exist!");
            }
            amazonS3.deleteObject(bucket, awsS3.getKey());
        });
    }
}
