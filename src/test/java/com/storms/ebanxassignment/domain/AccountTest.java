package com.storms.ebanxassignment.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Test
    void depositAddsAmountToBalance() {
        var account = new Account("100", new BigDecimal("10"));

        Account updated = account.deposit(new BigDecimal("15"));

        assertThat(updated).isEqualTo(new Account("100", new BigDecimal("25")));
    }

    @Test
    void depositOnEmptyAccountSetsBalanceToAmount() {
        Account updated = Account.empty("100").deposit(new BigDecimal("10"));

        assertThat(updated).isEqualTo(new Account("100", new BigDecimal("10")));
    }

    @Test
    void depositOfZeroKeepsBalance() {
        var account = new Account("100", new BigDecimal("10"));

        assertThat(account.deposit(BigDecimal.ZERO)).isEqualTo(account);
    }

    @Test
    void depositDoesNotChangeOriginalAccount() {
        var account = new Account("100", new BigDecimal("10"));

        account.deposit(new BigDecimal("5"));

        assertThat(account.balance()).isEqualTo(new BigDecimal("10"));
    }

    @Test
    void depositRejectsNegativeAmount() {
        var account = new Account("100", new BigDecimal("10"));

        assertThatThrownBy(() -> account.deposit(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deposit amount must not be negative");
    }

    @Test
    void depositRejectsNullAmount() {
        var account = new Account("100", new BigDecimal("10"));

        assertThatThrownBy(() -> account.deposit(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
