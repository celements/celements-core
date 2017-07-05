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
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.celements.model.access.exception.ClassDocumentLoadException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.TestClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.web.Utils;

public class DefaultXObjectHandlerTest extends AbstractComponentTest {

  private WikiReference wikiRef;
  private XWikiDocument doc;
  private ClassReference classRef;
  private ClassReference classRef2;

  @Before
  public void prepareTest() throws Exception {
    wikiRef = new WikiReference("db");
    doc = new XWikiDocument(new DocumentReference(wikiRef.getName(), "space", "doc"));
    classRef = Utils.getComponent(ClassDefinition.class,
        TestClassDefinition.NAME).getClassReference();
    classRef2 = new ClassReference("class", "other");
  }

  private XObjectHandler getXObjHandler() {
    return Utils.getComponent(XObjectHandler.class).onDoc(doc);
  }

  @Test
  public void test_onDoc_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().onDoc(null);
      }
    }.evaluate();
  }

  @Test
  public void test_onDoc_isTranslation() throws Exception {
    Exception ise = new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws Exception {
        doc.setLanguage("en");
        doc.setTranslation(1);
        getXObjHandler();
      }
    }.evaluate();
    assertTrue("format not replacing placeholder 0", ise.getMessage().contains("'en'"));
    assertTrue("format not replacing placeholder 1", ise.getMessage().contains("'"
        + doc.getDocumentReference() + "'"));
  }

  @Test
  public void test_filter_classRef_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().filter(null);
      }
    }.evaluate();
  }

  @Test
  public void test_filter_field_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().filter((ClassField<String>) null, null);
      }
    }.evaluate();
  }

  @Test
  public void test_filter_values_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().filter(TestClassDefinition.FIELD_MY_STRING, (List<String>) null);
      }
    }.evaluate();
  }

  @Test
  public void test_filter_values_empty() throws Exception {
    new ExceptionAsserter<IllegalArgumentException>(IllegalArgumentException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().filter(TestClassDefinition.FIELD_MY_STRING,
            Collections.<String>emptyList());

      }
    }.evaluate();
  }

  @Test
  public void test_filterAbsent() throws Exception {
    ClassField<String> field = TestClassDefinition.FIELD_MY_STRING;
    BaseObject obj = addObj(classRef, field.getName(), null);
    assertObjs(getXObjHandler().filterAbsent(field), obj);
    assertObjs(getXObjHandler().filter(classRef).filterAbsent(field), obj);
  }

  @Test
  public void test_filterAbsent_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().filterAbsent(null);
      }
    }.evaluate();
  }

  @Test
  public void test_filterAbsent_afterFilter() throws Exception {
    final ClassField<String> field = TestClassDefinition.FIELD_MY_STRING;
    Exception ise = new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().filter(field, "val").filterAbsent(field);
      }
    }.evaluate();
    assertEquals("filter field already present", ise.getMessage());
  }

  @Test
  public void test_filterAbsent_beforeFilter() throws Exception {
    final ClassField<String> field = TestClassDefinition.FIELD_MY_STRING;
    Exception ise = new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().filterAbsent(field).filter(field, "val");
      }
    }.evaluate();
    assertEquals("filter field already absent", ise.getMessage());
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
    assertObjs(getXObjHandler().filterAbsent(field));
  }

  @Test
  public void test_fetch_mutlipleObj() throws Exception {
    ClassField<String> field = TestClassDefinition.FIELD_MY_STRING;
    String val1 = "val1";
    String val2 = "val2";
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, field.getName(), val1);
    BaseObject obj3 = addObj(classRef, field.getName(), val1);
    BaseObject obj4 = addObj(classRef, "other", null);
    BaseObject obj5 = addObj(classRef, field.getName(), null);
    BaseObject obj6 = addObj(classRef, field.getName(), val2);
    assertObjs(getXObjHandler(), obj1, obj3, obj4, obj5, obj6, obj2);
    assertObjs(getXObjHandler().filter(classRef), obj1, obj3, obj4, obj5, obj6);
    assertObjs(getXObjHandler().filterAbsent(field), obj1, obj4, obj5);
    assertObjs(getXObjHandler().filter(field, val1), obj3);
    assertObjs(getXObjHandler().filter(field, val2), obj6);
    assertObjs(getXObjHandler().filter(field, Arrays.asList(val1, val2)), obj3, obj6);
    assertObjs(getXObjHandler().filter(field, val1).filter(field, val2), obj3, obj6);
    assertObjs(getXObjHandler().filter(field, "other"));
    assertObjs(getXObjHandler().filter(classRef2), obj2);
    assertObjs(getXObjHandler().filter(field, val1).filter(classRef2), obj3, obj2);
    assertObjs(getXObjHandler().filter(classRef).filter(classRef2), obj1, obj3, obj4, obj5, obj6,
        obj2);
  }

  @Test
  public void test_fetch_mutlipleFields() throws Exception {
    ClassField<String> field1 = TestClassDefinition.FIELD_MY_STRING;
    String val1 = "val";
    ClassField<Integer> field2 = TestClassDefinition.FIELD_MY_INT;
    Integer val2 = 5;

    BaseObject obj1 = addObj(classRef, field1.getName(), val1);
    BaseObject obj2 = addObj(classRef, null, null);
    obj2.setIntValue(field2.getName(), val2);
    BaseObject obj3 = addObj(classRef, field1.getName(), val1);
    obj3.setIntValue(field2.getName(), val2);

    assertObjs(getXObjHandler().filter(field1, val1), obj1, obj3);
    assertObjs(getXObjHandler().filter(field2, val2), obj2, obj3);
    assertObjs(getXObjHandler().filter(field1, val1).filter(field2, val2), obj3);
    assertObjs(getXObjHandler().filterAbsent(field1), obj2);
    assertObjs(getXObjHandler().filterAbsent(field1).filter(field2, val2), obj2);
    assertObjs(getXObjHandler().filterAbsent(field2), obj1);
    assertObjs(getXObjHandler().filterAbsent(field2).filter(field1, val1), obj1);
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
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().fetchFirst();
      }
    }.evaluate();
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
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().fetchNumber(0);
      }
    }.evaluate();
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
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().fetchList();
      }
    }.evaluate();
  }

  @Test
  public void test_fetchList_unmodifiable() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    getXObjHandler().fetchList().remove(0); // may not remove obj from doc
    assertObjs(getXObjHandler(), obj);
  }

  @Test
  public void test_fetchMap() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);
    BaseObject obj3 = addObj(classRef, null, null);
    Map<ClassReference, List<BaseObject>> ret = getXObjHandler().fetchMap();
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
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().fetchMap();
      }
    }.evaluate();
  }

  @Test
  public void test_fetchMap_unmodifiable() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    getXObjHandler().fetchMap().remove(classRef); // may not remove obj from doc
    assertObjs(getXObjHandler(), obj);
    getXObjHandler().fetchMap().get(classRef).remove(0); // may not remove obj from doc
    assertObjs(getXObjHandler(), obj);
  }

  @Test
  public void test_create() throws Exception {
    expectNewBaseObject(classRef.getDocRef(wikiRef));
    replayDefault();
    List<BaseObject> ret = getXObjHandler().filter(classRef).create();
    verifyDefault();
    assertEquals(1, ret.size());
    assertEquals(classRef.getDocRef(wikiRef), ret.get(0).getXClassReference());
    assertObjs(getXObjHandler(), ret.get(0));
  }

  @Test
  public void test_create_keyValue() throws Exception {
    BaseClass bClassMock = expectNewBaseObject(classRef.getDocRef(wikiRef));
    ClassField<String> field1 = TestClassDefinition.FIELD_MY_STRING;
    expect(bClassMock.get(eq(field1.getName()))).andReturn(field1.getXField()).anyTimes();
    List<String> vals = Arrays.asList("val1", "val2");
    ClassField<Integer> field2 = TestClassDefinition.FIELD_MY_INT;
    expect(bClassMock.get(eq(field2.getName()))).andReturn(field2.getXField()).anyTimes();
    int val = 2;
    expectNewBaseObject(classRef2.getDocRef(wikiRef));
    replayDefault();
    List<BaseObject> ret = getXObjHandler().filter(field1, vals).filter(field2, val).filter(
        classRef2).create();
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(classRef.getDocRef(wikiRef), ret.get(0).getXClassReference());
    assertTrue(vals.contains(ret.get(0).getStringValue(field1.getName())));
    assertEquals(val, ret.get(0).getIntValue(field2.getName()));
    assertEquals(classRef2.getDocRef(wikiRef), ret.get(1).getXClassReference());
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
    expect(createBaseClassMock(classRef.getDocRef(wikiRef)).newCustomClassInstance(same(
        getContext()))).andThrow(cause).once();
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
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getXObjHandler().create();
      }
    }.evaluate();
  }

  @Test
  public void test_createIfNotExists_create() throws Exception {
    expectNewBaseObject(classRef.getDocRef(wikiRef));
    replayDefault();
    List<BaseObject> ret = getXObjHandler().filter(classRef).createIfNotExists();
    verifyDefault();
    assertEquals(1, ret.size());
    assertEquals(classRef.getDocRef(wikiRef), ret.get(0).getXClassReference());
    assertObjs(getXObjHandler(), ret.get(0));
  }

  @Test
  public void test_createIfNotExists_create_field() throws Exception {
    ClassField<String> field = TestClassDefinition.FIELD_MY_STRING;
    String val = "val";
    BaseObject obj = addObj(classRef, field.getName(), "otherval");
    BaseClass bClass = expectNewBaseObject(classRef.getDocRef(wikiRef));
    expect(bClass.get(field.getName())).andReturn(new StringClass()).once();
    replayDefault();
    List<BaseObject> ret = getXObjHandler().filter(field, val).createIfNotExists();
    verifyDefault();
    assertEquals(1, ret.size());
    assertNotSame(obj, ret.get(0));
    assertEquals(classRef.getDocRef(wikiRef), ret.get(0).getXClassReference());
    assertEquals(val, ret.get(0).getStringValue(field.getName()));
    assertObjs(getXObjHandler(), obj, ret.get(0));
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
  public void test_createIfNotExists_exists_field() throws Exception {
    ClassField<String> field = TestClassDefinition.FIELD_MY_STRING;
    String val = "val";
    BaseObject obj = addObj(classRef, field.getName(), val);
    replayDefault();
    List<BaseObject> ret = getXObjHandler().filter(field, val).createIfNotExists();
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

  private BaseObject addObj(ClassReference classRef, String key, String value) {
    BaseObject obj = createObj(classRef, key, value);
    doc.addXObject(obj);
    return obj;
  }

  private BaseObject createObj(ClassReference classRef, String key, String value) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef.getDocRef(wikiRef));
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
