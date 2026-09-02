package com.lukeludonglai.eventflow.repository.jdbc;

import com.lukeludonglai.eventflow.database.JdbcConnectionFactory;
import com.lukeludonglai.eventflow.domain.Event;
import com.lukeludonglai.eventflow.domain.EventCategory;
import com.lukeludonglai.eventflow.domain.EventStatus;
import com.lukeludonglai.eventflow.repository.EventRepository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcEventRepository implements EventRepository {
    private final JdbcConnectionFactory connectionFactory;

    public JdbcEventRepository(
            JdbcConnectionFactory connectionFactory
    ){
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Event> findById(UUID id) {
        String sql = """
                SELECT
                    id,
                    title,
                    category,
                    starts_at,
                    unit_price,
                    capacity,
                    available_tickets,
                    status
                FROM events
                WHERE id = ?
                """;

        try(
                Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
                ){
            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    return Optional.of(mapEvent(resultSet));
                }

                return Optional.empty();
            }
        } catch (SQLException e){
            throw new RuntimeException("Failed to find event by id", e);
        }
    }

    @Override
    public List<Event> findAll() {
        String sql = """
                SELECT
                    id,
                    title,
                    category,
                    starts_at,
                    unit_price,
                    capacity,
                    available_tickets,
                    status
                FROM events
                """;

        try(
                Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
                ){
            try (ResultSet rs = statement.executeQuery()){
                List<Event> events = new ArrayList<>();
                while(rs.next()){
                    events.add(mapEvent(rs));
                }
                return events;
            }
        } catch (SQLException e){
            throw new RuntimeException("Failed to find events", e);
        }
    }

    @Override
    public Event save(Event event) {
        String sql = """
                INSERT INTO events (
                    id,
                    title,
                    category,
                    starts_at,
                    unit_price,
                    capacity,
                    available_tickets,
                    status
                )
                VALUES (?,?,?,?,?,?,?,?)
                
                ON CONFLICT (id)
                DO UPDATE SET
                    title = EXCLUDED.title,
                    category = EXCLUDED.category,
                    starts_at = EXCLUDED.starts_at,
                    unit_price = EXCLUDED.unit_price,
                    capacity = EXCLUDED.capacity,
                    available_tickets = EXCLUDED.available_tickets,
                    status = EXCLUDED.status;
                """;
        try ( Connection connection = connectionFactory.getConnection();
              PreparedStatement statement = connection.prepareStatement(sql)
            ) {
            statement.setObject(1, event.getId());
            statement.setString(2, event.getTitle());
            statement.setString(3, event.getCategory().name());
            statement.setObject(4, event.getStartsAt().toOffsetDateTime());
            statement.setBigDecimal(5, event.getUnitPrice());
            statement.setInt(6, event.getCapacity());
            statement.setInt(7, event.getAvailableTickets());
            statement.setString(8,event.getStatus().name());

            int rows = statement.executeUpdate();

            if(rows != 1){
                throw new IllegalStateException("Expected to save exactly one event, but affected " + rows + " rows");
            }

            return event;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save event: " + event.getId(), e);
        }
    }

    private Event mapEvent(ResultSet rs) throws SQLException{
        UUID id = rs.getObject("id", UUID.class);
        String title = rs.getString("title");
        EventCategory category = EventCategory.valueOf(rs.getString("category"));
        BigDecimal unitPrice = rs.getBigDecimal("unit_price");
        int capacity = rs.getInt("capacity");
        int availableTickets = rs.getInt("available_tickets");
        EventStatus status = EventStatus.valueOf(rs.getString("status"));
        OffsetDateTime offsetDateTime = rs.getObject("starts_at", OffsetDateTime.class);
        ZonedDateTime startsAt = offsetDateTime.atZoneSameInstant(ZoneId.of("Europe/Madrid"));

        return Event.rehydrate(
                id,
                title,
                category,
                startsAt,
                unitPrice,
                capacity,
                availableTickets,
                status
        );
    }
}
