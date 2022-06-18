package com.yandex.enrollment.api.utils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

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
}
