package com.celements.collections.service;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.temporal.Temporal;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.script.service.ScriptService;

import com.celements.common.date.DateUtil;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class DateScriptServiceTest extends AbstractComponentTest {

  private DateScriptService ss;

  @Before
  public void prepareTest() {
    ss = (DateScriptService) Utils.getComponent(ScriptService.class, "date");
  }

  @Test
  public void test_getNow() {
    Instant before = Instant.now();
    Instant now = ss.getNow();
    Instant after = Instant.now();
    assertTrue(!before.isAfter(now));
    assertTrue(!after.isBefore(now));
  }

  @Test
  public void test_format() {
    LocalDate dt = LocalDate.of(2020, 4, 1);
    String pattern = "dd.MM.yyyy HH:mm";
    ZoneId zone = DateUtil.getDefaultZone();
    assertEquals("01.04.2020 00:00", ss.format(pattern, dt));
    assertEquals("01.04.2020 05:13", ss.format(pattern, dt.atTime(5, 13)));
    assertEquals("01.04.2020 00:00", ss.format(pattern, dt.atStartOfDay(zone)));
    assertEquals("01.04.2020 00:00", ss.format(pattern, dt.atStartOfDay(zone).toInstant()));
    assertNull(ss.format(null, dt));
    assertNull(ss.format(" ", dt));
    assertNull(ss.format("asdf", dt));
    assertNull(ss.format(pattern, (Temporal) null));
  }

  @Test
  public void test_format_ISO() {
    LocalDateTime dt = LocalDate.of(2022, 8, 14).atTime(20, 54, 04, 123000000);
    assertEquals("2022-08-14T20:54:04.123Z", ss.formatISO(dt));
  }

  @Test
  public void test_parse() {
    LocalDate dt = LocalDate.of(2020, 4, 1);
    String pattern = "dd.MM.yyyy HH:mm";
    ZoneId zone = DateUtil.getDefaultZone();
    assertEquals(dt.atTime(5, 13).atZone(zone), ss.parse(pattern, "01.04.2020 05:13"));
    assertEquals(dt.atStartOfDay(zone), ss.parse(pattern, "01.04.2020 00:00"));
    assertEquals(Year.of(2020).atDay(1).atStartOfDay(zone), ss.parse("yyyy", "2020"));
    assertNull(ss.parse(null, "01.04.2020 05:13"));
    assertNull(ss.parse(" ", "01.04.2020 05:13"));
    assertNull(ss.parse("asdf", "01.04.2020 05:13"));
    assertNull(ss.parse(pattern, null));
    assertNull(ss.parse(pattern, " "));
    assertNull(ss.parse(pattern, "asdf"));
  }

}
