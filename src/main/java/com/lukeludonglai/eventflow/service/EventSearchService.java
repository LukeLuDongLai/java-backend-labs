package com.lukeludonglai.eventflow.service;

import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.repository.EventRepository;
import com.lukeludonglai.eventflow.search.EventSearchCriteria;
import com.lukeludonglai.eventflow.search.EventSort;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class EventSearchService {
    private final EventRepository eventRepository;

    public EventSearchService(EventRepository eventRepository){
        this.eventRepository = eventRepository;
    }

    public List<Event> search (EventSearchCriteria criteria){
        if (criteria == null){
            throw new IllegalArgumentException("Search criteria must not be null");
        }

        Stream<Event> eventStream = eventRepository.findAll().stream()
                .filter(Event::isPublished);

        if (criteria.keyword() != null){
            String keyword = criteria.keyword().strip().toLowerCase(Locale.ROOT);
            eventStream = eventStream.filter(event ->
                                                    event.getTitle().toLowerCase()
                                                    .contains(keyword));
        }

        if (criteria.category() != null){
            eventStream = eventStream.filter(event ->
                                            event.getCategory() == criteria.category());
        }

        if (criteria.from() != null){
            eventStream = eventStream.filter(event ->
                                            !event.getStartsAt().isBefore(criteria.from()));
        }

        if (criteria.to() != null){
            eventStream = eventStream.filter(event ->
                                            !event.getStartsAt().isAfter(criteria.to()));
        }

        if (criteria.maxPrice() != null){
            eventStream = eventStream.filter(event ->
                                            event.getUnitPrice().compareTo(criteria.maxPrice()) <= 0);
        }

        eventStream = eventStream.sorted(getComparator(criteria.sort()));

        return eventStream.toList();
    }

    private Comparator<Event> getComparator(EventSort sort){
        if (sort == null){
            sort = EventSort.DATE_DESC;
        }
        return switch (sort){
            case DATE_ASC ->
                    Comparator.comparing(Event::getStartsAt)
                            .thenComparing(Event::getTitle);
            case DATE_DESC ->
                    Comparator.comparing(Event::getStartsAt).reversed()
                            .thenComparing(Event::getTitle);
            case PRICE_ASC ->
                    Comparator.comparing(Event::getUnitPrice)
                            .thenComparing(Event::getTitle);
            case PRICE_DESC ->
                    Comparator.comparing(Event::getUnitPrice).reversed()
                            .thenComparing(Event::getTitle);
        };
    }

}
