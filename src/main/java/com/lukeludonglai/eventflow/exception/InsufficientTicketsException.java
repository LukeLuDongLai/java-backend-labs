package com.lukeludonglai.eventflow.exception;

public class InsufficientTicketsException extends RuntimeException {
    public InsufficientTicketsException(int requested, int available){
        super(
                "Insufficient tickets: requested "
                        + requested
                        + ", but only "
                        + available
                        + " available"
        );
    }
}
