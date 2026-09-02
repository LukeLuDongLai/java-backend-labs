package com.lukeludonglai.eventflow.persistence;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.Event;

public interface BookingPersistence {
    void saveCreatedBooking (
            Event event,
            Booking book
    );

    void saveCancelledBooking(
            Event event,
            Booking book
    );
}
