package com.extickets.uploadService.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.extickets.uploadService.model.Ticket;
import com.extickets.uploadService.model.TicketWithStatus;
import com.extickets.uploadService.repository.UploadTicketRepository;

@Service
public class UploadTicketService {

	@Autowired
	private UploadTicketRepository ticketRepository;

	public void saveTicket(Ticket ticket) {
		ticketRepository.save(ticket);
	}

	public List<Ticket> getAllTickets() {
		return ticketRepository.findAll();
	}

	public List<TicketWithStatus> getAllTicketsBasedOnStatus(String status) {
		return ticketRepository.findByStatus(status);
	}

	public Ticket getTicketById(Long id) {
		return ticketRepository.findById(id);
	}
	public List<Ticket> getTicketByUserEmail(String userEmail) {
		return ticketRepository.findByUserEmail(userEmail);
	}
}