package com.yeditepe.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service")
public interface TicketClient {

    @GetMapping("/api/v1/tickets/{id}")
    ResponseEntity<?> getTicketById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/tickets/number/{ticketNumber}")
    ResponseEntity<?> getTicketByNumber(@PathVariable("ticketNumber") String ticketNumber);
}
