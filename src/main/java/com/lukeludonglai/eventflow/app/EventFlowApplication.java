package com.lukeludonglai.eventflow.app;

import com.lukeludonglai.eventflow.cli.EventFlowCli;
import com.lukeludonglai.eventflow.database.JdbcConnectionFactory;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.persistence.BookingPersistence;
import com.lukeludonglai.eventflow.persistence.jdbc.JdbcBookingPersistence;
import com.lukeludonglai.eventflow.pricing.PricingPolicy;
import com.lukeludonglai.eventflow.pricing.StandardPricingPolicy;
import com.lukeludonglai.eventflow.repository.BookingRepository;
import com.lukeludonglai.eventflow.repository.EventRepository;
import com.lukeludonglai.eventflow.repository.inmemory.InMemoryBookingRepository;
import com.lukeludonglai.eventflow.repository.inmemory.InMemoryEventRepository;
import com.lukeludonglai.eventflow.repository.jdbc.JdbcBookingRepository;
import com.lukeludonglai.eventflow.repository.jdbc.JdbcEventRepository;
import com.lukeludonglai.eventflow.service.BookingService;
import com.lukeludonglai.eventflow.service.EventSearchService;
import com.lukeludonglai.eventflow.service.SalesReportService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Scanner;

public class EventFlowApplication {

    public static void main(String[] args) throws Exception{
        JdbcConnectionFactory connectionFactory =
                new JdbcConnectionFactory(
                        System.getenv("DB_URL"),
                        System.getenv("DB_USER"),
                        System.getenv("DB_PASSWORD")
                );

        EventRepository eventRepository = new JdbcEventRepository(connectionFactory);
        BookingRepository bookingRepository = new JdbcBookingRepository(connectionFactory);
        BookingPersistence bookingPersistence = new JdbcBookingPersistence(connectionFactory);
        PricingPolicy pricingPolicy = new StandardPricingPolicy();
        Clock clock = Clock.systemUTC();

        BookingService bookingService =
                new BookingService(
                        eventRepository,
                        bookingRepository,
                        pricingPolicy,
                        clock,
                        bookingPersistence
                );

        EventSearchService eventSearchService =
                new EventSearchService(
                        eventRepository
                );

        SalesReportService salesReportService =
                new SalesReportService(
                        bookingRepository,
                        eventRepository
                );

        loadDemoData(eventRepository, clock);

        Scanner scanner = new Scanner(System.in);

        EventFlowCli cli = new EventFlowCli(
                eventSearchService,
                bookingService,
                salesReportService,
                scanner
                );

        cli.run();
    }

    private static void loadDemoData(
            EventRepository eventRepository,
            Clock clock
    ){
        ZoneId madrid = ZoneId.of("Europe/Madrid");
        ZonedDateTime now = Instant.now(clock).atZone(madrid);

        Event javaMeetup = new Event(
                "Barcelona Java Meetup",
                EventCategory.TECH,
                now.plusDays(10)
                        .withHour(18)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0),
                new BigDecimal("20.00"),
                50
        );

        Event musicNight = new Event(
                "Barcelona Music Night",
                EventCategory.MUSIC,
                now.plusDays(20)
                        .withHour(21)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0),
                new BigDecimal("45.00"),
                100
        );

        Event springConference = new Event(
                "Spring Backend Conference",
                EventCategory.TECH,
                now.plusDays(30)
                        .withHour(9)
                        .withMinute(30)
                        .withSecond(0)
                        .withNano(0),
                new BigDecimal("35.00"),
                80
        );

        javaMeetup.publish();
        musicNight.publish();
        springConference.publish();

        eventRepository.save(javaMeetup);
        eventRepository.save(musicNight);
        eventRepository.save(springConference);
    }
}
