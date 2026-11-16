package com.student.taskprioritizer.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalDateEpochConverterTest {

    private final LocalDateEpochConverter converter = new LocalDateEpochConverter();

    @Test
    void roundTripsLocalDateThroughEpochMillisAtLocalMidnight() {
        LocalDate due = LocalDate.of(2026, 10, 26);
        Long epoch = converter.convertToDatabaseColumn(due);

        assertEquals(due.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), epoch);
        assertEquals(due, converter.convertToEntityAttribute(epoch));
    }

    @Test
    void existingSqliteEpochMillisMapsToLocalCalendarDay() {
        long storedEpoch = 1_792_953_000_000L;
        LocalDate due = converter.convertToEntityAttribute(storedEpoch);
        LocalDate expected = Instant.ofEpochMilli(storedEpoch)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        assertEquals(expected, due);
        assertEquals(0, ChronoUnit.DAYS.between(due, expected));
    }

    @Test
    void daysRemainingUsesLocalDatesNotUtcShift() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate due = today.plusDays(9);
        long epoch = converter.convertToDatabaseColumn(due);
        LocalDate restored = converter.convertToEntityAttribute(epoch);

        assertEquals(9, ChronoUnit.DAYS.between(today, restored));
    }
}
