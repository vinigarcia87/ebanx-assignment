package com.storms.ebanxassignment.domain;

import java.math.BigDecimal;

public record Deposit(String destination, BigDecimal amount) implements Event {
}
