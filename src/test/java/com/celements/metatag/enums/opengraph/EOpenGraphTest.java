package com.celements.metatag.enums.opengraph;

import static org.junit.Assert.*;

import org.junit.Test;

public class EOpenGraphTest {

  private static String FIELDS_OPENGRAPH_TITLE = "og:title";
  private static String FIELDS_OPENGRAPH_TYPE = "og:type";
  private static String FIELDS_OPENGRAPH_IMAGE = "og:image";
  private static String FIELDS_OPENGRAPH_URL = "og:url";
  private static String FIELDS_OPENGRAPH_OPTIONAL_AUDIO = "og:audio";
  private static String FIELDS_OPENGRAPH_OPTIONAL_DESCRIPTION = "og:description";
  private static String FIELDS_OPENGRAPH_OPTIONAL_DETERMINER = "og:determiner";
  private static String FIELDS_OPENGRAPH_OPTIONAL_LOCALE = "og:locale ";
  private static String FIELDS_OPENGRAPH_OPTIONAL_LOCALE_ALTERNATE = "og:locale:alternate";
  private static String FIELDS_OPENGRAPH_OPTIONAL_SITENAME = "og:site_name";
  private static String FIELDS_OPENGRAPH_OPTIONAL_VIDEO = "og:video";
  private static String FIELDS_OPENGRAPH_OPTIONAL_IMAGE_WIDTH = "og:image:width";
  private static String FIELDS_OPENGRAPH_OPTIONAL_IMAGE_HEIGHT = "og:image:height";

  @Test
  public void testAttribs() {
    assertEquals("property", EOpenGraph.ATTRIB_NAME);
  }

  @Test
  public void testGetOpenGraph() {
    assertEquals(EOpenGraph.OPENGRAPH_TITLE, EOpenGraph.getOpenGraph(FIELDS_OPENGRAPH_TITLE));
    assertEquals(EOpenGraph.OPENGRAPH_TYPE, EOpenGraph.getOpenGraph(FIELDS_OPENGRAPH_TYPE));
    assertEquals(EOpenGraph.OPENGRAPH_IMAGE, EOpenGraph.getOpenGraph(FIELDS_OPENGRAPH_IMAGE));
    assertEquals(EOpenGraph.OPENGRAPH_URL, EOpenGraph.getOpenGraph(FIELDS_OPENGRAPH_URL));
    assertEquals(EOpenGraph.OPENGRAPH_OPTIONAL_AUDIO, EOpenGraph.getOpenGraph(
        FIELDS_OPENGRAPH_OPTIONAL_AUDIO));
    assertEquals(EOpenGraph.OPENGRAPH_OPTIONAL_DESCRIPTION, EOpenGraph.getOpenGraph(
        FIELDS_OPENGRAPH_OPTIONAL_DESCRIPTION));
    assertEquals(EOpenGraph.OPENGRAPH_OPTIONAL_DETERMINER, EOpenGraph.getOpenGraph(
        FIELDS_OPENGRAPH_OPTIONAL_DETERMINER));
    assertEquals(EOpenGraph.OPENGRAPH_OPTIONAL_LOCALE, EOpenGraph.getOpenGraph(
        FIELDS_OPENGRAPH_OPTIONAL_LOCALE));
    assertEquals(EOpenGraph.OPENGRAPH_OPTIONAL_LOCALE_ALTERNATE, EOpenGraph.getOpenGraph(
        FIELDS_OPENGRAPH_OPTIONAL_LOCALE_ALTERNATE));
    assertEquals(EOpenGraph.OPENGRAPH_OPTIONAL_SITENAME, EOpenGraph.getOpenGraph(
        FIELDS_OPENGRAPH_OPTIONAL_SITENAME));
    assertEquals(EOpenGraph.OPENGRAPH_OPTIONAL_VIDEO, EOpenGraph.getOpenGraph(
        FIELDS_OPENGRAPH_OPTIONAL_VIDEO));
    assertEquals(EOpenGraph.OPENGRAPH_OPTIONAL_IMAGE_WIDTH, EOpenGraph.getOpenGraph(
        FIELDS_OPENGRAPH_OPTIONAL_IMAGE_WIDTH));
    assertEquals(EOpenGraph.OPENGRAPH_OPTIONAL_IMAGE_HEIGHT, EOpenGraph.getOpenGraph(
        FIELDS_OPENGRAPH_OPTIONAL_IMAGE_HEIGHT));
  }

  @Test
  public void testGetIdentifier() {
    assertEquals(FIELDS_OPENGRAPH_TITLE, EOpenGraph.OPENGRAPH_TITLE.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_TYPE, EOpenGraph.OPENGRAPH_TYPE.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_IMAGE, EOpenGraph.OPENGRAPH_IMAGE.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_URL, EOpenGraph.OPENGRAPH_URL.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_OPTIONAL_AUDIO,
        EOpenGraph.OPENGRAPH_OPTIONAL_AUDIO.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_OPTIONAL_DESCRIPTION,
        EOpenGraph.OPENGRAPH_OPTIONAL_DESCRIPTION.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_OPTIONAL_DETERMINER,
        EOpenGraph.OPENGRAPH_OPTIONAL_DETERMINER.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_OPTIONAL_LOCALE,
        EOpenGraph.OPENGRAPH_OPTIONAL_LOCALE.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_OPTIONAL_LOCALE_ALTERNATE,
        EOpenGraph.OPENGRAPH_OPTIONAL_LOCALE_ALTERNATE.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_OPTIONAL_SITENAME,
        EOpenGraph.OPENGRAPH_OPTIONAL_SITENAME.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_OPTIONAL_VIDEO,
        EOpenGraph.OPENGRAPH_OPTIONAL_VIDEO.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_OPTIONAL_IMAGE_WIDTH,
        EOpenGraph.OPENGRAPH_OPTIONAL_IMAGE_WIDTH.getIdentifier());
    assertEquals(FIELDS_OPENGRAPH_OPTIONAL_IMAGE_HEIGHT,
        EOpenGraph.OPENGRAPH_OPTIONAL_IMAGE_HEIGHT.getIdentifier());
  }
}
