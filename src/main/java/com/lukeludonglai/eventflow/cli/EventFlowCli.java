package com.lukeludonglai.eventflow.cli;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.exception.BookingNotFoundException;
import com.lukeludonglai.eventflow.exception.EventNotBookableException;
import com.lukeludonglai.eventflow.exception.EventNotFoundException;
import com.lukeludonglai.eventflow.exception.InsufficientTicketsException;
import com.lukeludonglai.eventflow.exception.BookingAlreadyCancelledException;
import com.lukeludonglai.eventflow.report.EventSalesSummary;
import com.lukeludonglai.eventflow.search.EventSearchCriteria;
import com.lukeludonglai.eventflow.search.EventSort;
import com.lukeludonglai.eventflow.service.BookingService;
import com.lukeludonglai.eventflow.service.EventSearchService;
import com.lukeludonglai.eventflow.service.SalesReportService;

import java.math.BigDecimal;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class EventFlowCli {
    private final EventSearchService eventSearchService;
    private final BookingService bookingService;
    private final SalesReportService salesReportService;
    private final Scanner scanner;

    public EventFlowCli(
            EventSearchService eventSearchService,
            BookingService bookingService,
            SalesReportService salesReportService,
            Scanner scanner
    ){
        this.eventSearchService = eventSearchService;
        this.bookingService = bookingService;
        this.salesReportService = salesReportService;
        this.scanner = scanner;
    }

    public void run(){
        boolean running = true;
        while (running){
            printMenu();

            String choice = scanner.nextLine();

            switch (choice){
                case "1" -> listEvents();
                case "2" -> searchEvents();
                case "3" -> createBooking();
                case "4" -> cancelBooking();
                case "5" -> showSalesReport();
                case "0" -> running = false;
            }
        }
    }

    public void printMenu(){
        System.out.println("========================");
        System.out.println("        EVENTFLOW       ");
        System.out.println("========================");
        System.out.println();
        System.out.println("1. List published events");
        System.out.println("2. Search events");
        System.out.println("3. Book tickets");
        System.out.println("4. Cancel booking");
        System.out.println("5. View sales report");
        System.out.println("0. Exit");
    }

    private void listEvents(){
        System.out.println("========================");
        System.out.println("    AVAILABLE EVENTS    ");
        System.out.println("========================");
        System.out.println();

        EventSearchCriteria criteria = new EventSearchCriteria(null, null, null, null, null, null);
        eventSearchService.search(criteria).forEach(event -> printEvent(event));
    }

    private void searchEvents(){
        System.out.println("Keyword (leave blank for any):");
        String keyword = scanner.nextLine().strip();
        if (keyword.isBlank()){
            keyword = null;
        }

        EventCategory category = readCategory();

        ZonedDateTime from = readOptionalFromDate();

        ZonedDateTime to = readOptionalToDate();

        BigDecimal maxPrice = readOptionalPrice();

        EventSort sort = readSort();

        System.out.println(
                """
                    ========================
                         SEARCH RESULTS    
                    ========================
                """
        );

        try{
            EventSearchCriteria criteria = new EventSearchCriteria(keyword, category, from, to, maxPrice, sort);
            List<Event> results = eventSearchService.search(criteria);

            if (results.isEmpty()){
                System.out.println("No events found.");
                return;
            }

            results.forEach(this::printEvent);
        } catch (IllegalArgumentException e){
            System.out.println("Invalid search criteria: " + e.getMessage());
        }
    }

    private void createBooking(){
        UUID eventId;
        String email;
        int quantity;

        // 1. read event ID
        while (true){
            System.out.println("Enter Event ID:");
            String eventIdInput = scanner.nextLine().strip();

            try {
                eventId = UUID.fromString(eventIdInput);
                break;
            } catch(IllegalArgumentException e){
                System.out.println("Invalid Event ID format. Try again.");
            }
        }


        // 2. read email
        System.out.println("Customer email:");
        email = scanner.nextLine().strip();

        // 3. read quantity
        System.out.println("Quantity:");
        while (true){
            String quantityInput = scanner.nextLine().strip();
            try{
                quantity = Integer.parseInt(quantityInput);
                break;
            }catch(NumberFormatException e){
                System.out.println("Invalid quantity. Please enter a whole number:");
            }
        }

        // 4. call bookingService.createBooking(...)
        try{
            Booking booking = bookingService.createBooking(
                    eventId,
                    email,
                    quantity
            );

            System.out.println("""
                    ========================
                         BOOKING PLACED    
                    ========================
                    """);
            printBooking(booking);
        } catch (EventNotFoundException
                 | EventNotBookableException
                 | InsufficientTicketsException
                 | IllegalArgumentException e){
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    private void cancelBooking(){
        UUID bookingId;
        while (true){
            System.out.println("Enter Booking ID:");
            String input = scanner.nextLine().strip();
            try{
                bookingId = UUID.fromString(input);
                break;
            } catch(IllegalArgumentException e){
                System.out.println("Invalid Booking ID format. Try again.");
            }
        }
            try{
                Booking booking = bookingService.cancelBooking(bookingId);
                System.out.println("""
                    ========================
                        BOOKING CANCELLED    
                    ========================
                    """);
                printBooking(booking);
            } catch (BookingNotFoundException
                     | BookingAlreadyCancelledException
                     | EventNotFoundException e){
                System.out.println("Cancellation failed: " + e.getMessage());
            }

    }

    private void showSalesReport(){
        System.out.println("""
                    ========================
                          SALES REPORT    
                    ========================
                    """);
        // total tickets sold
        int totalTicketsSold = salesReportService.getTotalTicketsSold();
        System.out.println("Total tickets sold: " + totalTicketsSold);
        System.out.println();

        // revenue by category
        System.out.println("Revenue by category:");

        Map<EventCategory, BigDecimal> revenueByCategory = salesReportService.getRevenueByCategory();

        if (revenueByCategory.isEmpty()){
            System.out.println("No revenue data yet.");
        } else {
            for (EventCategory category : EventCategory.values()) {
                BigDecimal revenue = revenueByCategory.get(category);

                if (revenue != null) {
                    System.out.println(category + ": €" + revenue);
                }
            }
        }
        System.out.println();

        // top events
        System.out.println("Top events:");

        List<EventSalesSummary> topEvents = salesReportService.getTopEventsByTicketsSold();

        if (topEvents.isEmpty()) {
            System.out.println("No sales data yet.");
        } else{
            for (int i =0; i< topEvents.size(); i++){
                EventSalesSummary summary = topEvents.get(i);
                printEventSalesSummary(summary, i + 1);
            }
        }
    }

    private void printEvent(Event event){
        System.out.println("ID: " + event.getId());
        System.out.println("Title: " + event.getTitle());
        System.out.println("Category: " + event.getCategory());
        System.out.println("Start time: " + event.getStartsAt());
        System.out.println("Price: €" + event.getUnitPrice());
        System.out.println("Available tickets: " + event.getAvailableTickets());
        System.out.println();
    }

    private void printBooking(Booking booking){
        System.out.println("Booking ID: " + booking.getId());
        System.out.println("Event ID: " + booking.getEventId());
        System.out.println("Customer email: " + booking.getCustomerEmail());
        System.out.println("Quantity: " + booking.getQuantity());
        System.out.println("Total price: €" + booking.getTotalPrice());
        System.out.println("Booking status: " + booking.getStatus());
        System.out.println("Create time: " + booking.getCreatedAt());
        System.out.println();
    }

    private void printEventSalesSummary(
            EventSalesSummary summary,
            int rank
    ){
        System.out.println(rank + ". " + summary.eventTitle());
        System.out.println("   Tickets sold: " + summary.ticketsSold());
        System.out.println("   Revenue: €" + summary.revenue());
        System.out.println();
    }

    private EventCategory readCategory() {
        while (true){
            System.out.println("""
            Category (leave blank for any):
            1. MUSIC
            2. TECH
            3. SPORTS
            4. ART
            5. FOOD
            6. HIKING
            7. OTHER
            """);

            String input = scanner.nextLine().strip();

            switch (input) {
                case "1":
                    return EventCategory.MUSIC;
                case "2":
                    return EventCategory.TECH;
                case "3":
                    return EventCategory.SPORTS;
                case "4":
                    return EventCategory.ART;
                case "5":
                    return EventCategory.FOOD;
                case "6":
                    return EventCategory.HIKING;
                case "7":
                    return EventCategory.OTHER;
                case "":
                    return null;
                default:
                    System.out.println("Invalid category. Please choose 1-7 or press Enter.");
            }
        }

    }

    private EventSort readSort() {
        while (true){
            System.out.println("""
            Sort:
            1. Date ascending
            2. Date descending
            3. Price ascending
            4. Price descending
            """);

            String input = scanner.nextLine().strip();

            switch (input) {
                case "1":
                    return EventSort.DATE_ASC;
                case "2":
                    return EventSort.DATE_DESC;
                case "3":
                    return EventSort.PRICE_ASC;
                case "4":
                    return EventSort.PRICE_DESC;
                case "":
                    return null;
                default:
                    System.out.println("Invalid sorting option. Please choose 1-4 or press Enter.");
            }
        }

    }

    private ZonedDateTime readOptionalFromDate(){
        ZoneId madrid = ZoneId.of("Europe/Madrid");

        while(true){
            System.out.println("From date (yyyy-MM-dd, leave blank for any):");
            String input = scanner.nextLine().strip();

            if (input.isBlank()){
                return null;
            }

            try {
                LocalDate date = LocalDate.parse(input);
                return date.atStartOfDay(madrid);
            } catch (DateTimeParseException e){
                System.out.println("Invalid date. Please use yyyy-MM-dd.");
            }
        }
    }

    private ZonedDateTime readOptionalToDate() {
        ZoneId madrid = ZoneId.of("Europe/Madrid");

        while (true) {
            System.out.println(
                    "To date (yyyy-MM-dd, leave blank for any):"
            );

            String input = scanner.nextLine().strip();

            if (input.isBlank()) {
                return null;
            }

            try {
                LocalDate date = LocalDate.parse(input);

                return date
                        .plusDays(1)
                        .atStartOfDay(madrid)
                        .minusNanos(1);

            } catch (DateTimeParseException e) {
                System.out.println(
                        "Invalid date. Please use yyyy-MM-dd."
                );
            }
        }
    }

    private BigDecimal readOptionalPrice(){
        while (true){
            System.out.println("Maximum price (leave blank for any):");
            String input = scanner.nextLine().strip();
            if(input.isBlank()){
                return null;
            }

            try{
                BigDecimal price = new BigDecimal(input);
                if(price.compareTo(BigDecimal.ZERO) < 0){
                    System.out.println("Price must not be negative");
                    continue;
                }
                return price;
            } catch (NumberFormatException e){
                System.out.println("Invalid price. Please enter a number, for example 30.00.");
            }
        }
    }
}
