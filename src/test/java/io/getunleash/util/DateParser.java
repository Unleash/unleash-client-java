package io.getunleash.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DateParser {
    private static final List<DateTimeFormatter> formatters = new ArrayList<>();

    static {
        formatters.add(DateTimeFormatter.ISO_INSTANT);
        formatters.add(DateTimeFormatter.ISO_DATE_TIME);
        formatters.add(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        formatters.add(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public static ZonedDateTime parseDate(String date) {
        if (date != null && date.length() > 0) {
            return formatters.stream()
                    .map(
                            f -> {
                                try {
                                    return ZonedDateTime.parse(date, f);
                                } catch (DateTimeParseException dateTimeParseException) {
                                    return null;
                                }
                            })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseGet(
                            () -> {
                                try {
                                    return LocalDateTime.parse(
                                                    date, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                            .atZone(ZoneOffset.UTC);
                                } catch (DateTimeParseException dateTimeParseException) {
                                    return null;
                                }
                            });
        } else {
            return null;
        }
    }
}
