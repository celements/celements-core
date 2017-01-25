package com.celements.metatag.enums.twitter;

import static org.junit.Assert.*;

import org.junit.Test;

public class ETwitterCardTypeTest {

  private static String FIELDS_SUMMARY = "summary";
  private static String FIELDS_SUMMARY_LARGE_IMAGE = "summary_large_image";
  private static String FIELDS_PLAYER = "player";
  private static String FIELDS_APP = "app";

  @Test
  public void testGetTwitterCardType() {
    assertEquals(ETwitterCardType.SUMMARY, ETwitterCardType.getTwitterCardType(FIELDS_SUMMARY));
    assertEquals(ETwitterCardType.SUMMARY_LARGE_IMAGE, ETwitterCardType.getTwitterCardType(
        FIELDS_SUMMARY_LARGE_IMAGE));
    assertEquals(ETwitterCardType.PLAYER, ETwitterCardType.getTwitterCardType(FIELDS_PLAYER));
    assertEquals(ETwitterCardType.APP, ETwitterCardType.getTwitterCardType(FIELDS_APP));
  }

  @Test
  public void testGetIdentifier() {
    assertEquals(FIELDS_SUMMARY, ETwitterCardType.SUMMARY.getIdentifier());
    assertEquals(FIELDS_SUMMARY_LARGE_IMAGE, ETwitterCardType.SUMMARY_LARGE_IMAGE.getIdentifier());
    assertEquals(FIELDS_PLAYER, ETwitterCardType.PLAYER.getIdentifier());
    assertEquals(FIELDS_APP, ETwitterCardType.APP.getIdentifier());
  }
}
