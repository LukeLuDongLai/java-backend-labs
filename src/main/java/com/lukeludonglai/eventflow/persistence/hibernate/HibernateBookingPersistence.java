package com.lukeludonglai.eventflow.persistence.hibernate;

import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.persistence.BookingPersistence;
import com.lukeludonglai.eventflow.repository.hibernate.HibernateBookingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class HibernateBookingPersistence implements BookingPersistence {

    private final EntityManagerFactory emf;

    public HibernateBookingPersistence(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public void saveCreatedBooking(Event event, Booking booking) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try{
            transaction.begin();

            em.merge(event);
            em.persist(booking);

            transaction.commit();
        } catch (RuntimeException e){
            if (transaction.isActive()){
                transaction.rollback();
            } throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void saveCancelledBooking(Event event, Booking booking) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try{
            transaction.begin();

            em.merge(event);
            em.merge(booking);

            transaction.commit();
        } catch (RuntimeException e){
            if (transaction.isActive()){
                transaction.rollback();
            } throw e;
        } finally {
            em.close();
        }
    }
}
