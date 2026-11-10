package com.storms.ebanxassignment.service;

import com.storms.ebanxassignment.domain.Account;
import com.storms.ebanxassignment.exception.AccountNotFoundException;
import com.storms.ebanxassignment.domain.Deposit;
import com.storms.ebanxassignment.domain.EventResult;
import com.storms.ebanxassignment.exception.InsufficientFundsException;
import com.storms.ebanxassignment.domain.Transfer;
import com.storms.ebanxassignment.domain.Withdraw;
import com.storms.ebanxassignment.repository.AccountRepository;
import com.storms.ebanxassignment.repository.InMemoryAccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class TransactionServiceTest {

    private final AccountRepository accountRepository = new InMemoryAccountRepository();
    private final TransactionService service = spy(new TransactionService(accountRepository));

    @Test
    void depositEventCallsDepositAndReturnsDestination() {
        var deposit = new Deposit("100", new BigDecimal("10"));

        EventResult result = service.event(deposit);

        verify(service).deposit(deposit);
        assertThat(result).isEqualTo(EventResult.withDestination(new Account("100", new BigDecimal("10"))));
    }

    @Test
    void withdrawEventCallsWithdrawAndReturnsOrigin() {
        accountRepository.save(new Account("100", new BigDecimal("20")));
        var withdraw = new Withdraw("100", new BigDecimal("5"));

        EventResult result = service.event(withdraw);

        verify(service).withdraw(withdraw);
        assertThat(result).isEqualTo(EventResult.withOrigin(new Account("100", new BigDecimal("15"))));
    }

    @Test
    void transferEventCallsTransferAndReturnsItsResult() {
        var transfer = new Transfer("100", "300", new BigDecimal("15"));
        var expected = new EventResult(new Account("100", BigDecimal.ZERO), new Account("300", new BigDecimal("15")));
        doReturn(expected).when(service).transfer(transfer);

        EventResult result = service.event(transfer);

        verify(service).transfer(transfer);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void depositCreatesAccountWhenItDoesNotExist() {
        EventResult result = service.deposit(new Deposit("100", new BigDecimal("10")));

        var expected = new Account("100", new BigDecimal("10"));
        assertThat(result).isEqualTo(EventResult.withDestination(expected));
        assertThat(accountRepository.findById("100")).contains(expected);
    }

    @Test
    void depositAddsToExistingBalance() {
        accountRepository.save(new Account("100", new BigDecimal("10")));

        EventResult result = service.deposit(new Deposit("100", new BigDecimal("10")));

        var expected = new Account("100", new BigDecimal("20"));
        assertThat(result).isEqualTo(EventResult.withDestination(expected));
        assertThat(accountRepository.findById("100")).contains(expected);
    }

    @Test
    void depositRejectsNegativeAmountForNewAccount() {
        assertThatThrownBy(() -> service.deposit(new Deposit("100", new BigDecimal("-10"))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(accountRepository.findById("100")).isEmpty();
    }

    @Test
    void depositRejectsNegativeAmountForExistingAccount() {
        accountRepository.save(new Account("100", new BigDecimal("10")));

        assertThatThrownBy(() -> service.deposit(new Deposit("100", new BigDecimal("-10"))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(accountRepository.findById("100")).contains(new Account("100", new BigDecimal("10")));
    }

    @Test
    void withdrawSubtractsFromExistingBalance() {
        accountRepository.save(new Account("100", new BigDecimal("20")));

        EventResult result = service.withdraw(new Withdraw("100", new BigDecimal("5")));

        var expected = new Account("100", new BigDecimal("15"));
        assertThat(result).isEqualTo(EventResult.withOrigin(expected));
        assertThat(accountRepository.findById("100")).contains(expected);
    }

    @Test
    void withdrawFromNonExistingAccountThrowsAndCreatesNothing() {
        assertThatThrownBy(() -> service.withdraw(new Withdraw("200", new BigDecimal("10"))))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("200");

        assertThat(accountRepository.findById("200")).isEmpty();
    }

    @Test
    void withdrawWithInsufficientFundsKeepsBalance() {
        accountRepository.save(new Account("100", new BigDecimal("10")));

        assertThatThrownBy(() -> service.withdraw(new Withdraw("100", new BigDecimal("15"))))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(accountRepository.findById("100")).contains(new Account("100", new BigDecimal("10")));
    }

    @Test
    void withdrawRejectsNegativeAmountAndKeepsBalance() {
        accountRepository.save(new Account("100", new BigDecimal("10")));

        assertThatThrownBy(() -> service.withdraw(new Withdraw("100", new BigDecimal("-5"))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(accountRepository.findById("100")).contains(new Account("100", new BigDecimal("10")));
    }
}
