package com.celements.metatag.enums;

import static org.junit.Assert.*;

import org.junit.Test;

public class ENameStandardTest {

  private static String FIELDS_APPLICATION_NAME = "application-name";
  private static String FIELDS_AUTHOR = "author";
  private static String FIELDS_DESCRIPTION = "description";
  private static String FIELDS_GENERATOR = "generator";
  private static String FIELDS_KEYWORDS = "keywords";
  private static String FIELDS_REFERRER = "referrer";

  @Test
  public void testAttribs() {
    assertEquals("name", ENameStandard.ATTRIB_NAME);
    assertEquals("property", ENameStandard.ATTRIB_NAME_ALT);
  }

  @Test
  public void testGetName() {
    assertEquals(ENameStandard.APPLICATION_NAME, ENameStandard.getName(
        FIELDS_APPLICATION_NAME).get());
    assertEquals(ENameStandard.AUTHOR, ENameStandard.getName(FIELDS_AUTHOR).get());
    assertEquals(ENameStandard.DESCRIPTION, ENameStandard.getName(FIELDS_DESCRIPTION).get());
    assertEquals(ENameStandard.GENERATOR, ENameStandard.getName(FIELDS_GENERATOR).get());
    assertEquals(ENameStandard.KEYWORDS, ENameStandard.getName(FIELDS_KEYWORDS).get());
    assertEquals(ENameStandard.REFERRER, ENameStandard.getName(FIELDS_REFERRER).get());
  }

  @Test
  public void testGetIdentifier() {
    assertEquals(FIELDS_APPLICATION_NAME, ENameStandard.APPLICATION_NAME.getIdentifier());
    assertEquals(FIELDS_AUTHOR, ENameStandard.AUTHOR.getIdentifier());
    assertEquals(FIELDS_DESCRIPTION, ENameStandard.DESCRIPTION.getIdentifier());
    assertEquals(FIELDS_GENERATOR, ENameStandard.GENERATOR.getIdentifier());
    assertEquals(FIELDS_KEYWORDS, ENameStandard.KEYWORDS.getIdentifier());
    assertEquals(FIELDS_REFERRER, ENameStandard.REFERRER.getIdentifier());
  }
}
