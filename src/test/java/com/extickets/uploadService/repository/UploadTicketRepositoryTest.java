package com.extickets.uploadService.repository;

import com.extickets.uploadService.model.Ticket;
import com.extickets.uploadService.model.TicketWithStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UploadTicketRepositoryTest {

    @InjectMocks
    private UploadTicketRepository uploadTicketRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ticket = new Ticket();
        ticket.setEventName("Concert");
        ticket.setEventDateTime(LocalDateTime.of(2025, 10, 25, 18, 0));
        ticket.setVenue("Arena");
        ticket.setPrice(1200.0);
        ticket.setFilePath("/files/ticket.pdf");
        ticket.setEventImagePath("/files/event.jpg");
        ticket.setUploadedUserName("John");
        ticket.setUploadedEmail("john@example.com");
    }

    @Test
    void testSaveTicket_Success() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.queryForObject(eq("SELECT LAST_INSERT_ID()"), eq(Long.class))).thenReturn(10L);

        int result = uploadTicketRepository.save(ticket);

        verify(jdbcTemplate, times(1)).update(startsWith("INSERT INTO tickets"), any(Object[].class));
        verify(jdbcTemplate, times(1)).update(startsWith("INSERT INTO transactions"), eq(10L), eq("in-review"));
        assertEquals(0, result);
    }

    @Test
    void testFindAll_ReturnsList() {
        List<Ticket> mockTickets = List.of(ticket);
        when(jdbcTemplate.query(eq("SELECT * FROM tickets"), any(BeanPropertyRowMapper.class))).thenReturn(mockTickets);

        List<Ticket> result = uploadTicketRepository.findAll();

        assertEquals(1, result.size());
        assertEquals("Concert", result.get(0).getEventName());
    }

    @Test
    void testFindByStatus_ReturnsList() {
        TicketWithStatus t = new TicketWithStatus();
        when(jdbcTemplate.query(contains("FROM tickets t"), any(BeanPropertyRowMapper.class), eq("in-review")))
                .thenReturn(List.of(t));

        List<TicketWithStatus> result = uploadTicketRepository.findByStatus("in-review");

        assertEquals(1, result.size());
        assertEquals("in-review", result.get(0).getStatus());
    }

    @Test
    void testFindById_ReturnsTicket() {
        when(jdbcTemplate.queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(1L))).thenReturn(ticket);

        Ticket result = uploadTicketRepository.findById(1L);

        assertEquals("Concert", result.getEventName());
        verify(jdbcTemplate).queryForObject(anyString(), any(BeanPropertyRowMapper.class), eq(1L));
    }

    @Test
    void testFindByUserEmail_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), eq("john@example.com")))
                .thenReturn(List.of(ticket));

        List<Ticket> result = uploadTicketRepository.findByUserEmail("john@example.com");

        assertEquals(1, result.size());
        assertEquals("john@example.com", result.get(0).getUploadedEmail());
    }
}
