package com.lukeludonglai.eventflow.repository.jdbc;

import com.lukeludonglai.eventflow.database.JdbcConnectionFactory;
import com.lukeludonglai.eventflow.domain.Booking;
import com.lukeludonglai.eventflow.domain.BookingStatus;
import com.lukeludonglai.eventflow.repository.BookingRepository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcBookingRepository implements BookingRepository {
    private final JdbcConnectionFactory connectionFactory;

    public JdbcBookingRepository(
            JdbcConnectionFactory connectionFactory
    ){
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Booking save(Booking booking) {
        String sql = """
                INSERT INTO bookings(
                    id,
                    event_id,
                    customer_email,
                    quantity,
                    total_price,
                    status,
                    created_at
                )
                VALUES(?,?,?,?,?,?,?)
                
                ON CONFLICT (id)
                DO UPDATE SET
                    status = EXCLUDED.status 
                """;
        try(Connection connection = connectionFactory.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, booking.getId());
            statement.setObject(2, booking.getEventId());
            statement.setString(3, booking.getCustomerEmail());
            statement.setInt(4, booking.getQuantity());
            statement.setBigDecimal(5, booking.getTotalPrice());
            statement.setString(6, booking.getStatus().name());
            statement.setObject(7, booking.getCreatedAt().atOffset(ZoneOffset.UTC));

            int rows = statement.executeUpdate();

            if (rows != 1){
                throw new IllegalStateException("Expected to save exactly one booking, but affected " + rows + " rows");
            }

            return booking;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save booking: " + booking.getId(), e);
        }
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        String sql = """
                SELECT 
                    id,
                    event_id,
                    customer_email,
                    quantity,
                    total_price,
                    status,
                    created_at
                FROM bookings
                WHERE id = ?
                """;
        try(
                Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)){

                statement.setObject(1, id);
                try(ResultSet rs = statement.executeQuery()){
                    if (rs.next()){
                        return Optional.of(mapBooking(rs));
                    }
                    return Optional.empty();
                }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find booking by id", e);
        }
    }

    @Override
    public List<Booking> findAll() {
        String sql = """
                SELECT 
                    id,
                    event_id,
                    customer_email,
                    quantity,
                    total_price,
                    status,
                    created_at
                FROM bookings
                """;
        try(
                Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)){

            try(ResultSet rs = statement.executeQuery()){
                List<Booking> bookings = new ArrayList<>();

                while (rs.next()){
                    bookings.add(mapBooking(rs));
                }
                return bookings;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find bookings", e);
        }
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        UUID eventId = rs.getObject("event_id", UUID.class);
        String customerEmail = rs.getString("customer_email");
        int quantity = rs.getInt("quantity");
        BigDecimal totalPrice = rs.getBigDecimal("total_price");
        BookingStatus status = BookingStatus.valueOf(rs.getString("status"));
        Instant createdAt = rs.getObject("created_at", OffsetDateTime.class).toInstant();

        return Booking.rehydrate(
                id,
                eventId,
                customerEmail,
                quantity,
                totalPrice,
                status,
                createdAt
        );
    }
}
