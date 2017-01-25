package com.celements.metatag.enums;

import static org.junit.Assert.*;

import org.junit.Test;

public class EReferrerTest {

  private static String FIELDS_NO_REFFERER = "no-referrer";
  private static String FIELDS_ORIGIN = "origin";
  private static String FIELDS_NO_REFERRER_WHEN_DOWNGRADE = "no-referrer-when-downgrade";
  private static String FIELDS_ORIGIN_WHEN_CROSSORIGIN = "origin-when-crossorigin";
  private static String FIELDS_UNSAVE_URL = "unsafe-URL";

  @Test
  public void testGetReferrer() {
    assertEquals(EReferrer.NO_REFFERER, EReferrer.getReferrer(FIELDS_NO_REFFERER));
    assertEquals(EReferrer.ORIGIN, EReferrer.getReferrer(FIELDS_ORIGIN));
    assertEquals(EReferrer.NO_REFERRER_WHEN_DOWNGRADE, EReferrer.getReferrer(
        FIELDS_NO_REFERRER_WHEN_DOWNGRADE));
    assertEquals(EReferrer.ORIGIN_WHEN_CROSSORIGIN, EReferrer.getReferrer(
        FIELDS_ORIGIN_WHEN_CROSSORIGIN));
    assertEquals(EReferrer.UNSAVE_URL, EReferrer.getReferrer(FIELDS_UNSAVE_URL));
  }

  @Test
  public void testGetIdentifier() {
    assertEquals(FIELDS_NO_REFFERER, EReferrer.NO_REFFERER.getIdentifier());
    assertEquals(FIELDS_ORIGIN, EReferrer.ORIGIN.getIdentifier());
    assertEquals(FIELDS_NO_REFERRER_WHEN_DOWNGRADE,
        EReferrer.NO_REFERRER_WHEN_DOWNGRADE.getIdentifier());
    assertEquals(FIELDS_ORIGIN_WHEN_CROSSORIGIN, EReferrer.ORIGIN_WHEN_CROSSORIGIN.getIdentifier());
    assertEquals(FIELDS_UNSAVE_URL, EReferrer.UNSAVE_URL.getIdentifier());
  }
}
