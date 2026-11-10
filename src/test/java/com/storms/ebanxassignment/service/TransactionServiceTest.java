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
        accountRepository.save(new Account("100", new BigDecimal("15")));
        var transfer = new Transfer("100", "300", new BigDecimal("15"));

        EventResult result = service.event(transfer);

        verify(service).transfer(transfer);
        assertThat(result).isEqualTo(new EventResult(
                new Account("100", new BigDecimal("0")),
                new Account("300", new BigDecimal("15"))));
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

    @Test
    void transferMovesAmountBetweenExistingAccounts() {
        accountRepository.save(new Account("100", new BigDecimal("20")));
        accountRepository.save(new Account("300", new BigDecimal("5")));

        EventResult result = service.transfer(new Transfer("100", "300", new BigDecimal("15")));

        var expectedOrigin = new Account("100", new BigDecimal("5"));
        var expectedDestination = new Account("300", new BigDecimal("20"));
        assertThat(result).isEqualTo(new EventResult(expectedOrigin, expectedDestination));
        assertThat(accountRepository.findById("100")).contains(expectedOrigin);
        assertThat(accountRepository.findById("300")).contains(expectedDestination);
    }

    @Test
    void transferCreatesDestinationWhenItDoesNotExist() {
        accountRepository.save(new Account("100", new BigDecimal("15")));

        EventResult result = service.transfer(new Transfer("100", "300", new BigDecimal("15")));

        assertThat(result.destination()).isEqualTo(new Account("300", new BigDecimal("15")));
        assertThat(accountRepository.findById("300")).contains(new Account("300", new BigDecimal("15")));
    }

    @Test
    void transferFromNonExistingAccountThrowsAndCreatesNothing() {
        assertThatThrownBy(() -> service.transfer(new Transfer("200", "300", new BigDecimal("15"))))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("200");

        assertThat(accountRepository.findById("200")).isEmpty();
        assertThat(accountRepository.findById("300")).isEmpty();
    }

    @Test
    void transferWithInsufficientFundsChangesNothing() {
        accountRepository.save(new Account("100", new BigDecimal("10")));
        accountRepository.save(new Account("300", new BigDecimal("5")));

        assertThatThrownBy(() -> service.transfer(new Transfer("100", "300", new BigDecimal("15"))))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(accountRepository.findById("100")).contains(new Account("100", new BigDecimal("10")));
        assertThat(accountRepository.findById("300")).contains(new Account("300", new BigDecimal("5")));
    }

    @Test
    void transferRejectsNegativeAmountAndChangesNothing() {
        accountRepository.save(new Account("100", new BigDecimal("10")));

        assertThatThrownBy(() -> service.transfer(new Transfer("100", "300", new BigDecimal("-5"))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(accountRepository.findById("100")).contains(new Account("100", new BigDecimal("10")));
        assertThat(accountRepository.findById("300")).isEmpty();
    }

    @Test
    void transferToSameAccountIsRejected() {
        accountRepository.save(new Account("100", new BigDecimal("10")));

        assertThatThrownBy(() -> service.transfer(new Transfer("100", "100", new BigDecimal("5"))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(accountRepository.findById("100")).contains(new Account("100", new BigDecimal("10")));
    }

    @Test
    void balanceReturnsCurrentBalanceOfExistingAccount() {
        accountRepository.save(new Account("100", new BigDecimal("20")));

        assertThat(service.balance("100")).isEqualTo(new BigDecimal("20"));
    }

    @Test
    void balanceOfNonExistingAccountThrows() {
        assertThatThrownBy(() -> service.balance("1234"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("1234");
    }

    @Test
    void resetRemovesAllAccounts() {
        accountRepository.save(new Account("100", new BigDecimal("10")));
        accountRepository.save(new Account("300", new BigDecimal("15")));

        service.reset();

        assertThat(accountRepository.findById("100")).isEmpty();
        assertThat(accountRepository.findById("300")).isEmpty();
    }
}
