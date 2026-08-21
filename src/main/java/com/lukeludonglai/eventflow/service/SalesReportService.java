package com.lukeludonglai.eventflow.service;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.BookingStatus;
import com.lukeludonglai.eventflow.repository.BookingRepository;
import com.lukeludonglai.eventflow.repository.EventRepository;

import java.util.List;

// for statistics
public class SalesReportService {
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    public SalesReportService(
            BookingRepository bookingRepository,
            EventRepository eventRepository
    ){
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
    }

    public int getTotalTicketsSold(){
        return bookingRepository
                .findAll()
                .stream()
                .filter(booking-> booking.getStatus()== BookingStatus.CONFIRMED)
                .mapToInt(Booking::getQuantity)
                .sum();
    }
}
