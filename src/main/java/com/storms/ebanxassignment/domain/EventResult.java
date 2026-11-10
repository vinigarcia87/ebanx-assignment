package com.storms.ebanxassignment.domain;

public record EventResult(Account origin, Account destination) {

    public static EventResult withOrigin(Account origin) {
        return new EventResult(origin, null);
    }

    public static EventResult withDestination(Account destination) {
        return new EventResult(null, destination);
    }
}
