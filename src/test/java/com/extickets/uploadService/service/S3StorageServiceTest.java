package com.extickets.uploadService.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private S3StorageService s3StorageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(s3StorageService, "bucketName", "test-bucket");
    }

    @Test
    void testUploadFile_Success() throws IOException {
        // Mock file behavior
        when(multipartFile.getOriginalFilename()).thenReturn("ticket.pdf");
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getBytes()).thenReturn("dummy content".getBytes());

        // Mock S3 client call to return a valid response (not void)
        PutObjectResponse mockResponse = PutObjectResponse.builder().eTag("12345").build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(mockResponse);

        // Execute
        String result = s3StorageService.uploadFile(multipartFile);

        // Verify results
        assertNotNull(result);
        assertTrue(result.startsWith("https://test-bucket.s3.amazonaws.com/"));
        assertTrue(result.contains("ticket.pdf"));

        // Ensure S3 was called once
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_ThrowsIOException() throws IOException {
        // Mock exception when reading file bytes
        when(multipartFile.getOriginalFilename()).thenReturn("file.txt");
        when(multipartFile.getBytes()).thenThrow(new IOException("Failed to read bytes"));

        IOException exception = assertThrows(IOException.class, () -> {
            s3StorageService.uploadFile(multipartFile);
        });

        assertEquals("Failed to read bytes", exception.getMessage());
//        verify(s3Client, never()).putObject(any(), any());
    }
}
