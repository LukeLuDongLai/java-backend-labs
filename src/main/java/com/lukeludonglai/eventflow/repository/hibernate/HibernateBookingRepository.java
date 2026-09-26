package com.lukeludonglai.eventflow.repository.hibernate;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.repository.BookingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HibernateBookingRepository implements BookingRepository {

    public final EntityManagerFactory emf;

    public HibernateBookingRepository(EntityManagerFactory emf){
        this.emf = emf;
    }
    @Override
    public Booking save(Booking booking) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try{
            transaction.begin();
            Booking managed = em.merge(booking);
            transaction.commit();
            return managed;
        } catch (RuntimeException e){
            if (transaction.isActive()){
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try{
            Booking booking = em.find(Booking.class, id);
            return Optional.ofNullable(booking);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Booking> findAll() {
        EntityManager em = emf.createEntityManager();
        try{
            return em.createQuery(
                    "select b from Booking b",
                    Booking.class
            ).getResultList();
        } finally {
            em.close();
        }
    }
}
