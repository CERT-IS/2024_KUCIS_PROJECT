package org.certis.siem;

import org.certis.siem.entity.Metadata;
import org.certis.siem.repository.MetaRepository;
import org.certis.siem.service.MetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

    @InjectMocks
    private MetadataService metadataService;

    @Mock
    private MetaRepository metaRepository;

    private final String logGroup = "test-log-group";
    private final LocalDateTime timestamp = LocalDateTime.now();
    private Metadata metadata;

    @BeforeEach
    void init() {
        metadata = new Metadata();
        metadata.setLogGroup(logGroup);
        metadata.setTimestamp(timestamp);
    }

    @Test
    void metadata가_존재하는_경우_Timestamp를_업데이트하는_메서드() {
        when(metaRepository.findByLogGroup(logGroup)).thenReturn(Mono.just(metadata));
        when(metaRepository.save(metadata)).thenReturn(Mono.just(metadata));

        StepVerifier.create(metadataService.updateTimestamp(logGroup, timestamp))
                .expectNextMatches(updatedMetadata -> updatedMetadata.getLogGroup().equals(logGroup) && updatedMetadata.getTimestamp().equals(timestamp))
                .verifyComplete();

        verify(metaRepository).findByLogGroup(logGroup);
        verify(metaRepository).save(metadata);
    }

    @Test
    void metadata가_존재하지_않는_경우_Timestamp를_업데이트하는_메서드() {
        when(metaRepository.findByLogGroup(logGroup)).thenReturn(Mono.empty());
        when(metaRepository.save(any(Metadata.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(metadataService.updateTimestamp(logGroup, timestamp))
                .expectNextMatches(newMetadata -> newMetadata.getLogGroup().equals(logGroup) && newMetadata.getTimestamp().equals(timestamp))
                .verifyComplete();

        verify(metaRepository).findByLogGroup(logGroup);
        verify(metaRepository).save(any(Metadata.class));
    }

    @Test
    void metadata가_존재하는_경우_해당_로그그룹의_마지막_Timestamp를_업데이트하는_메서드() {
        when(metaRepository.findByLogGroup(logGroup)).thenReturn(Mono.just(metadata));

        StepVerifier.create(metadataService.getLastTimestampForLogGroup(logGroup))
                .expectNextMatches(foundMetadata -> foundMetadata.getLogGroup().equals(logGroup) && foundMetadata.getTimestamp().equals(timestamp))
                .verifyComplete();

        verify(metaRepository).findByLogGroup(logGroup);
    }

    @Test
    void metadata가_존재하지_않는_경우_해당_로그그룹의_마지막_Timestamp를_업데이트하는_메서드() {
        when(metaRepository.findByLogGroup(logGroup)).thenReturn(Mono.empty());

        StepVerifier.create(metadataService.getLastTimestampForLogGroup(logGroup))
                .verifyComplete();

        verify(metaRepository).findByLogGroup(logGroup);
    }
}
