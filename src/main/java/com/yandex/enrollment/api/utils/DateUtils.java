package com.yandex.enrollment.api.utils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

  public final static String MIN_DATE = "1971-01-01T00:00:00.000Z";

  public final static String MAX_DATE = "2038-01-01T00:00:00.000Z";

  private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd'T'HH:mm:ss.SSSX");

  public static String dateToString(OffsetDateTime date) {
    return date.format(formatter);
  }

  public static OffsetDateTime stringToDate(String date) {
    return OffsetDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
  }

  public static String unifyDate(String date) {
    return DateUtils.dateToString(DateUtils.stringToDate(date));
  }

  public static String minus(String date, int value, ChronoUnit unit) {
    return DateUtils.dateToString(DateUtils.stringToDate(date).minus(value, unit));
  }
}
