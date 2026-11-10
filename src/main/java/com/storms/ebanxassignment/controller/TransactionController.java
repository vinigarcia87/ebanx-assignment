package com.storms.ebanxassignment.controller;

import com.storms.ebanxassignment.domain.EventRequest;
import com.storms.ebanxassignment.domain.EventResponse;
import com.storms.ebanxassignment.domain.EventResult;
import com.storms.ebanxassignment.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/event")
    public ResponseEntity<EventResponse> event(@RequestBody EventRequest request) {
        EventResult result = transactionService.event(request.toEvent());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(result));
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> balance(@RequestParam("account_id") String accountId) {
        return ResponseEntity.ok(transactionService.balance(accountId));
    }
}
