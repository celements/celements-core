package com.celements.collections.service;

import static com.google.common.base.Predicates.*;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;

import com.celements.common.date.DateFormat;
import com.celements.common.date.DateUtil;

@Component("date")
public class DateScriptService implements ScriptService {

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

  public Date toDate(TemporalAccessor temporal) {
    try {
      return guard(temporal).map(Instant::from).map(Date::from).orElse(null);
    } catch (DateTimeException | IllegalArgumentException exc) {
      return null;
    }
  }

  public String format(String pattern, TemporalAccessor temporal) {
    try {
      return guard(temporal)
          .map(guard(pattern).map(DateFormat::formatter).orElseGet(() -> (t -> null)))
          .orElse(null);
    } catch (DateTimeException exc) {
      return null;
    }
  }

  public TemporalAccessor parse(String pattern, String text) {
    try {
      return guard(text)
          .map(guard(pattern).map(DateFormat::parser).orElseGet(() -> (t -> null)))
          .orElse(null);
    } catch (DateTimeParseException exc) {
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
