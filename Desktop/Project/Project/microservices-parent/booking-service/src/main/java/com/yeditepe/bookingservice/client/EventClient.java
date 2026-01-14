package com.yeditepe.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/api/v1/events/{id}")
    ResponseEntity<?> getEventById(@PathVariable("id") Long id);

    @PostMapping("/api/v1/events/{id}/book-ticket")
    ResponseEntity<?> bookTicket(@PathVariable("id") Long id);

    @PostMapping("/api/v1/events/{id}/cancel-ticket")
    ResponseEntity<?> cancelTicket(@PathVariable("id") Long id);
}
