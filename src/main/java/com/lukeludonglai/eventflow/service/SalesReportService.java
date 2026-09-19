package com.lukeludonglai.eventflow.service;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.BookingStatus;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.exception.EventNotFoundException;
import com.lukeludonglai.eventflow.report.EventSalesSummary;
import com.lukeludonglai.eventflow.repository.BookingRepository;
import com.lukeludonglai.eventflow.repository.EventRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
                .filter(booking-> booking.getStatus() == BookingStatus.CONFIRMED)
                .mapToInt(Booking::getQuantity)
                .sum();
    }

    public Map<EventCategory, BigDecimal> getRevenueByCategory(){
        Map<EventCategory, BigDecimal> revenueByCategory = new HashMap<>();
        for (Booking booking : bookingRepository.findAll()){
            if (booking.getStatus() != BookingStatus.CONFIRMED){
                continue;
            }
            Event event = eventRepository.findById(booking.getEventId()).orElseThrow(()-> new EventNotFoundException(booking.getEventId()));
            EventCategory category = event.getCategory();
            BigDecimal revenue = booking.getTotalPrice();
            revenueByCategory.merge(category, revenue, BigDecimal::add);
        }
        return revenueByCategory;
    }

    public List<EventSalesSummary> getTopEventsByTicketsSold(){
        Map<UUID, List<Booking>> bookingsByEvent = bookingRepository.findAll()
                .stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .collect(Collectors.groupingBy(Booking::getEventId));

        return bookingsByEvent.entrySet().stream()
                .map(entry ->{
                    UUID eventId = entry.getKey();
                    List<Booking> bookings = entry.getValue();

                    int ticketsSold = bookings.stream()
                            .reduce(
                                    0,
                                    (total,booking) -> total + booking.getQuantity(),
                                    Integer::sum
                        );

                    BigDecimal revenue = bookings.stream()
                            .map(Booking::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Event event = eventRepository.findById(eventId)
                            .orElseThrow(()-> new EventNotFoundException(eventId));

                    return new EventSalesSummary(
                            eventId,
                            event.getTitle(),
                            ticketsSold,
                            revenue
                    );
                })
                .sorted(
                        Comparator.comparingInt(EventSalesSummary::ticketsSold).reversed()
                )
                .limit(3)
                .toList();
    }
}
