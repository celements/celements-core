package com.celements.metatag.enums;

import static org.junit.Assert.*;

import org.junit.Test;

public class EViewportTest {

  private static String FIELDS_HEIGHT = "height";
  private static String FIELDS_WIDTH = "width";
  private static String FIELDS_INITIAL_SCALE = "initial-scale";
  private static String FIELDS_MINIMUM_SCALE = "minimum-scale";
  private static String FIELDS_MAXIMUM_SCALE = "maximum-scale";
  private static String FIELDS_USER_SCALABLE = "user-scalable";

  @Test
  public void testGetViewport() {
    assertEquals(EViewport.HEIGHT, EViewport.getViewport(FIELDS_HEIGHT).get());
    assertEquals(EViewport.WIDTH, EViewport.getViewport(FIELDS_WIDTH).get());
    assertEquals(EViewport.INITIAL_SCALE, EViewport.getViewport(FIELDS_INITIAL_SCALE).get());
    assertEquals(EViewport.MINIMUM_SCALE, EViewport.getViewport(FIELDS_MINIMUM_SCALE).get());
    assertEquals(EViewport.MAXIMUM_SCALE, EViewport.getViewport(FIELDS_MAXIMUM_SCALE).get());
    assertEquals(EViewport.USER_SCALABLE, EViewport.getViewport(FIELDS_USER_SCALABLE).get());
  }

  @Test
  public void testGetIdentifier() {
    assertEquals(FIELDS_HEIGHT, EViewport.HEIGHT.getIdentifier());
    assertEquals(FIELDS_WIDTH, EViewport.WIDTH.getIdentifier());
    assertEquals(FIELDS_INITIAL_SCALE, EViewport.INITIAL_SCALE.getIdentifier());
    assertEquals(FIELDS_MINIMUM_SCALE, EViewport.MINIMUM_SCALE.getIdentifier());
    assertEquals(FIELDS_MAXIMUM_SCALE, EViewport.MAXIMUM_SCALE.getIdentifier());
    assertEquals(FIELDS_USER_SCALABLE, EViewport.USER_SCALABLE.getIdentifier());
  }
}
