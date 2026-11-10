package com.storms.ebanxassignment.domain;

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
}
