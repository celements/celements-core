package com.celements.collections.service;

import static com.google.common.base.Predicates.*;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;

import com.celements.common.date.DateFormat;
import com.celements.common.date.DateUtil;

@Component("date")
public class DateScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DateScriptService.class);

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

  public Date toDate(Temporal temporal) {
    try {
      return guard(temporal).map(Instant::from).map(Date::from).orElse(null);
    } catch (DateTimeException | IllegalArgumentException exc) {
      LOGGER.info("toDate - failed for [{}]", temporal);
      return null;
    }
  }

  public String format(String pattern, Temporal temporal) {
    try {
      return guard(temporal)
          .map(guard(pattern).map(DateFormat::formatter).orElseGet(() -> (t -> null)))
          .orElse(null);
    } catch (DateTimeException exc) {
      LOGGER.info("format - failed for [{}] with pattern [{}]", temporal, pattern, exc);
      return null;
    }
  }

  public ZonedDateTime parse(String pattern, String text) {
    try {
      return guard(text)
          .map(guard(pattern).map(DateFormat::parser).orElseGet(() -> (t -> null)))
          .orElse(null);
    } catch (DateTimeException exc) {
      LOGGER.info("parse - failed for [{}] with pattern [{}]", text, pattern, exc);
      return null;
    }
  }

  private <T> Optional<T> guard(T obj) {
    return Optional.ofNullable(obj);
  }

  private Optional<String> guard(String str) {
    return Optional.ofNullable(str).map(String::trim).filter(not(String::isEmpty));
  }

}
