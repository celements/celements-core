package com.celements.javascript;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

public class JsFileEntryTest {

  private JsFileEntry jsFileEntry;

  @Before
  public void setUp() throws Exception {
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

  // @Test
  // public void test_bean() {
  // DocumentReference docRef = new DocumentReference("wikiName", "space", "document");
  // tag.setKey("description");
  // tag.setLang("de");
  // tag.setValue("the most fabulous thing ever");
  // tag.setOverridable(false);
  // BaseObject metaTagObj = new BaseObject();
  // metaTagObj.setXClassReference(metaTagClass.getClassReference());
  // metaTagObj.setDocumentReference(docRef);
  // metaTagObj.setId(2342423, IdVersion.CELEMENTS_3);
  // metaTagObj.setNumber(1);
  // metaTagObj.setStringValue(MetaTagClass.FIELD_KEY.getName(), tag.getKey());
  // metaTagObj.setStringValue(MetaTagClass.FIELD_LANGUAGE.getName(), tag.getLang());
  // metaTagObj.setStringValue(MetaTagClass.FIELD_VALUE.getName(), tag.getValue());
  // metaTagObj.setIntValue(MetaTagClass.FIELD_OVERRIDABLE.getName(), tag.getOverridable() ? 1 : 0);
  // try {
  // MetaTag metaTagBean = metaTagConverter.get().apply(metaTagObj);
  // assertEquals(tag, metaTagBean);
  // } catch (ConversionException exp) {
  // fail();
  // }
  // }

}
