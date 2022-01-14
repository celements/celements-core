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
    assertFalse(tag.getKeyOpt().isPresent());
    tag.setKey("tagname");
    assertEquals("tagname", tag.getKeyOpt().get());
    tag.setKey("other");
    assertEquals("other", tag.getKeyOpt().get());
  }

  @Test
  public void testGetSetValue() {
    MetaTag tag = new MetaTag();
    assertFalse(tag.getValueOpt().isPresent());
    tag.setValue("value1");
    assertEquals("value1", tag.getValueOpt().get());
    tag.setValue("value2");
    assertEquals("value2", tag.getValueOpt().get());
  }

  @Test
  public void testGetSetLang() {
    MetaTag tag = new MetaTag();
    assertFalse(tag.getLangOpt().isPresent());
    tag.setLang("fr");
    assertEquals("fr", tag.getLangOpt().get());
    tag.setLang("de");
    assertEquals("de", tag.getLangOpt().get());
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
    assertNotEquals(null, new MetaTag());
  }

  @Test
  public void testEqualsObject_empty() {
    assertEquals(new MetaTag(), new MetaTag());
  }

  @Test
  public void testEqualsObject_langDifference() {
    MetaTag m0 = new MetaTag();
    MetaTag m1 = new MetaTag();
    m1.setLang("de");
    MetaTag m2 = new MetaTag();
    m2.setLang("en");
    assertNotEquals(m0, m1);
    assertEquals(m1, m1);
    assertNotEquals(m1, m2);
  }

  @Test
  public void testEqualsObject_keyDifference() {
    MetaTag m0 = new MetaTag();
    MetaTag m1 = new MetaTag();
    m1.setValue("content tag1");
    MetaTag m2 = new MetaTag();
    m2.setValue("content tag2");
    assertNotEquals(m0, m1);
    assertEquals(m1, m1);
    assertNotEquals(m1, m2);
  }

  @Test
  public void testEqualsObject_valueDifference() {
    MetaTag m0 = new MetaTag();
    MetaTag m1 = new MetaTag();
    m1.setKey("description");
    MetaTag m2 = new MetaTag();
    m2.setKey("keywords");
    assertNotEquals(m0, m1);
    assertEquals(m1, m1);
    assertNotEquals(m1, m2);
  }

  @Test
  public void testEqualsObject_overrideDifference() {
    MetaTag m0 = new MetaTag();
    MetaTag m1 = new MetaTag();
    m1.setOverridable(true);
    MetaTag m2 = new MetaTag();
    m2.setOverridable(false);
    assertEquals(m0, m1);
    assertEquals(m1, m1);
    assertEquals(m1, m2);
  }

}
