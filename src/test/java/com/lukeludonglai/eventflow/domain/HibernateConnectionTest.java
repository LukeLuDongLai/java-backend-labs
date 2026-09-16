package com.lukeludonglai.eventflow.domain;

import com.lukeludonglai.eventflow.database.JpaEntityManagerFactory;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HibernateConnectionTest {

    @Test
    void shouldStartHibernate() {
        System.out.println("DB_URL = " + System.getenv("DB_URL"));
        System.out.println("DB_USER = " + System.getenv("DB_USER"));
        System.out.println("DB_PASSWORD exists = "
                + (System.getenv("DB_PASSWORD") != null));

        EntityManagerFactory emf =
                JpaEntityManagerFactory.create();

        EntityManager em =
                emf.createEntityManager();

        assertTrue(em.isOpen());

        em.close();
        emf.close();
    }

    @Test
    void shouldPersistEvent() {
        EntityManagerFactory emf =
                JpaEntityManagerFactory.create();

        EntityManager em =
                emf.createEntityManager();

        EntityTransaction tx =
                em.getTransaction();

        try {
            tx.begin();

            Event event = new Event(
                    "Hibernate Test Event",
                    EventCategory.TECH,
                    ZonedDateTime.now().plusDays(30),
                    new BigDecimal("20.00"),
                    50
            );

            em.persist(event);

            tx.commit();

        } finally {
            em.close();
            emf.close();
        }
    }

    @Test
    void shouldShowManagedEntities() {
        EntityManagerFactory emf =
                JpaEntityManagerFactory.create();


        try {
            System.out.println("Managed entities:");

            emf.getMetamodel()
                    .getEntities()
                    .forEach(entity ->
                            System.out.println(
                                    entity.getJavaType().getName()
                            )
                    );

            System.out.println("Event class name: " + Event.class.getName());
            System.out.println("Event annotation: " + Event.class.getAnnotation(Entity.class));
        } finally {
            emf.close();
        }
    }

    @Test
    void shouldShowPersistenceFiles() throws Exception {
        var resources = Thread.currentThread()
                .getContextClassLoader()
                .getResources("META-INF/persistence.xml");

        while (resources.hasMoreElements()) {
            System.out.println(
                    "persistence.xml = " + resources.nextElement()
            );
        }
    }

}