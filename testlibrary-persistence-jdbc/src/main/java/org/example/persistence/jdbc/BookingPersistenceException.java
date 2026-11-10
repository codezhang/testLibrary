package org.example.persistence.jdbc;

public class BookingPersistenceException extends RuntimeException {

    public BookingPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
