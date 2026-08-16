package com.lukeludonglai.eventflow.exception;

import java.util.UUID;

public class EventNotBookableException extends RuntimeException {
    public EventNotBookableException(UUID eventId, String reason) {
        super("Event is not bookable: " + eventId + ". Reason: " + reason);
    }
}
