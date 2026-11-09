package com.storms.ebanxassignment.domain;

import java.math.BigDecimal;

public record Withdraw(String origin, BigDecimal amount) implements Event {
}
