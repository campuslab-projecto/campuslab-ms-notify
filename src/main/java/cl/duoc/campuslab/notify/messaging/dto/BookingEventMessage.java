package cl.duoc.campuslab.notify.messaging.dto;

import java.time.LocalDateTime;

public class BookingEventMessage {

    private Long bookingId;
    private String status;
    private String type;
    private String message;

    private String eventId;
    private String traceId;
    private String correlationId;

    private LocalDateTime timestamp;

    public BookingEventMessage() {
    }

    public BookingEventMessage(Long bookingId,
                               String status,
                               String type,
                               String message,
                               String eventId,
                               String traceId,
                               String correlationId,
                               LocalDateTime timestamp) {
        this.bookingId = bookingId;
        this.status = status;
        this.type = type;
        this.message = message;
        this.eventId = eventId;
        this.traceId = traceId;
        this.correlationId = correlationId;
        this.timestamp = timestamp;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}