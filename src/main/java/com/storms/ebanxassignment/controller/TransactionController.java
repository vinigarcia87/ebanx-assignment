package com.storms.ebanxassignment.controller;

import com.storms.ebanxassignment.domain.EventRequest;
import com.storms.ebanxassignment.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/event")
    public ResponseEntity<Void> event(@RequestBody EventRequest request) {
        transactionService.event(request.toEvent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/balance")
    public ResponseEntity<?> balance(@RequestParam("account_id") String accountId) {
        // TODO: balance
        return ResponseEntity.ok(0);
    }
}
