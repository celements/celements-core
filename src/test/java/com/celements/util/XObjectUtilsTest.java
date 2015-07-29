package com.celements.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class XObjectUtilsTest extends AbstractBridgedComponentTestCase {

  private XWikiDocument doc;

  private DocumentReference classRef;
  private DocumentReference classRef2;

  @Before
  public void setUp_XObjectUtilsTest() {
    doc = new XWikiDocument(new DocumentReference("db", "space", "doc"));
    classRef = new DocumentReference("db", "class", "any");
    classRef2 = new DocumentReference("db", "class", "other");
  }

  @Test
  public void test_getXObjects_nullDoc() {
    try {
      XObjectUtils.getXObjects(null, null);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_getXObjects_nullClassRef() {
    List<BaseObject> ret = XObjectUtils.getXObjects(doc, null);
    assertEquals(ret.size(), 0);
  }

  @Test
  public void test_getXObjects_emptyDoc() {
    List<BaseObject> ret = XObjectUtils.getXObjects(doc, classRef);
    assertEquals(ret.size(), 0);
  }

  @Test
  public void test_getXObjects_withObj() {
    BaseObject obj = addObj(classRef, null, null);
    List<BaseObject> ret = XObjectUtils.getXObjects(doc, classRef);
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(0));
  }

  @Test
  public void test_getXObjects_mutlipleObj() {
    BaseObject obj1 = addObj(classRef, null, null);
    addObj(classRef2, null, null);
    BaseObject obj2 = addObj(classRef, null, null);
    List<BaseObject> ret = XObjectUtils.getXObjects(doc, classRef);
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj2, ret.get(1));
  }

  @Test
  public void test_getXObjects_key() {
    String key = "field";
    String val = "val";
    addObj(classRef, null, null);
    addObj(classRef2, key, val);
    BaseObject obj1 = addObj(classRef, key, val);
    addObj(classRef, key, null);
    BaseObject obj2 = addObj(classRef, key, val);
    assertEquals(0, XObjectUtils.getXObjects(doc, classRef, key, null).size());
    assertEquals(0, XObjectUtils.getXObjects(doc, classRef, key, "").size());
    List<BaseObject> ret = XObjectUtils.getXObjects(doc, classRef, key, val);
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj2, ret.get(1));
  }

  @Test
  public void test_getXObjects_key_values() {
    String key = "field";
    List<String> vals = Arrays.asList("val1", "val2");
    addObj(classRef, null, null);
    addObj(classRef2, key, vals.get(0));
    BaseObject obj1 = addObj(classRef, key, vals.get(0));
    addObj(classRef, key, null);
    BaseObject obj2 = addObj(classRef, key, vals.get(1));
    List<BaseObject> ret = XObjectUtils.getXObjects(doc, classRef, key, vals);
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj2, ret.get(1));
  }

  @Test
  public void test_removeXObjects() {
    addObj(classRef, null, null);
    assertTrue(XObjectUtils.removeXObjects(doc, classRef));
    assertEquals(0, XObjectUtils.getXObjects(doc, classRef).size());
  }

  @Test
  public void test_removeXObjects_noChange() {
    addObj(classRef2, null, null);
    assertFalse(XObjectUtils.removeXObjects(doc, classRef));
    assertEquals(0, XObjectUtils.getXObjects(doc, classRef).size());
    assertEquals(1, XObjectUtils.getXObjects(doc, classRef2).size());
  }

  @Test
  public void test_removeXObjects_mutlipleObj() {
    addObj(classRef, null, null);
    addObj(classRef2, null, null);
    addObj(classRef, null, null);
    assertTrue(XObjectUtils.removeXObjects(doc, classRef));
    assertEquals(0, XObjectUtils.getXObjects(doc, classRef).size());
    assertEquals(1, XObjectUtils.getXObjects(doc, classRef2).size());
  }

  private BaseObject addObj(DocumentReference classRef, String key, String value) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    if (key != null) {
      obj.setStringValue(key, value);
    }
    doc.addXObject(obj);
    return obj;
  }

}
