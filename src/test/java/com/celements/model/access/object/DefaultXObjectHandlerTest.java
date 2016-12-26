package com.celements.model.access.object;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.TestClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.util.References;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class DefaultXObjectHandlerTest extends AbstractComponentTest {

  private XWikiDocument doc;
  private DocumentReference classRef;
  private DocumentReference classRef2;

  @Before
  public void prepareTest() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    doc = new XWikiDocument(new DocumentReference(wikiRef.getName(), "space", "doc"));
    classRef = Utils.getComponent(ClassDefinition.class, TestClassDefinition.NAME).getClassRef(
        wikiRef);
    classRef2 = new DocumentReference(wikiRef.getName(), "class", "other");
  }

  private XObjectHandler getXObjHandler() {
    return Utils.getComponent(XObjectHandler.class).onDoc(doc);
  }

  @Test
  public void test_onDoc_null() throws Exception {
    try {
      getXObjHandler().onDoc(null);
      fail("expecting NullPointerException");
    } catch (NullPointerException exc) {
      // expected
    }
  }

  @Test
  public void test_onDoc_isTranslation() throws Exception {
    try {
      doc.setTranslation(1);
      getXObjHandler();
      fail("expecting IllegalStateException");
    } catch (IllegalStateException exc) {
      // expected
    }
  }

  @Test
  public void test_filter_classRef_null() throws Exception {
    try {
      getXObjHandler().filter(null);
      fail("expecting NullPointerException");
    } catch (NullPointerException exc) {
      // expected
    }
  }

  @Test
  public void test_filter_field_null() throws Exception {
    try {
      getXObjHandler().filter((ClassField<String>) null, (String) null);
      fail("expecting NullPointerException");
    } catch (NullPointerException exc) {
      // expected
    }
  }

  @Test
  public void test_filter_values_null() throws Exception {
    try {
      getXObjHandler().filter(TestClassDefinition.FIELD_MY_STRING, (List<String>) null);
      fail("expecting NullPointerException");
    } catch (NullPointerException exc) {
      // expected
    }
  }

  @Test
  public void test_filter_values_empty() throws Exception {
    try {
      getXObjHandler().filter(TestClassDefinition.FIELD_MY_STRING, Collections.<String>emptyList());
      fail("expecting IllegalArgumentException");
    } catch (IllegalArgumentException exc) {
      // expected
    }
  }

  @Test
  public void test_filter_key_null() throws Exception {
    try {
      getXObjHandler().filter(classRef, null, null);
      fail("expecting NullPointerException");
    } catch (NullPointerException exc) {
      // expected
    }
  }

  @Test
  public void test_filter_key_empty() throws Exception {
    try {
      getXObjHandler().filter(classRef, "", null);
      fail("expecting IllegalArgumentException");
    } catch (IllegalArgumentException exc) {
      // expected
    }
  }

  @Test
  public void test_filter_otherWiki() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    // changing the wiki ref should not affect the result since it is normalised to the docs wiki
    classRef = References.cloneRef(classRef, DocumentReference.class);
    classRef.setWikiReference(new WikiReference("otherWiki"));
    assertObjs(getXObjHandler().filter(classRef), obj);
  }

  @Test
  public void test_fetch_emptyDoc() throws Exception {
    List<BaseObject> ret = getXObjHandler().fetchList();
    assertEquals(0, ret.size());
  }

  @Test
  public void test_fetch_oneObj() throws Exception {
    ClassField<String> field = TestClassDefinition.FIELD_MY_STRING;
    String val = "val";
    BaseObject obj = addObj(classRef, field.getName(), val);
    assertObjs(getXObjHandler(), obj);
    assertObjs(getXObjHandler().filter(classRef), obj);
    assertObjs(getXObjHandler().filter(field, val), obj);
    assertObjs(getXObjHandler().filter(field, Arrays.asList("", val)), obj);
    assertObjs(getXObjHandler().filter(field, (String) null));
  }

  @Test
  public void test_fetch_mutlipleObj() throws Exception {
    ClassField<String> field = TestClassDefinition.FIELD_MY_STRING;
    List<String> vals = Arrays.asList("val1", "val2");
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, field.getName(), vals.get(0));
    BaseObject obj3 = addObj(classRef, field.getName(), vals.get(0));
    BaseObject obj4 = addObj(classRef, "other", null);
    BaseObject obj5 = addObj(classRef, field.getName(), null);
    BaseObject obj6 = addObj(classRef, field.getName(), vals.get(1));
    assertObjs(getXObjHandler(), obj1, obj3, obj4, obj5, obj6, obj2);
    assertObjs(getXObjHandler().filter(classRef), obj1, obj3, obj4, obj5, obj6);
    assertObjs(getXObjHandler().filter(field, (String) null), obj1, obj4, obj5);
    assertObjs(getXObjHandler().filter(field, vals), obj3, obj6);
    assertObjs(getXObjHandler().filter(field, vals.get(0)), obj3);
    assertObjs(getXObjHandler().filter(field, vals.get(1)), obj6);
    assertObjs(getXObjHandler().filter(field, "other"));
    assertObjs(getXObjHandler().filter(classRef2), obj2);
  }

  @Test
  public void test_fetchFirst() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    addObj(classRef2, null, null);
    addObj(classRef, null, null);
    Optional<BaseObject> ret = getXObjHandler().fetchFirst();
    assertTrue(ret.isPresent());
    assertSame(obj1, ret.get());
  }

  @Test
  public void test_fetchFirst_none() throws Exception {
    Optional<BaseObject> ret = getXObjHandler().fetchFirst();
    assertFalse(ret.isPresent());
  }

  @Test
  public void test_fetchFirst_noDoc() throws Exception {
    doc = null;
    try {
      getXObjHandler().fetchFirst();
      fail("expecting NullPointerException");
    } catch (NullPointerException exc) {
      // expected
    }
  }

  @Test
  public void test_fetchNumber() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    addObj(classRef2, null, null);
    addObj(classRef, null, null);
    Optional<BaseObject> ret = getXObjHandler().fetchNumber(obj1.getNumber());
    assertTrue(ret.isPresent());
    assertSame(obj1, ret.get());
  }

  @Test
  public void test_fetchNumber_none() throws Exception {
    Optional<BaseObject> ret = getXObjHandler().fetchNumber(0);
    assertFalse(ret.isPresent());
  }

  @Test
  public void test_fetchNumber_noDoc() throws Exception {
    doc = null;
    try {
      getXObjHandler().fetchNumber(0);
      fail("expecting NullPointerException");
    } catch (NullPointerException exc) {
      // expected
    }
  }

  @Test
  public void test_fetchList() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);
    BaseObject obj3 = addObj(classRef, null, null);
    List<BaseObject> ret = getXObjHandler().fetchList();
    assertEquals(3, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj3, ret.get(1));
    assertSame(obj2, ret.get(2));
  }

  @Test
  public void test_fetchList_noDoc() throws Exception {
    doc = null;
    try {
      getXObjHandler().fetchList();
      fail("expecting NullPointerException");
    } catch (NullPointerException exc) {
      // expected
    }
  }

  @Test
  public void test_fetchList_unmodifiable() throws Exception {
    addObj(classRef, null, null);
    try {
      getXObjHandler().fetchList().remove(0);
      fail("expecting UnsupportedOperationException");
    } catch (UnsupportedOperationException exc) {
      // expected
    }
  }

  @Test
  public void test_fetchMap() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);
    BaseObject obj3 = addObj(classRef, null, null);
    Map<DocumentReference, List<BaseObject>> ret = getXObjHandler().fetchMap();
    assertEquals(2, ret.size());
    assertSame(2, ret.get(classRef).size());
    assertSame(obj1, ret.get(classRef).get(0));
    assertSame(obj3, ret.get(classRef).get(1));
    assertSame(1, ret.get(classRef2).size());
    assertSame(obj2, ret.get(classRef2).get(0));
  }

  @Test
  public void test_fetchMap_noDoc() throws Exception {
    doc = null;
    try {
      getXObjHandler().fetchMap();
      fail("expecting NullPointerException");
    } catch (NullPointerException exc) {
      // expected
    }
  }

  @Test
  public void test_fetchMap_unmodifiable() throws Exception {
    addObj(classRef, null, null);
    try {
      getXObjHandler().fetchMap().remove(classRef);
      fail("expecting UnsupportedOperationException");
    } catch (UnsupportedOperationException exc) {
      // expected
    }
    try {
      getXObjHandler().fetchMap().get(classRef).remove(0);
      fail("expecting UnsupportedOperationException");
    } catch (UnsupportedOperationException exc) {
      // expected
    }
  }

  @Test
  public void test_create() throws Exception {
    expectNewBaseObject(classRef);
    replayDefault();
    List<BaseObject> ret = getXObjHandler().filter(classRef).create();
    verifyDefault();
    assertEquals(1, ret.size());
    assertEquals(classRef, ret.get(0).getXClassReference());
    assertObjs(getXObjHandler(), ret.get(0));
  }

  @Test
  public void test_create_keyValue() throws Exception {
    BaseClass bClassMock = expectNewBaseObject(classRef);
    ClassField<String> field1 = TestClassDefinition.FIELD_MY_STRING;
    expect(bClassMock.get(eq(field1.getName()))).andReturn(field1.getXField()).anyTimes();
    List<String> vals = Arrays.asList("val1", "val2");
    ClassField<Integer> field2 = TestClassDefinition.FIELD_MY_INT;
    expect(bClassMock.get(eq(field2.getName()))).andReturn(field2.getXField()).anyTimes();
    int val = 2;
    expectNewBaseObject(classRef2);
    replayDefault();
    List<BaseObject> ret = getXObjHandler().filter(field1, vals).filter(field2, val).filter(
        classRef2).create();
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(classRef, ret.get(0).getXClassReference());
    assertTrue(vals.contains(ret.get(0).getStringValue(field1.getName())));
    assertEquals(val, ret.get(0).getIntValue(field2.getName()));
    assertEquals(classRef2, ret.get(1).getXClassReference());
    assertObjs(getXObjHandler(), ret.get(0), ret.get(1));
  }

  @Test
  public void test_create_none() throws Exception {
    replayDefault();
    List<BaseObject> ret = getXObjHandler().create();
    verifyDefault();
    assertEquals(0, ret.size());
  }

  @Test
  public void test_create_ClassDocumentLoadException() throws Exception {
    Throwable cause = new XWikiException();
    expect(createBaseClassMock(classRef).newCustomClassInstance(same(getContext()))).andThrow(
        cause).once();
    replayDefault();
    try {
      getXObjHandler().filter(classRef).create();
    } catch (ClassDocumentLoadException exc) {
      assertSame(cause, exc.getCause());
    }
  }

  @Test
  public void test_create_noDoc() throws Exception {
    doc = null;
    try {
      getXObjHandler().create();
      fail("expecting NullPointerException");
    } catch (NullPointerException exc) {
      // expected
    }
  }

  @Test
  public void test_createIfNotExists_create() throws Exception {
    expectNewBaseObject(classRef);
    replayDefault();
    List<BaseObject> ret = getXObjHandler().filter(classRef).createIfNotExists();
    verifyDefault();
    assertEquals(1, ret.size());
    assertEquals(classRef, ret.get(0).getXClassReference());
    assertObjs(getXObjHandler(), ret.get(0));
  }

  @Test
  public void test_createIfNotExists_exists() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    replayDefault();
    List<BaseObject> ret = getXObjHandler().filter(classRef).createIfNotExists();
    verifyDefault();
    assertEquals(0, ret.size());
    assertObjs(getXObjHandler(), obj);
  }

  @Test
  public void test_createIfNotExists_none() throws Exception {
    replayDefault();
    List<BaseObject> ret = getXObjHandler().createIfNotExists();
    verifyDefault();
    assertEquals(0, ret.size());
  }

  @Test
  public void test_remove() {
    BaseObject obj = addObj(classRef, null, null);
    List<BaseObject> ret = getXObjHandler().remove();
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(0));
    assertObjs(getXObjHandler());
  }

  @Test
  public void test_remove_classRef() {
    BaseObject obj = addObj(classRef, null, null);
    List<BaseObject> ret = getXObjHandler().filter(classRef).remove();
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(0));
    assertObjs(getXObjHandler());
  }

  @Test
  public void test_remove_none() {
    BaseObject obj = addObj(classRef2, null, null);
    List<BaseObject> ret = getXObjHandler().filter(classRef).remove();
    assertEquals(0, ret.size());
    assertObjs(getXObjHandler(), obj);
  }

  @Test
  public void test_remove_multiple() {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);
    BaseObject obj3 = addObj(classRef, null, null);
    List<BaseObject> ret = getXObjHandler().filter(classRef).remove();
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj3, ret.get(1));
    assertObjs(getXObjHandler(), obj2);
  }

  @Test
  public void test_remove_keyValue() {
    ClassField<String> field = TestClassDefinition.FIELD_MY_STRING;
    List<String> vals = Arrays.asList("val1", "val2");
    BaseObject obj1 = addObj(classRef, field.getName(), vals.get(0));
    BaseObject obj2 = addObj(classRef, null, null);
    BaseObject obj3 = addObj(classRef, field.getName(), vals.get(1));
    BaseObject obj4 = addObj(classRef2, field.getName(), vals.get(0));
    List<BaseObject> ret = getXObjHandler().filter(field, vals).remove();
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj3, ret.get(1));
    assertObjs(getXObjHandler(), obj2, obj4);
  }

  private BaseObject addObj(DocumentReference classRef, String key, String value) {
    BaseObject obj = createObj(classRef, key, value);
    doc.addXObject(obj);
    return obj;
  }

  private BaseObject createObj(DocumentReference classRef, String key, String value) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    if (key != null) {
      obj.setStringValue(key, value);
    }
    return obj;
  }

  private void assertObjs(XObjectHandler xObjHandler, BaseObject... expObjs) {
    List<BaseObject> ret = xObjHandler.fetchList();
    assertEquals("not same size, objs: " + ret, expObjs.length, ret.size());
    for (int i = 0; i < ret.size(); i++) {
      assertSame("not same obj at " + i, expObjs[i], ret.get(i));
    }
  }

}
