package com.lukeludonglai.eventflow.persistence.jdbc;

import com.lukeludonglai.eventflow.database.JdbcConnectionFactory;
import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.persistence.BookingPersistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZoneOffset;

public class JdbcBookingPersistence implements BookingPersistence {

    private final JdbcConnectionFactory connectionFactory;

    public JdbcBookingPersistence(
            JdbcConnectionFactory connectionFactory
    ) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void saveCreatedBooking(
            Event event,
            Booking booking
    ) {
        // transaction
        try (Connection connection =
                     connectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try {
                updateEvent(connection, event);

                insertBooking(connection, booking);

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();

                throw new RuntimeException(
                        "Failed to persist booking transaction",
                        e
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database transaction failed",
                    e
            );
        }
    }

    @Override
    public void saveCancelledBooking(Event event, Booking booking) {
        try(
                Connection connection = connectionFactory.getConnection()
                ) {
            connection.setAutoCommit(false);
            try{
                updateEvent(connection,event);
                cancelBooking(connection,booking);

                connection.commit();
            } catch (SQLException e){
                connection.rollback();
                throw new RuntimeException(
                        "Failed to persist booking cancellation transaction",
                        e
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException( "Database cancellation transaction failed", e);
        }
    }

    private void updateEvent(
            Connection connection,
            Event event
    ) throws SQLException {

        String sql = """
            UPDATE events
            SET available_tickets = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    event.getAvailableTickets()
            );

            statement.setObject(
                    2,
                    event.getId()
            );

            int rows = statement.executeUpdate();

            if (rows != 1) {
                throw new SQLException(
                        "Expected to update one event"
                );
            }
        }
    }

    private void insertBooking(
            Connection connection,
            Booking booking
    ) throws SQLException {

        String sql = """
            INSERT INTO bookings (
                id,
                event_id,
                customer_email,
                quantity,
                total_price,
                status,
                created_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // bind parameters
            statement.setObject(1, booking.getId());
            statement.setObject(2, booking.getEventId());
            statement.setString(3, booking.getCustomerEmail());
            statement.setInt(4, booking.getQuantity());
            statement.setBigDecimal(5, booking.getTotalPrice());
            statement.setString(6, booking.getStatus().name());
            statement.setObject(7, booking.getCreatedAt().atOffset(ZoneOffset.UTC));

            int rows = statement.executeUpdate();

            if (rows != 1) {
                throw new SQLException(
                        "Expected to insert one booking"
                );
            }
        }
    }

    private void cancelBooking(
            Connection connection,
            Booking booking
    ) throws SQLException {

        String sql = """
            UPDATE bookings
            SET status = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    booking.getStatus().name()
            );

            statement.setObject(
                    2,
                    booking.getId()
            );

            int rows = statement.executeUpdate();

            if (rows != 1) {
                throw new SQLException(
                        "Expected to update one booking"
                );
            }
        }
    }

}
