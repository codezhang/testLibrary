package org.example.wrapper.persistence;

import org.example.core.mapper.BookingTransactionMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "library.persistence", havingValue = "memory", matchIfMissing = true)
public class InMemoryPersistenceConfiguration {

    @Bean
    public BookingTransactionMapper bookingTransactionMapper() {
        return new InMemoryBookingTransactionMapper();
    }
}
