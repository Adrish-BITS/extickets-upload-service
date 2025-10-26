package com.extickets.uploadService.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.extickets.uploadService.model.GoogleUserPrincipal;
import com.extickets.uploadService.model.Ticket;
import com.extickets.uploadService.service.EmailService;
import com.extickets.uploadService.service.S3StorageService;
import com.extickets.uploadService.service.UploadTicketService;



@RestController
@RequestMapping("/api/tickets")
public class UploadTicketsController {

	@Autowired
	private UploadTicketService ticketService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private S3StorageService s3StorageService;
	
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> uploadTicket(@RequestParam("eventName") String eventName,
			@RequestParam("eventDateTime") String eventDateTime, @RequestParam("venue") String venue,
			@RequestParam("price") Double price, @RequestParam("file") MultipartFile file,
			@RequestParam("eventImage") MultipartFile eventImage) throws IOException {

		try {
		String fileUploadUrl = s3StorageService.uploadFile(file);
	    String eventImageUploadUrl = s3StorageService.uploadFile(eventImage);
	    
	    GoogleUserPrincipal principal = (GoogleUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    String userEmail = principal.getEmail();
	    String userName = principal.getName();
	    System.out.println("User Email: " + userEmail);
	    System.out.println("User Name: " + userName);
	    
	    
		Ticket ticket = new Ticket();
		ticket.setEventName(eventName);
		ticket.setEventDateTime(LocalDateTime.parse(eventDateTime));
		ticket.setVenue(venue);
		ticket.setPrice(price);
		ticket.setFilePath(fileUploadUrl);
		ticket.setEventImagePath(eventImageUploadUrl);
		ticket.setUploadedEmail(userEmail);
		ticket.setUploadedUserName(userName);
		ticketService.saveTicket(ticket);
		emailService.sendEmail(ticket, eventImageUploadUrl);
		
		return ResponseEntity.ok("Ticket uploaded successfully!");
		}catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
	    }
	}

	@GetMapping
	public ResponseEntity<List<Ticket>> getAllTickets() {
		return ResponseEntity.ok(ticketService.getAllTickets());
	}

	@GetMapping("id/{id}")
	public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
		return ResponseEntity.ok(ticketService.getTicketById(id));
	}
	
	@GetMapping("user/{userEmail}")
	public ResponseEntity<List<Ticket>> getTicketByUserEmail(@PathVariable String userEmail) {
		return ResponseEntity.ok(ticketService.getTicketByUserEmail(userEmail));
	}

}
