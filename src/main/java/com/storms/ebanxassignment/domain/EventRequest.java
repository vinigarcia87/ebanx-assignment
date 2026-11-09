package com.storms.ebanxassignment.domain;

import java.math.BigDecimal;

public record EventRequest(String type, String origin, String destination, BigDecimal amount) {

    public Event toEvent() {
        return switch (type) {
            case "deposit" -> new Deposit(destination, amount);
            case "withdraw" -> new Withdraw(origin, amount);
            case "transfer" -> new Transfer(origin, destination, amount);
            default -> throw new IllegalArgumentException("Unknown event type: " + type);
        };
    }
}
