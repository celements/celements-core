package com.celements.metatag.enums;

import static org.junit.Assert.*;

import org.junit.Test;

import com.celements.metatag.enums.opengraph.EOpenGraph;

public class EHttpEquivTest {

  private static String FIELDS_CONTENT_SECURITY_POLICY = "Content-Security-Policy";
  private static String FIELDS_DEFAULT_STYLE = "default-style";
  private static String FIELDS_REFRESH = "refresh";

  @Test
  public void testAttribs() {
    assertEquals("property", EOpenGraph.ATTRIB_NAME);
  }

  @Test
  public void testGetHttpEquiv() {
    assertEquals(EHttpEquiv.CONTENT_SECURITY_POLICY, EHttpEquiv.getHttpEquiv(
        FIELDS_CONTENT_SECURITY_POLICY));
    assertEquals(EHttpEquiv.DEFAULT_STYLE, EHttpEquiv.getHttpEquiv(FIELDS_DEFAULT_STYLE));
    assertEquals(EHttpEquiv.REFRESH, EHttpEquiv.getHttpEquiv(FIELDS_REFRESH));
  }

  @Test
  public void testGetIdentifier() {
    assertEquals(FIELDS_CONTENT_SECURITY_POLICY,
        EHttpEquiv.CONTENT_SECURITY_POLICY.getIdentifier());
    assertEquals(FIELDS_DEFAULT_STYLE, EHttpEquiv.DEFAULT_STYLE.getIdentifier());
    assertEquals(FIELDS_REFRESH, EHttpEquiv.REFRESH.getIdentifier());
  }
}
