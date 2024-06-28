package org.certis.siem.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.AwsS3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final AmazonS3 amazonS3;
    private final String eventKey ="siem-events:%s-%s";

    @Value("${cloud.aws.bucket.cloudtrail}")
    private String cloudTrailBucket;

    @Value("${cloud.aws.bucket.waf}")
    private String WAFBucket;

    @Value("${cloud.aws.bucket.flowlog}")
    private String flowLogBucket;


    @Value("${cloud.aws.bucket.certis}")
    private String certisBucket;


    public Mono<AwsS3> upload(String eventLog, String eventName) {
        String key = String.format(eventKey, eventName, Instant.now());
        return putS3(eventLog, key)
                .map(path -> AwsS3.builder().key(key).path(path).build());
    }

    private Mono<String> putS3(String uploadEventLog, String fileName) {
        return Mono.fromCallable(() -> {
            byte[] bytes = uploadEventLog.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);

            amazonS3.putObject(new PutObjectRequest(certisBucket, fileName, byteArrayInputStream, metadata));
            return getS3(certisBucket, fileName);
        });
    }

    private String getS3(String bucket, String fileName) {
        return amazonS3.getUrl(bucket, fileName).toString();
    }


    // cloudtrail bucket의 로그 데이터를 가져오는 함수
    public Mono<String> downloadFile(String key) {
        return Mono.fromCallable(() -> {
            S3Object s3object = amazonS3.getObject(cloudTrailBucket, key);
            S3ObjectInputStream inputStream = s3object.getObjectContent();
            File file = new File(System.getProperty("user.dir") + "/" + key);
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return file.getAbsolutePath();
        });
    }

    public Flux<String> downloadAllFiles() {
        return Flux.create(emitter -> {
            ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
                    .withBucketName(cloudTrailBucket);
            ListObjectsV2Result listObjectsV2Result;

            do {
                listObjectsV2Result = amazonS3.listObjectsV2(listObjectsV2Request);
                for (com.amazonaws.services.s3.model.S3ObjectSummary objectSummary : listObjectsV2Result.getObjectSummaries()) {
                    String key = objectSummary.getKey();
                    Mono<String> downloadedFilePath = downloadFile(key);
                    downloadedFilePath.subscribe(
                            filePath -> emitter.next(filePath),
                            emitter::error,
                            emitter::complete
                    );
                }
                listObjectsV2Request.setContinuationToken(listObjectsV2Result.getNextContinuationToken());
            } while (listObjectsV2Result.isTruncated());
        });
    }
}
