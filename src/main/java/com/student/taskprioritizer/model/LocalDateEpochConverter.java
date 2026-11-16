package com.student.taskprioritizer.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * SQLite stores {@code due_date} as epoch milliseconds at local midnight.
 * Convert with the JVM default zone so calendar days match what the student entered
 * and {@link java.time.temporal.ChronoUnit#DAYS} is not off by one.
 */
@Converter(autoApply = false)
public class LocalDateEpochConverter implements AttributeConverter<LocalDate, Long> {

    private ZoneId zone() {
        return ZoneId.systemDefault();
    }

    @Override
    public Long convertToDatabaseColumn(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay(zone()).toInstant().toEpochMilli();
    }

    @Override
    public LocalDate convertToEntityAttribute(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis).atZone(zone()).toLocalDate();
    }
}
