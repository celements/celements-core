package com.celements.metatag.enums;

import static org.junit.Assert.*;

import org.junit.Test;

public class ERobotTest {

  private static String FIELDS_INDEX = "index";
  private static String FIELDS_NOINDEX = "noindex";
  private static String FIELDS_FOLLOW = "follow";
  private static String FIELDS_NOFOLLOW = "nofollow";
  private static String FIELDS_NOODP = "noodp";
  private static String FIELDS_NOARCHIVE = "noarchive";
  private static String FIELDS_NOSNIPPET = "nosnippet";
  private static String FIELDS_NOIMAGEINDEX = "noimageindex";
  private static String FIELDS_NOCACHE = "nocache";

  @Test
  public void testGetRobot() {
    assertEquals(ERobot.INDEX, ERobot.getRobot(FIELDS_INDEX).get());
    assertEquals(ERobot.NOINDEX, ERobot.getRobot(FIELDS_NOINDEX).get());
    assertEquals(ERobot.FOLLOW, ERobot.getRobot(FIELDS_FOLLOW).get());
    assertEquals(ERobot.NOFOLLOW, ERobot.getRobot(FIELDS_NOFOLLOW).get());
    assertEquals(ERobot.NOODP, ERobot.getRobot(FIELDS_NOODP).get());
    assertEquals(ERobot.NOARCHIVE, ERobot.getRobot(FIELDS_NOARCHIVE).get());
    assertEquals(ERobot.NOSNIPPET, ERobot.getRobot(FIELDS_NOSNIPPET).get());
    assertEquals(ERobot.NOIMAGEINDEX, ERobot.getRobot(FIELDS_NOIMAGEINDEX).get());
    assertEquals(ERobot.NOCACHE, ERobot.getRobot(FIELDS_NOCACHE).get());
  }

  @Test
  public void testGetIdentifier() {
    assertEquals(FIELDS_INDEX, ERobot.INDEX.getIdentifier());
    assertEquals(FIELDS_NOINDEX, ERobot.NOINDEX.getIdentifier());
    assertEquals(FIELDS_FOLLOW, ERobot.FOLLOW.getIdentifier());
    assertEquals(FIELDS_NOFOLLOW, ERobot.NOFOLLOW.getIdentifier());
    assertEquals(FIELDS_NOODP, ERobot.NOODP.getIdentifier());
    assertEquals(FIELDS_NOARCHIVE, ERobot.NOARCHIVE.getIdentifier());
    assertEquals(FIELDS_NOSNIPPET, ERobot.NOSNIPPET.getIdentifier());
    assertEquals(FIELDS_NOIMAGEINDEX, ERobot.NOIMAGEINDEX.getIdentifier());
    assertEquals(FIELDS_NOCACHE, ERobot.NOCACHE.getIdentifier());
  }
}
