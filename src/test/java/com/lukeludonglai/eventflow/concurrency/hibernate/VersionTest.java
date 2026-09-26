package com.lukeludonglai.eventflow.concurrency.hibernate;

import com.lukeludonglai.eventflow.database.JpaEntityManagerFactory;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.repository.hibernate.HibernateEventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.lukeludonglai.eventflow.helper.TestDataHelper.createPublishedEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VersionTest {
    private static EntityManagerFactory emf;
    private HibernateEventRepository eventRepository;

    @BeforeAll
    static void startHibernate() {
        emf = JpaEntityManagerFactory.create();
    }

    @AfterAll
    static void stopHibernate() {
        emf.close();
    }

    @BeforeEach
    void setUp() {
        eventRepository =
                new HibernateEventRepository(emf);
    }

    @Test
    void shouldDetectConcurrentEventUpdate() {
        Event event = createPublishedEvent(10);

        eventRepository.save(event);

        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();

        EntityTransaction tx1 = em1.getTransaction();
        EntityTransaction tx2 = em2.getTransaction();

        try {
            tx1.begin();
            tx2.begin();

            Event event1 =
                    em1.find(Event.class, event.getId());

            Event event2 =
                    em2.find(Event.class, event.getId());

            assertEquals(
                    event1.getVersion(),
                    event2.getVersion()
            );

            event1.reserveTickets(1);
            event2.reserveTickets(1);

            tx1.commit();

            assertThrows(
                    RuntimeException.class,
                    tx2::commit
            );

        } finally {
            if (tx1.isActive()) {
                tx1.rollback();
            }

            if (tx2.isActive()) {
                tx2.rollback();
            }

            em1.close();
            em2.close();
        }

        Event storedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(
                9,
                storedEvent.getAvailableTickets()
        );
    }
}