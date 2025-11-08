package com.extickets.uploadService.controller;

import com.extickets.uploadService.model.GoogleUserPrincipal;
import com.extickets.uploadService.model.Ticket;
import com.extickets.uploadService.service.EmailService;
import com.extickets.uploadService.service.S3StorageService;
import com.extickets.uploadService.service.UploadTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UploadTicketsControllerTest {

    @InjectMocks
    private UploadTicketsController uploadTicketsController;

    @Mock
    private UploadTicketService ticketService;

    @Mock
    private EmailService emailService;

    @Mock
    private S3StorageService s3StorageService;

    private GoogleUserPrincipal principal;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        principal = new GoogleUserPrincipal("test@example.com", "Test User");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testUploadTicket_Success() throws Exception {
        // Mocking files
        MockMultipartFile file = new MockMultipartFile("file", "ticket.pdf", "application/pdf", "dummy content".getBytes());
        MockMultipartFile eventImage = new MockMultipartFile("eventImage", "poster.jpg", "image/jpeg", "image content".getBytes());

        // Mock S3 URLs
        when(s3StorageService.uploadFile(file)).thenReturn("https://s3.aws.com/ticket.pdf");
        when(s3StorageService.uploadFile(eventImage)).thenReturn("https://s3.aws.com/poster.jpg");

        // Act
        ResponseEntity<String> response = uploadTicketsController.uploadTicket(
                "Music Fest",
                LocalDateTime.now().toString(),
                "Stadium",
                1500.0,
                file,
                eventImage
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Ticket uploaded successfully"));
        verify(ticketService, times(1)).saveTicket(any(Ticket.class));
        verify(emailService, times(1)).sendEmail(any(Ticket.class), anyString());
    }

    @Test
    void testUploadTicket_Failure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "ticket.pdf", "application/pdf", "dummy".getBytes());
        MockMultipartFile eventImage = new MockMultipartFile("eventImage", "poster.jpg", "image/jpeg", "image".getBytes());

        when(s3StorageService.uploadFile(file)).thenThrow(new RuntimeException("S3 Upload Failed"));

        ResponseEntity<String> response = uploadTicketsController.uploadTicket(
                "Music Fest", LocalDateTime.now().toString(),
                "Stadium", 1500.0, file, eventImage
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Upload failed"));
    }

    @Test
    void testGetAllTickets() {
        Ticket ticket = new Ticket();
        ticket.setEventName("Concert");
        when(ticketService.getAllTickets()).thenReturn(List.of(ticket));

        ResponseEntity<List<Ticket>> response = uploadTicketsController.getAllTickets();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetTicketById() {
        Ticket ticket = new Ticket();
        ticket.setEventName("Rock Show");
        when(ticketService.getTicketById(1L)).thenReturn(ticket);

        ResponseEntity<Ticket> response = uploadTicketsController.getTicketById(1L);

        assertEquals("Rock Show", response.getBody().getEventName());
    }

    @Test
    void testGetTicketByUserEmail() {
        Ticket ticket = new Ticket();
        ticket.setUploadedEmail("test@example.com");
        when(ticketService.getTicketByUserEmail("test@example.com")).thenReturn(List.of(ticket));

        ResponseEntity<List<Ticket>> response = uploadTicketsController.getTicketByUserEmail("test@example.com");

        assertEquals(1, response.getBody().size());
        assertEquals("test@example.com", response.getBody().get(0).getUploadedEmail());
    }
}
