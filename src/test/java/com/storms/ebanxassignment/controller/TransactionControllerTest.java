package com.storms.ebanxassignment.controller;

import com.storms.ebanxassignment.domain.Deposit;
import com.storms.ebanxassignment.domain.EventRequest;
import com.storms.ebanxassignment.domain.Transfer;
import com.storms.ebanxassignment.domain.Withdraw;
import com.storms.ebanxassignment.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController controller;

    @Test
    void depositEventIsSentToServiceAndReturnsCreated() {
        var request = new EventRequest("deposit", null, "100", new BigDecimal("10"));

        ResponseEntity<Void> response = controller.event(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(transactionService).event(new Deposit("100", new BigDecimal("10")));
    }

    @Test
    void withdrawEventIsSentToServiceAndReturnsCreated() {
        var request = new EventRequest("withdraw", "100", null, new BigDecimal("5"));

        ResponseEntity<Void> response = controller.event(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(transactionService).event(new Withdraw("100", new BigDecimal("5")));
    }

    @Test
    void transferEventIsSentToServiceAndReturnsCreated() {
        var request = new EventRequest("transfer", "100", "300", new BigDecimal("15"));

        ResponseEntity<Void> response = controller.event(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(transactionService).event(new Transfer("100", "300", new BigDecimal("15")));
    }

    @Test
    void unknownEventTypeIsRejectedWithoutCallingService() {
        var request = new EventRequest("refund", "100", null, new BigDecimal("10"));

        assertThatThrownBy(() -> controller.event(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("refund");

        verify(transactionService, never()).event(any());
    }
}
