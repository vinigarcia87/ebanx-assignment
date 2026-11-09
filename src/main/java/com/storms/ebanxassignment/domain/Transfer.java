package com.storms.ebanxassignment.domain;

import java.math.BigDecimal;

public record Transfer(String origin, String destination, BigDecimal amount) implements Event {
}
