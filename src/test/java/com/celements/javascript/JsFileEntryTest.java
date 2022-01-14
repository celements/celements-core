package com.celements.javascript;

import static org.junit.Assert.*;

import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.reflect.ReflectiveInstanceSupplier;
import com.celements.common.test.AbstractComponentTest;
import com.celements.convert.ConversionException;
import com.celements.convert.bean.BeanClassDefConverter;
import com.celements.convert.bean.XObjectBeanConverter;
import com.celements.store.id.IdVersion;
import com.celements.web.classes.CelementsClassDefinition;
import com.google.common.base.Suppliers;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class JsFileEntryTest extends AbstractComponentTest {

  private final Supplier<BeanClassDefConverter<BaseObject, JsFileEntry>> jsFileEntryConverter = Suppliers
      .memoize(this::jsFileEntryConverter);

  private JsFileEntry jsFileEntry;

  private BeanClassDefConverter<BaseObject, JsFileEntry> jsFileEntryConverter() {
    @SuppressWarnings("unchecked")
    BeanClassDefConverter<BaseObject, JsFileEntry> converter = Utils.getComponent(
        BeanClassDefConverter.class, XObjectBeanConverter.NAME);
    converter.initialize(Utils.getComponent(CelementsClassDefinition.class,
        JavaScriptExternalFilesClass.CLASS_DEF_HINT));
    converter.initialize(new ReflectiveInstanceSupplier<>(JsFileEntry.class));
    return converter;
  }

  @Before
  public void setUp_JsFileEntryTest() throws Exception {
    jsFileEntry = new JsFileEntry();
  }

  @Test
  public void testHashCode() {
    String fileUrl = ":space.doc:attachment.js";
    jsFileEntry.setFilepath(fileUrl);
    assertEquals(fileUrl.hashCode(), jsFileEntry.hashCode());
  }

  @Test
  public void testAddFilepath() {
    String fileUrl = ":space.doc:attachment.js";
    assertSame(jsFileEntry, jsFileEntry.addFilepath(fileUrl));
    assertEquals(fileUrl, jsFileEntry.getFilepath());
  }

  @Test
  public void testAddLoadMode_DEFER() {
    assertSame(jsFileEntry, jsFileEntry.addLoadMode(JsLoadMode.DEFER));
    assertEquals(JsLoadMode.DEFER, jsFileEntry.getLoadMode());
  }

  @Test
  public void testAddLoadMode_ASYNC() {
    assertSame(jsFileEntry, jsFileEntry.addLoadMode(JsLoadMode.ASYNC));
    assertEquals(JsLoadMode.ASYNC, jsFileEntry.getLoadMode());
  }

  @Test
  public void testSetFilepath() {
    String fileUrl = ":space.doc:attachment.js";
    jsFileEntry.setFilepath(fileUrl);
    assertEquals(fileUrl, jsFileEntry.getFilepath());
  }

  @Test
  public void testGetFilepath_null() {
    jsFileEntry.setFilepath(null);
    assertNotNull(jsFileEntry.getFilepath());
    assertEquals("", jsFileEntry.getFilepath());
  }

  @Test
  public void testSetLoadMode() {
    jsFileEntry.setLoadMode(JsLoadMode.ASYNC);
    assertEquals(JsLoadMode.ASYNC, jsFileEntry.getLoadMode());
  }

  @Test
  public void testGetLoadMode_null() {
    jsFileEntry.setLoadMode(null);
    assertNotNull(jsFileEntry.getLoadMode());
    assertEquals(JsLoadMode.SYNC, jsFileEntry.getLoadMode());
  }

  @Test
  public void testSetNumber() {
    Integer num = 123;
    jsFileEntry.setNumber(num);
    assertEquals("Number field is needed for bean", num, jsFileEntry.getNumber());
  }

  @Test
  public void testSetClassReference() {
    ClassReference classRef = new ClassReference("space", "classname");
    jsFileEntry.setClassReference(classRef);
    assertEquals("ClassReference field is needed for bean", classRef,
        jsFileEntry.getClassReference());
  }

  @Test
  public void testSetDocumentReference() {
    DocumentReference docRef = new DocumentReference("wikiName", "space", "classname");
    jsFileEntry.setDocumentReference(docRef);
    assertEquals("ClassReference field is needed for bean", docRef,
        jsFileEntry.getDocumentReference());
  }

  @Test
  public void testSetId() {
    Long num = 123L;
    jsFileEntry.setId(num);
    assertEquals("Id field is needed for bean", num, jsFileEntry.getId());
  }

  @Test
  public void testIsValid_valid() {
    jsFileEntry.setFilepath(":space.doc:attachment.js");
    assertTrue(jsFileEntry.isValid());
  }

  @Test
  public void testIsValid_null() {
    jsFileEntry.setFilepath(null);
    assertFalse(jsFileEntry.isValid());
  }

  @Test
  public void testIsValid_empty() {
    jsFileEntry.setFilepath("");
    assertFalse(jsFileEntry.isValid());
  }

  @Test
  public void testIsValid_nothingSet() {
    assertFalse(jsFileEntry.isValid());
  }

  @Test
  public void testEqualsObject_equal() {
    String fileUrl = ":space.doc:attachment.js";
    jsFileEntry.setFilepath(fileUrl);
    assertEquals(jsFileEntry, new JsFileEntry().addFilepath(fileUrl));
  }

  @Test
  public void testEqualsObject_NotEqual() {
    jsFileEntry.setFilepath(":space.doc:attachment.js");
    assertNotEquals(jsFileEntry, new JsFileEntry().addFilepath(":space.doc:attachment2.js"));
  }

  @Test
  public void testToString() {
    String fileUrl = ":space.doc:attachment.js";
    jsFileEntry.setFilepath(fileUrl);
    JsLoadMode loadMode = JsLoadMode.ASYNC;
    jsFileEntry.setLoadMode(loadMode);
    DocumentReference docRef = new DocumentReference("wikiName", "space", "classname");
    jsFileEntry.setDocumentReference(docRef);
    assertEquals("jsFileUrl [" + fileUrl + "], loadMode [" + loadMode + "] from docRef [" + docRef
        + "]", jsFileEntry.toString());
  }

  @Test
  public void test_JsExtFileObj_bean() {
    DocumentReference docRef = new DocumentReference("wikiName", "space", "document");
    jsFileEntry.addFilepath(":space.doc:attachment.js")
        .addLoadMode(JsLoadMode.ASYNC);
    BaseObject jsExtFileObj = new BaseObject();
    jsExtFileObj.setXClassReference(getJavaScriptExternalFilesClassRef());
    jsExtFileObj.setDocumentReference(docRef);
    jsExtFileObj.setId(2342423, IdVersion.CELEMENTS_3);
    jsExtFileObj.setNumber(1);
    jsExtFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_FILEPATH.getName(),
        jsFileEntry.getFilepath());
    jsExtFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_LOAD_MODE.getName(),
        jsFileEntry.getLoadMode().toString());
    try {
      JsFileEntry jsFileEntryBean = jsFileEntryConverter.get().apply(jsExtFileObj);
      assertEquals(jsFileEntry, jsFileEntryBean);
    } catch (ConversionException exp) {
      fail();
    }
  }

  private ClassReference getJavaScriptExternalFilesClassRef() {
    return Utils
        .getComponent(CelementsClassDefinition.class, JavaScriptExternalFilesClass.CLASS_DEF_HINT)
        .getClassReference();
  }
}
