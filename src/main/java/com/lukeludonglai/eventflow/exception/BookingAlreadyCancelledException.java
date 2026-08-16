package com.lukeludonglai.eventflow.exception;

import java.util.UUID;

public class BookingAlreadyCancelledException extends RuntimeException {
    public BookingAlreadyCancelledException(UUID bookingId){
        super("Booking is already cancelled: " + bookingId);
    }
}
