package com.storms.ebanxassignment.domain;

import com.storms.ebanxassignment.exception.InsufficientFundsException;

import java.math.BigDecimal;

public record Account(String id, BigDecimal balance) {

    public static Account empty(String id) {
        return new Account(id, BigDecimal.ZERO);
    }

    public Account deposit(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Deposit amount must not be negative");
        }

        return new Account(id, balance.add(amount));
    }

    public Account withdraw(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Withdraw amount must not be negative");
        }
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(id);
        }

        return new Account(id, balance.subtract(amount));
    }
}
