package com.extickets.uploadService.service;

import com.extickets.uploadService.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JavaMailSender mailSender;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ticket = new Ticket();
        ticket.setEventName("Concert Night");
        ticket.setVenue("Stadium");
        ticket.setPrice(1000.0);
        ticket.setEventDateTime(LocalDateTime.now());
        ticket.setUploadedUserName("John");
        ticket.setUploadedEmail("john@example.com");
    }

    @Test
    void testSendEmail_SuccessfulRequests() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(null);

        emailService.sendEmail(ticket, "https://bucket/img.jpg");

        verify(restTemplate, times(2))
                .postForEntity(contains("http://localhost:8082"), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testSendEmail_ExceptionHandled() {
        doThrow(new RuntimeException("Connection failed"))
                .when(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));

        emailService.sendEmail(ticket, "https://bucket/img.jpg");

        verify(restTemplate, atLeastOnce())
                .postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }
}
