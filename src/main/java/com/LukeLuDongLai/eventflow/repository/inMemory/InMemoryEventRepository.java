package com.LukeLuDongLai.eventflow.repository.inMemory;

import com.LukeLuDongLai.eventflow.domain.Event;
import com.LukeLuDongLai.eventflow.repository.EventRepository;

import java.util.*;

public class InMemoryEventRepository implements EventRepository {
    private final Map<UUID, Event> events = new HashMap<>();

    @Override
    public Optional<Event> findById(UUID id) {
        return Optional.ofNullable(events.get(id));
    }

    @Override
    public List<Event> findAll() {
        return new ArrayList<>(events.values());
    }

    @Override
    public Event save(Event event) {
        events.put(event.getId(), event);
        return event;
    }
}
