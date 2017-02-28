package com.celements.metatag.enums;

import static org.junit.Assert.*;

import org.junit.Test;

public class ENameNonStandardTest {

  private static String FIELDS_CREATOR = "creator";
  private static String FIELDS_GOOGLEBOT = "googlebot";
  private static String FIELDS_PUBLISHER = "publisher";
  private static String FIELDS_ROBOTS = "robots";
  private static String FIELDS_SLURP = "slurp";
  private static String FIELDS_VIEWPORT = "viewport";

  @Test
  public void testAttribs() {
    assertEquals("name", ENameStandard.ATTRIB_NAME);
    assertEquals("property", ENameStandard.ATTRIB_NAME_ALT);
  }

  @Test
  public void testGetName() {
    assertEquals(ENameNonStandard.CREATOR, ENameNonStandard.getName(FIELDS_CREATOR).get());
    assertEquals(ENameNonStandard.GOOGLEBOT, ENameNonStandard.getName(FIELDS_GOOGLEBOT).get());
    assertEquals(ENameNonStandard.PUBLISHER, ENameNonStandard.getName(FIELDS_PUBLISHER).get());
    assertEquals(ENameNonStandard.ROBOTS, ENameNonStandard.getName(FIELDS_ROBOTS).get());
    assertEquals(ENameNonStandard.SLURP, ENameNonStandard.getName(FIELDS_SLURP).get());
    assertEquals(ENameNonStandard.VIEWPORT, ENameNonStandard.getName(FIELDS_VIEWPORT).get());
  }

  @Test
  public void testGetIdentifier() {
    assertEquals(FIELDS_CREATOR, ENameNonStandard.CREATOR.getIdentifier());
    assertEquals(FIELDS_GOOGLEBOT, ENameNonStandard.GOOGLEBOT.getIdentifier());
    assertEquals(FIELDS_PUBLISHER, ENameNonStandard.PUBLISHER.getIdentifier());
    assertEquals(FIELDS_ROBOTS, ENameNonStandard.ROBOTS.getIdentifier());
    assertEquals(FIELDS_SLURP, ENameNonStandard.SLURP.getIdentifier());
    assertEquals(FIELDS_VIEWPORT, ENameNonStandard.VIEWPORT.getIdentifier());
  }
}
