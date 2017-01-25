package com.celements.metatag.enums.twitter;

import static org.junit.Assert.*;

import org.junit.Test;

import com.celements.metatag.enums.opengraph.EOpenGraph;

public class ETwitterTest {

  private static String FIELDS_TWITTER_CARD = "twitter:card";
  private static String FIELDS_TWITTER_SITE = "twitter:site";
  private static String FIELDS_TWITTER_SITE_ID = "twitter:site:id";
  private static String FIELDS_TWITTER_CREATOR = "twitter:creator";
  private static String FIELDS_TWITTER_CREATOR_ID = "twitter:creator:id";
  private static String FIELDS_TWITTER_DESCRIPTION = "twitter:description";
  private static String FIELDS_TWITTER_TITLE = "twitter:title";
  private static String FIELDS_TWITTER_IMAGE = "twitter:image";
  private static String FIELDS_TWITTER_IMAGE_ALT = "twitter:image:alt";

  @Test
  public void testAttribs() {
    assertEquals("property", EOpenGraph.ATTRIB_NAME);
  }

  @Test
  public void testGetTwitter() {
    assertEquals(ETwitter.TWITTER_CARD, ETwitter.getTwitter(FIELDS_TWITTER_CARD));
    assertEquals(ETwitter.TWITTER_SITE, ETwitter.getTwitter(FIELDS_TWITTER_SITE));
    assertEquals(ETwitter.TWITTER_SITE_ID, ETwitter.getTwitter(FIELDS_TWITTER_SITE_ID));
    assertEquals(ETwitter.TWITTER_CREATOR, ETwitter.getTwitter(FIELDS_TWITTER_CREATOR));
    assertEquals(ETwitter.TWITTER_CREATOR_ID, ETwitter.getTwitter(FIELDS_TWITTER_CREATOR_ID));
    assertEquals(ETwitter.TWITTER_DESCRIPTION, ETwitter.getTwitter(FIELDS_TWITTER_DESCRIPTION));
    assertEquals(ETwitter.TWITTER_TITLE, ETwitter.getTwitter(FIELDS_TWITTER_TITLE));
    assertEquals(ETwitter.TWITTER_IMAGE, ETwitter.getTwitter(FIELDS_TWITTER_IMAGE));
    assertEquals(ETwitter.TWITTER_IMAGE_ALT, ETwitter.getTwitter(FIELDS_TWITTER_IMAGE_ALT));
  }

  @Test
  public void testGetIdentifier() {
    assertEquals(FIELDS_TWITTER_CARD, ETwitter.TWITTER_CARD.getIdentifier());
    assertEquals(FIELDS_TWITTER_SITE, ETwitter.TWITTER_SITE.getIdentifier());
    assertEquals(FIELDS_TWITTER_SITE_ID, ETwitter.TWITTER_SITE_ID.getIdentifier());
    assertEquals(FIELDS_TWITTER_CREATOR, ETwitter.TWITTER_CREATOR.getIdentifier());
    assertEquals(FIELDS_TWITTER_CREATOR_ID, ETwitter.TWITTER_CREATOR_ID.getIdentifier());
    assertEquals(FIELDS_TWITTER_DESCRIPTION, ETwitter.TWITTER_DESCRIPTION.getIdentifier());
    assertEquals(FIELDS_TWITTER_TITLE, ETwitter.TWITTER_TITLE.getIdentifier());
    assertEquals(FIELDS_TWITTER_IMAGE, ETwitter.TWITTER_IMAGE.getIdentifier());
    assertEquals(FIELDS_TWITTER_IMAGE_ALT, ETwitter.TWITTER_IMAGE_ALT.getIdentifier());
  }
}
