package com.celements.javascript;

import static org.junit.Assert.*;

import java.util.function.Supplier;

import javax.validation.constraints.NotNull;

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
  public void test_addFilepath() {
    String fileUrl = ":space.doc:attachment.js";
    assertSame(jsFileEntry, jsFileEntry.addFilepath(fileUrl));
    assertEquals(fileUrl, jsFileEntry.getFilepath());
  }

  @Test
  public void test_addLoadMode_DEFER() {
    assertSame(jsFileEntry, jsFileEntry.addLoadMode(JsLoadMode.DEFER));
    assertEquals(JsLoadMode.DEFER, jsFileEntry.getLoadMode());
  }

  @Test
  public void test_addLoadMode_ASYNC() {
    assertSame(jsFileEntry, jsFileEntry.addLoadMode(JsLoadMode.ASYNC));
    assertEquals(JsLoadMode.ASYNC, jsFileEntry.getLoadMode());
  }

  @Test
  public void test_setFilepath() {
    String fileUrl = ":space.doc:attachment.js";
    jsFileEntry.setFilepath(fileUrl);
    assertEquals(fileUrl, jsFileEntry.getFilepath());
  }

  @Test
  public void test_getFilepath_null() {
    jsFileEntry.setFilepath(null);
    assertNotNull(jsFileEntry.getFilepath());
    assertEquals("", jsFileEntry.getFilepath());
  }

  @Test
  public void test_setLoadMode() {
    jsFileEntry.setLoadMode(JsLoadMode.ASYNC);
    assertEquals(JsLoadMode.ASYNC, jsFileEntry.getLoadMode());
  }

  @Test
  public void test_getLoadMode_null() {
    jsFileEntry.setLoadMode(null);
    assertNotNull(jsFileEntry.getLoadMode());
    assertEquals(JsLoadMode.SYNC, jsFileEntry.getLoadMode());
  }

  @Test
  public void test_isValid_valid() {
    jsFileEntry.setFilepath(":space.doc:attachment.js");
    assertTrue(jsFileEntry.isValid());
  }

  @Test
  public void test_isModule_emptyPath() {
    jsFileEntry.setFilepath("");
    assertFalse(jsFileEntry.isModule());
  }

  @Test
  public void test_isModule_no() {
    jsFileEntry.setFilepath("/space/doc/attachment.js");
    assertFalse(jsFileEntry.isModule());
  }

  @Test
  public void test_isModule_no_search() {
    jsFileEntry.setFilepath("/space/doc/attachment.js?asdf");
    assertFalse(jsFileEntry.isModule());
  }

  @Test
  public void test_isModule_yes() {
    jsFileEntry.setFilepath("/space/doc/attachment.mjs");
    assertTrue(jsFileEntry.isModule());
  }

  @Test
  public void test_isModule_yes_search() {
    jsFileEntry.setFilepath("/space/doc/attachment.mjs?version=asdf");
    assertTrue(jsFileEntry.isModule());
  }

  @Test
  public void test_isValid_null() {
    jsFileEntry.setFilepath(null);
    assertFalse(jsFileEntry.isValid());
  }

  @Test
  public void test_isValid_empty() {
    jsFileEntry.setFilepath("");
    assertFalse(jsFileEntry.isValid());
  }

  @Test
  public void test_isValid_nothingSet() {
    assertFalse(jsFileEntry.isValid());
  }

  @Test
  public void test_equals_equal() {
    String fileUrl = ":space.doc:attachment.js";
    jsFileEntry.setFilepath(fileUrl);
    JsFileEntry jsFileEntry2 = new JsFileEntry().addFilepath(fileUrl);
    assertEquals(jsFileEntry, jsFileEntry2);
    assertEquals(jsFileEntry.hashCode(), jsFileEntry2.hashCode());
  }

  @Test
  public void test_equals_NotEqual() {
    jsFileEntry.setFilepath(":space.doc:attachment.js");
    @NotNull
    JsFileEntry jsFileEntry2 = new JsFileEntry().addFilepath(":space.doc:attachment2.js");
    assertNotEquals(jsFileEntry, jsFileEntry2);
    assertNotEquals(jsFileEntry.hashCode(), jsFileEntry2.hashCode());
  }

  @Test
  public void test_toString() {
    String fileUrl = ":space.doc:attachment.js";
    jsFileEntry.setFilepath(fileUrl);
    JsLoadMode loadMode = JsLoadMode.ASYNC;
    jsFileEntry.setLoadMode(loadMode);
    DocumentReference docRef = new DocumentReference("wikiName", "space", "classname");
    jsFileEntry.setDocumentReference(docRef);
    assertTrue(jsFileEntry.toString().startsWith("JsFileEntry [jsFileUrl=" + fileUrl
        + ", loadMode=" + loadMode + ", "));
  }

  @Test
  public void test_JsExtFileObj_bean() {
    DocumentReference docRef = new DocumentReference("wikiName", "space", "document");
    jsFileEntry.addFilepath("/space/doc/attachment.mjs")
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
      assertEquals(jsFileEntry.getFilepath(), jsFileEntryBean.getFilepath());
      assertEquals(jsFileEntry.getLoadMode(), jsFileEntryBean.getLoadMode());
      assertEquals(docRef, jsFileEntryBean.getDocumentReference());
      assertEquals(new ClassReference(jsExtFileObj.getXClassReference()),
          jsFileEntryBean.getClassReference());
      assertEquals(jsExtFileObj.getNumber(), jsFileEntryBean.getNumber().intValue());
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
