package com.extickets.uploadService.service;

import com.extickets.uploadService.model.Ticket;
import com.extickets.uploadService.model.TicketWithStatus;
import com.extickets.uploadService.repository.UploadTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UploadTicketServiceTest {

    @Mock
    private UploadTicketRepository ticketRepository;

    @InjectMocks
    private UploadTicketService uploadTicketService;

    private Ticket ticket;
    private TicketWithStatus ticketWithStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ticket = new Ticket();
        ticket.setId("1");
        ticket.setEventName("Music Concert");
        ticket.setVenue("Arena");
        ticket.setPrice(1500.0);
        ticket.setEventDateTime(LocalDateTime.now());
        ticket.setUploadedEmail("user@example.com");

        ticketWithStatus = new TicketWithStatus();
        ticketWithStatus.setEventName("Comedy Show");
    }

    @Test
    void testSaveTicket() {
//        doNothing().when(ticketRepository).save(ticket);

        uploadTicketService.saveTicket(ticket);

        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    void testGetAllTickets() {
        when(ticketRepository.findAll()).thenReturn(List.of(ticket));

        List<Ticket> result = uploadTicketService.getAllTickets();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Music Concert", result.get(0).getEventName());
        verify(ticketRepository, times(1)).findAll();
    }

    @Test
    void testGetAllTicketsBasedOnStatus() {
        when(ticketRepository.findByStatus("in-review")).thenReturn(List.of(ticketWithStatus));

        List<TicketWithStatus> result = uploadTicketService.getAllTicketsBasedOnStatus("in-review");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Comedy Show", result.get(0).getEventName());
        verify(ticketRepository, times(1)).findByStatus("in-review");
    }

    @Test
    void testGetTicketById() {
        when(ticketRepository.findById(1L)).thenReturn(ticket);

        Ticket result = uploadTicketService.getTicketById(1L);

        assertNotNull(result);
        assertEquals("Music Concert", result.getEventName());
        verify(ticketRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTicketByUserEmail() {
        when(ticketRepository.findByUserEmail("user@example.com")).thenReturn(List.of(ticket));

        List<Ticket> result = uploadTicketService.getTicketByUserEmail("user@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user@example.com", result.get(0).getUploadedEmail());
        verify(ticketRepository, times(1)).findByUserEmail("user@example.com");
    }
}
