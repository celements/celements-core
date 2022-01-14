package com.celements.metatag;

import static org.junit.Assert.*;

import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.reflect.ReflectiveInstanceSupplier;
import com.celements.common.test.AbstractComponentTest;
import com.celements.convert.ConversionException;
import com.celements.convert.bean.BeanClassDefConverter;
import com.celements.convert.bean.XObjectBeanConverter;
import com.celements.metatag.enums.ENameStandard;
import com.celements.metatag.enums.twitter.ETwitterCardType;
import com.celements.store.id.IdVersion;
import com.celements.web.classes.CelementsClassDefinition;
import com.celements.web.classes.MetaTagClass;
import com.google.common.base.Suppliers;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class MetaTagTest extends AbstractComponentTest {

  private final Supplier<BeanClassDefConverter<BaseObject, MetaTag>> metaTagConverter = Suppliers
      .memoize(this::createMetaTagConverter);

  private BeanClassDefConverter<BaseObject, MetaTag> createMetaTagConverter() {
    @SuppressWarnings("unchecked")
    BeanClassDefConverter<BaseObject, MetaTag> converter = Utils.getComponent(
        BeanClassDefConverter.class, XObjectBeanConverter.NAME);
    converter.initialize(Utils.getComponent(CelementsClassDefinition.class,
        MetaTagClass.CLASS_DEF_HINT));
    converter.initialize(new ReflectiveInstanceSupplier<>(MetaTag.class));
    return converter;
  }

  private MetaTag tag;

  @Before
  public void setup_MetaTagTest() throws Exception {
    tag = new MetaTag();
  }

  @Test
  public void testGetSetOverridable() {
    assertFalse(tag.getOverridable());
    tag.setOverridable(true);
    assertTrue(tag.getOverridable());
    tag.setOverridable(false);
    assertFalse(tag.getOverridable());
  }

  @Test
  public void testGetSetKey() {
    assertFalse(tag.getKeyOpt().isPresent());
    tag.setKey("tagname");
    assertEquals("tagname", tag.getKeyOpt().get());
    tag.setKey("other");
    assertEquals("other", tag.getKeyOpt().get());
  }

  @Test
  public void testGetSetValue() {
    assertFalse(tag.getValueOpt().isPresent());
    tag.setValue("value1");
    assertEquals("value1", tag.getValueOpt().get());
    tag.setValue("value2");
    assertEquals("value2", tag.getValueOpt().get());
  }

  @Test
  public void testGetSetLang() {
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

  @Test
  public void test_bean() {
    DocumentReference docRef = new DocumentReference("wikiName", "space", "document");
    tag.setKey("description");
    tag.setLang("de");
    tag.setValue("the most fabulous thing ever");
    tag.setOverridable(false);
    BaseObject metaTagObj = new BaseObject();
    metaTagObj.setXClassReference(MetaTagClass.CLASS_REF);
    metaTagObj.setDocumentReference(docRef);
    Long objId = 2342423L;
    metaTagObj.setId(objId, IdVersion.CELEMENTS_3);
    Integer objNum = 2;
    metaTagObj.setNumber(objNum);
    metaTagObj.setStringValue(MetaTagClass.FIELD_KEY.getName(), tag.getKey());
    metaTagObj.setStringValue(MetaTagClass.FIELD_LANGUAGE.getName(), tag.getLang());
    metaTagObj.setStringValue(MetaTagClass.FIELD_VALUE.getName(), tag.getValue());
    metaTagObj.setIntValue(MetaTagClass.FIELD_OVERRIDABLE.getName(), tag.getOverridable() ? 1 : 0);
    try {
      MetaTag metaTagBean = metaTagConverter.get().apply(metaTagObj);
      assertEquals(tag, metaTagBean);
      assertEquals(docRef, metaTagBean.getDocumentReference());
      assertEquals(MetaTagClass.CLASS_REF, metaTagBean.getClassReference());
      assertEquals(objNum, metaTagBean.getNumber());
      assertEquals(objId, metaTagBean.getId());
    } catch (ConversionException exp) {
      fail();
    }
  }

}
