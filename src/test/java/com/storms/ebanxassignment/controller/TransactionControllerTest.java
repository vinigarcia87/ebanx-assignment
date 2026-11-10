package com.storms.ebanxassignment.controller;

import com.storms.ebanxassignment.domain.*;
import com.storms.ebanxassignment.exception.AccountNotFoundException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController controller;

    @Test
    void depositReturnsCreatedWithDestinationAccount() {
        var request = new EventRequest("deposit", null, "100", new BigDecimal("10"));
        var destination = new Account("100", new BigDecimal("10"));
        when(transactionService.event(new Deposit("100", new BigDecimal("10"))))
                .thenReturn(EventResult.withDestination(destination));

        ResponseEntity<EventResponse> response = controller.event(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(new EventResponse(null, destination));
    }

    @Test
    void withdrawReturnsCreatedWithOriginAccount() {
        var request = new EventRequest("withdraw", "100", null, new BigDecimal("5"));
        var origin = new Account("100", new BigDecimal("15"));
        when(transactionService.event(new Withdraw("100", new BigDecimal("5"))))
                .thenReturn(EventResult.withOrigin(origin));

        ResponseEntity<EventResponse> response = controller.event(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(new EventResponse(origin, null));
    }

    @Test
    void transferReturnsCreatedWithBothAccounts() {
        var request = new EventRequest("transfer", "100", "300", new BigDecimal("15"));
        var origin = new Account("100", BigDecimal.ZERO);
        var destination = new Account("300", new BigDecimal("15"));
        when(transactionService.event(new Transfer("100", "300", new BigDecimal("15"))))
                .thenReturn(new EventResult(origin, destination));

        ResponseEntity<EventResponse> response = controller.event(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(new EventResponse(origin, destination));
    }

    @Test
    void unknownEventTypeIsRejectedWithoutCallingService() {
        var request = new EventRequest("refund", "100", null, new BigDecimal("10"));

        assertThatThrownBy(() -> controller.event(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("refund");

        verify(transactionService, never()).event(any());
    }

    @Test
    void balanceReturnsOkWithAccountBalance() {
        when(transactionService.balance("100")).thenReturn(new BigDecimal("20"));

        ResponseEntity<BigDecimal> response = controller.balance("100");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new BigDecimal("20"));
    }

    @Test
    void balanceOfNonExistingAccountPropagatesNotFound() {
        when(transactionService.balance("1234")).thenThrow(new AccountNotFoundException("1234"));

        assertThatThrownBy(() -> controller.balance("1234"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void resetCallsServiceAndReturnsOk() {
        ResponseEntity<String> response = controller.reset();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("OK");
        verify(transactionService).reset();
    }
}
