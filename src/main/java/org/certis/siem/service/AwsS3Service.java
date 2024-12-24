package org.certis.siem.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AwsS3Service {

    private final AmazonS3 amazonS3;
    private final String eventKey ="events:%s:%s-%s";

    @Value("${client.name:default}")
    private String name;

    @Value("${cloud.aws.bucket.cloudtrail}")
    private String cloudTrailBucket;


    @Value("${cloud.aws.bucket.certis}")
    private String certisBucket;


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

    public Mono<String> downloadFile(String key) {  // cloudtrail bucket
        return Mono.fromCallable(() -> {
            S3Object s3object = amazonS3.getObject(cloudTrailBucket, key);
            S3ObjectInputStream objectInputStream = s3object.getObjectContent();

            boolean isGzipped = s3object.getObjectMetadata().getContentEncoding() != null
                    && s3object.getObjectMetadata().getContentEncoding().contains("gzip");

            String content;
            if (isGzipped) {
                content = readGzippedInputStreamToString(objectInputStream);
            } else {
                content = readInputStreamToString(objectInputStream);
            }

            objectInputStream.close();
            return content;
        });
    }

    private String readInputStreamToString(S3ObjectInputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read input stream", e);
        }
        return stringBuilder.toString();
    }

    private String readGzippedInputStreamToString(S3ObjectInputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read gzipped input stream", e);
        }
        return stringBuilder.toString();
    }

    public Flux<String> downloadAllFiles(String prefix) {
        return Flux.create(emitter -> {
            ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
                    .withBucketName(cloudTrailBucket)
                    .withPrefix(prefix);

            ListObjectsV2Result listObjectsV2Result;
            do {
                listObjectsV2Result = amazonS3.listObjectsV2(listObjectsV2Request);

                for (S3ObjectSummary objectSummary : listObjectsV2Result.getObjectSummaries()) {
                    String key = objectSummary.getKey();
                    Mono<String> downloadedContent = downloadFile(key);
                    downloadedContent.subscribe(
                            emitter::next,
                            emitter::error,
                            emitter::complete
                    );
                }

                listObjectsV2Request.setContinuationToken(listObjectsV2Result.getNextContinuationToken());
            } while (listObjectsV2Result.isTruncated());
        });
    }

    public String getLatestEventStreamIdFromS3() {
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
                .withBucketName(certisBucket)
                .withMaxKeys(1)
                .withPrefix("events:"+name);

        ListObjectsV2Result listObjectsV2Result = amazonS3.listObjectsV2(listObjectsV2Request);
        return listObjectsV2Result.getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .map(this::extractEventStreamId)
                .max(Comparator.naturalOrder())
                .orElse("0");
    }

    private String extractEventStreamId(String key) {
        int startIndex = key.indexOf(":") + 1;
        int endIndex = key.lastIndexOf("-");
        return key.substring(startIndex, endIndex);  // events:client-name:streamId-2024-07-01
    }

}
