package com.celements.metatag;

import static org.junit.Assert.*;

import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.metatag.enums.ENameStandard;
import com.celements.metatag.enums.twitter.ETwitterCardType;

public class MetaTagTest extends AbstractComponentTest {

  @Test
  public void testGetSetOverridable() {
    MetaTag tag = new MetaTag();
    assertFalse(tag.getOverridable());
    tag.setOverridable(true);
    assertTrue(tag.getOverridable());
    tag.setOverridable(false);
    assertFalse(tag.getOverridable());
  }

  @Test
  public void testGetSetKey() {
    MetaTag tag = new MetaTag();
    assertNull(tag.getKey());
    tag.setKey("tagname");
    assertEquals("tagname", tag.getKey());
    tag.setKey("other");
    assertEquals("other", tag.getKey());
  }

  @Test
  public void testGetSetValue() {
    MetaTag tag = new MetaTag();
    assertNull(tag.getValue());
    tag.setValue("value1");
    assertEquals("value1", tag.getValue());
    tag.setValue("value2");
    assertEquals("value2", tag.getValue());
  }

  @Test
  public void testGetSetLang() {
    MetaTag tag = new MetaTag();
    assertNull(tag.getLang());
    tag.setLang("fr");
    assertEquals("fr", tag.getLang());
    tag.setLang("de");
    assertEquals("de", tag.getLang());
  }

  @Test
  public void testDisplay_oneAttribute() {
    MetaTag tag = new MetaTag(ETwitterCardType.SUMMARY);
    assertEquals("<meta name=\"twitter:card\" content=\"summary\" />", tag.display());
  }

  @Test
  public void testDisplay_multipleAttributes() {
    String keywords = "test,junit,keyword";
    MetaTag tag = new MetaTag(ENameStandard.KEYWORDS, keywords);
    assertEquals("<meta name=\"keywords\" property=\"keywords\" content=\"" + keywords + "\" />",
        tag.display());
  }

  @Test
  public void testEqualsObject_null() {
    assertFalse(new MetaTag().equals(null));
  }

  @Test
  public void testEqualsObject_empty() {
    assertTrue(new MetaTag().equals(new MetaTag()));
  }

  @Test
  public void testEqualsObject_langDifference() {
    MetaTag m0 = new MetaTag();
    MetaTag m1 = new MetaTag();
    m1.setLang("de");
    MetaTag m2 = new MetaTag();
    m2.setLang("en");
    assertFalse(m0.equals(m1));
    assertTrue(m1.equals(m1));
    assertFalse(m1.equals(m2));
  }

  @Test
  public void testEqualsObject_keyDifference() {
    MetaTag m0 = new MetaTag();
    MetaTag m1 = new MetaTag();
    m1.setValue("content tag1");
    MetaTag m2 = new MetaTag();
    m2.setValue("content tag2");
    assertFalse(m0.equals(m1));
    assertTrue(m1.equals(m1));
    assertFalse(m1.equals(m2));
  }

  @Test
  public void testEqualsObject_valueDifference() {
    MetaTag m0 = new MetaTag();
    MetaTag m1 = new MetaTag();
    m1.setKey("description");
    MetaTag m2 = new MetaTag();
    m2.setKey("keywords");
    assertFalse(m0.equals(m1));
    assertTrue(m1.equals(m1));
    assertFalse(m1.equals(m2));
  }

  @Test
  public void testEqualsObject_overrideDifference() {
    MetaTag m0 = new MetaTag();
    MetaTag m1 = new MetaTag();
    m1.setOverridable(true);
    MetaTag m2 = new MetaTag();
    m2.setOverridable(false);
    assertTrue(m0.equals(m1));
    assertTrue(m1.equals(m1));
    assertTrue(m1.equals(m2));
  }

}
