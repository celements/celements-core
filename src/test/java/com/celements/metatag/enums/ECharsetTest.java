package com.celements.metatag.enums;

import static org.junit.Assert.*;

import org.junit.Test;

import com.celements.metatag.enums.opengraph.EOpenGraph;

public class ECharsetTest {

  private static String FIELDS_UTF8 = "UTF-8";
  private static String FIELDS_LATIN1 = "ISO-8859-1";
  private static String FIELDS_USASCII = "US-ASCII";

  @Test
  public void testAttribs() {
    assertEquals("property", EOpenGraph.ATTRIB_NAME);
  }

  @Test
  public void testGetCharset() {
    assertTrue(ECharset.getCharset(FIELDS_UTF8).equals(ECharset.UTF8) || ECharset.getCharset(
        FIELDS_UTF8).get().equals(ECharset.DEFAULT));
    assertTrue(ECharset.getCharset(FIELDS_LATIN1).equals(ECharset.LATIN1) || ECharset.getCharset(
        FIELDS_LATIN1).get().equals(ECharset.ISO8859_1));
    assertEquals(ECharset.USASCII, ECharset.getCharset(FIELDS_USASCII).get());
  }

  @Test
  public void testGetIdentifier() {
    assertEquals(FIELDS_UTF8, ECharset.UTF8.getIdentifier());
    assertEquals(FIELDS_LATIN1, ECharset.LATIN1.getIdentifier());
    assertEquals(FIELDS_LATIN1, ECharset.ISO8859_1.getIdentifier());
    assertEquals(FIELDS_USASCII, ECharset.USASCII.getIdentifier());
    assertEquals(FIELDS_UTF8, ECharset.DEFAULT.getIdentifier());
  }
}
