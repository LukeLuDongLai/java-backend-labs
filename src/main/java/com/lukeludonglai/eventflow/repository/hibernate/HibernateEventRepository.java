package com.lukeludonglai.eventflow.repository.hibernate;

import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.repository.EventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HibernateEventRepository implements EventRepository {

    private final EntityManagerFactory entityManagerFactory;

    public HibernateEventRepository(EntityManagerFactory entityManagerFactory){
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Optional<Event> findById(UUID id) {

        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            Event event = em.find(Event.class, id);
            return Optional.ofNullable(event);
        }

    }

    @Override
    public List<Event> findAll() {

        try (EntityManager em = entityManagerFactory.createEntityManager()){
            return em.createQuery(
                    "select e from Event e",
                    Event.class
            ).getResultList();
        }

    }

    @Override
    public Event save(Event event) {

        try(EntityManager em = entityManagerFactory.createEntityManager()){

            EntityTransaction transaction = em.getTransaction();

            try{
                transaction.begin();

                Event managed = em.merge(event);

                transaction.commit();

                return managed;
            } catch (RuntimeException e){
                if (transaction.isActive()){
                    transaction.rollback();
                }
                throw e;
            }

        }
    }
}
