package com.celements.collections.service;

import static com.google.common.base.Predicates.*;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;

import com.celements.common.date.DateFormat;
import com.celements.common.date.DateUtil;
import com.google.common.base.Enums;
import com.google.common.base.Strings;

@Component("date")
public class DateScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DateScriptService.class);

  public static final String PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  public ZoneId getZone() {
    return DateUtil.getDefaultZone();
  }

  public ZoneId getZone(String zone) {
    try {
      return guard(zone).map(ZoneId::of).orElseGet(this::getZone);
    } catch (DateTimeException exc) {
      return getZone();
    }
  }

  public Instant getNow() {
    return Instant.now();
  }

  public ZonedDateTime getNowAtZone() {
    return getNowAtZone(getZone());
  }

  public ZonedDateTime getNowAtZone(String zone) {
    return getNowAtZone(getZone(zone));
  }

  public ZonedDateTime getNowAtZone(ZoneId zone) {
    return ZonedDateTime.now(guard(zone).orElseGet(this::getZone));
  }

  public Instant toInstant(Date date) {
    return guard(date).map(Date::toInstant).orElse(null);
  }

  public ZonedDateTime atZone(Date date) {
    return atZone(date, getZone());
  }

  public ZonedDateTime atZone(Date date, String zone) {
    return atZone(date, getZone(zone));
  }

  public ZonedDateTime atZone(Date date, ZoneId zone) {
    return guard(date).map(Date::toInstant)
        .map(instant -> instant.atZone(guard(zone).orElseGet(this::getZone)))
        .orElse(null);
  }

  public LocalDate getLocalDate(int year, int month, int dayOfMonth) {
    try {
      return LocalDate.of(year, month, dayOfMonth);
    } catch (DateTimeException exc) {
      LOGGER.info("getLocalDate - failed for [{}-{}-{}]", year, month, dayOfMonth);
      return null;
    }
  }

  public Duration getDuration(long amount, ChronoUnit unit) {
    return Optional.ofNullable(unit).map(u -> Duration.of(amount, u)).orElse(null);
  }

  public Duration getDuration(long amount, String unit) {
    return getDuration(amount, getChronoUnit(unit));
  }

  public ChronoUnit getChronoUnit(String unit) {
    String name = Strings.nullToEmpty(unit).toUpperCase();
    return Enums.getIfPresent(ChronoUnit.class, name)
        .or(() -> Enums.getIfPresent(ChronoUnit.class, name + "S")
            .orNull());
  }

  public Date toDate(Temporal temporal) {
    try {
      return guard(temporal).map(DateUtil::atZone).map(ZonedDateTime::toInstant).map(Date::from)
          .orElse(null);
    } catch (DateTimeException | IllegalArgumentException exc) {
      LOGGER.info("toDate - failed for [{}]", temporal);
      return null;
    }
  }

  public String format(String pattern, Temporal temporal) {
    return format(pattern, temporal, null);
  }

  public String format(String pattern, Temporal temporal, Locale locale) {
    try {
      return guard(temporal)
          .map(guard(pattern)
              .map(p -> DateFormat.formatter(p, guard(locale).orElseGet(Locale::getDefault)))
              .orElseGet(() -> (t -> null)))
          .orElse(null);
    } catch (DateTimeException exc) {
      LOGGER.info("format - failed for [{}] with pattern [{}] and locale [{}]", temporal, pattern,
          locale, exc);
      return null;
    }
  }

  public String format(String pattern, Date date) {
    return this.format(pattern, toInstant(date));
  }

  public String formatISO(Temporal temporal) {
    return format(PATTERN_ISO, DateUtil.atZone(temporal, ZoneId.of("UTC")));
  }

  public String formatISO(Date date) {
    return this.formatISO(toInstant(date));
  }

  public ZonedDateTime parse(String pattern, String text) {
    return parse(pattern, DateUtil.getDefaultZone(), text);
  }

  public ZonedDateTime parse(String pattern, ZoneId zone, String text) {
    try {
      ZoneId zoneId = guard(zone).orElseGet(DateUtil::getDefaultZone);
      return guard(text)
          .map(guard(pattern).map(p -> DateFormat.parser(p, zoneId)).orElseGet(() -> (t -> null)))
          .orElse(null);
    } catch (DateTimeException exc) {
      LOGGER.info("parse - failed for [{}] with pattern [{}]", text, pattern, exc);
      return null;
    }
  }

  public ZonedDateTime parseISO(String text) {
    return parse(PATTERN_ISO, ZoneId.of("UTC"), text);
  }

  private <T> Optional<T> guard(T obj) {
    return Optional.ofNullable(obj);
  }

  private Optional<String> guard(String str) {
    return Optional.ofNullable(str).map(String::trim).filter(not(String::isEmpty));
  }

}
