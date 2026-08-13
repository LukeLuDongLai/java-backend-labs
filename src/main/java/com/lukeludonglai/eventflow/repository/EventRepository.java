package com.lukeludonglai.eventflow.repository;

import com.lukeludonglai.eventflow.domain.Event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    Optional<Event> findById(UUID id);
    List<Event> findAll();
    Event save(Event event);

}
