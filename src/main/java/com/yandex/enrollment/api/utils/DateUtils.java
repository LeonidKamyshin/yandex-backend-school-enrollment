package com.yandex.enrollment.api.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Утилита для упрощения работы с датамими
 * Работает с датами в формате ISO 8601
 * Строки - всегда дата по шаблону {@link DateUtils#formatter}
 */
public class DateUtils {

  /**
   * Минимальное значение даты
   */
  public final static String MIN_DATE = "1971-01-01T00:00:00.000Z";

  /**
   * Максимальное значение даты
   */
  public final static String MAX_DATE = "2038-01-01T00:00:00.000Z";

  private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd'T'HH:mm:ss.SSSX");

  /**
   * Переводит {@link OffsetDateTime} в строку в формате ISO 8601 с точностью до милисекунд
   *
   * @param date Дата для перевода
   * @return Дата в строке
   */
  public static String dateToString(OffsetDateTime date) {
    return date.format(formatter);
  }

  /**
   * Парсит дату из сроки в формате ISO 8601
   *
   * @param date Строка с датой
   * @return Полученная дата
   */
  public static OffsetDateTime stringToDate(String date) {
    return OffsetDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
  }

  /**
   * Приводит формат даты к {@link DateUtils#formatter}
   *
   * @param date Строка с датой
   * @return Строка с датой единым форматом
   */
  public static String unifyDate(String date) {
    return DateUtils.dateToString(DateUtils.stringToDate(date));
  }

  /**
   * Вычитает из даты {@code value}
   *
   * @param date  Дата из который надо вычесть {@code value}
   * @param value Вычитаемое
   * @param unit  Юнит времени
   * @return Разность
   */
  public static String minus(String date, int value, ChronoUnit unit) {
    return DateUtils.dateToString(DateUtils.stringToDate(date).minus(value, unit));
  }
}
