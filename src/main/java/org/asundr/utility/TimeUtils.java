package org.asundr.utility;

import org.asundr.TradeUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;

final public class TimeUtils
{
    public final static long SECONDS_IN_DAY = 3600*24;
    public final static long MILLISECONDS_IN_DAY = SECONDS_IN_DAY * 1000L;

    // Returns a string representation of the timestamp using the passed pattern
    // https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns
    public static String timestampToString(final long timestamp, final String pattern)
    {
        final Instant instant = Instant.ofEpochSecond(timestamp);
        final ZoneId zoneId = ZoneId.systemDefault();
        final ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, zoneId);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    // Returns a detailed representation of the time without the time zone
    public static String timestampToString(final long timestamp)
    {
        return timestampToString(timestamp, "yyyy-MM-dd H:mm:ss");
    }

    // Returns a string for the time depending on the age of the timestamp.
    // Timestamps from the current day show the time, and Yesterday for the previous day.
    // Timestamps within the year will show the month and date and anything older shows dd/MM/yy
    public static String timestampToStringTime(final long timestamp)
    {
        if (isInCurrentDay(timestamp * 1000))
        {
            return timestampToString(timestamp, TradeUtils.getConfig().use24HourTime() ? "H:mm" : "h:mm a");
        }
        else if (isInCurrentDay((timestamp + SECONDS_IN_DAY) * 1000))
        {
            return "Yesterday";
        }
        else if (isInCurrentYear(timestamp*1000))
        {
            return timestampToString(timestamp, "LLL d");
        }
        else
        {
            return timestampToString(timestamp, "dd/MM/yy");
        }
    }

    // Returns true if the passed timestamp in milliseconds is during the current year in the user's timezone
    public static boolean isInCurrentYear(final long timestamp)
    {
        LocalDate date = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        final int currentYear = LocalDate.now().getYear();
        return date.getYear() == currentYear;
    }

    // Returns true if the passed timestamp in milliseconds is during the current day in the user's timezone
    public static boolean isInCurrentDay(final long timestamp)
    {
        final LocalDate date = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        final int currentDay = LocalDate.now().getDayOfYear();
        final int currentYear = LocalDate.now().getYear();
        return date.getYear() == currentYear && date.getDayOfYear() == currentDay;
    }

    // Returns the number of milliseconds before midnight in the user's time zone
    public static long getTimeUntilMidnight()
    {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight).toMillis();
    }

}
