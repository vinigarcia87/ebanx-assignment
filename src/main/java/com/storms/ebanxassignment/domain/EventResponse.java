package com.storms.ebanxassignment.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventResponse(Account origin, Account destination) {

    public static EventResponse from(EventResult result) {
        return new EventResponse(result.origin(), result.destination());
    }
}
