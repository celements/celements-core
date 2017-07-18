package com.celements.model.access.object;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.model.classes.TestClassDefinition.*;
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
import com.celements.model.access.object.restriction.ClassRestriction;
import com.celements.model.access.object.restriction.FieldAbsentRestriction;
import com.celements.model.access.object.restriction.ObjectQuery;
import com.celements.model.access.object.xwiki.XWikiObjectHandler;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.web.Utils;

public class ObjectHandlerTest extends AbstractComponentTest {

  private WikiReference wikiRef;
  private XWikiDocument doc;
  private ClassReference classRef;
  private ClassReference classRef2;

  @Before
  public void prepareTest() throws Exception {
    wikiRef = new WikiReference("db");
    doc = new XWikiDocument(new DocumentReference(wikiRef.getName(), "space", "doc"));
    classRef = Utils.getComponent(ClassDefinition.class, NAME).getClassReference();
    classRef2 = new ClassReference("class", "other");
  }

  private ObjectHandler<XWikiDocument, BaseObject> getObjHandler() {
    return XWikiObjectHandler.on(doc);
  }

  @Test
  public void test_onDoc_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        XWikiObjectHandler.on(null);
      }
    }.evaluate();
  }

  @Test
  public void test_onDoc_isTranslation() throws Exception {
    IllegalArgumentException iae = new ExceptionAsserter<IllegalArgumentException>(
        IllegalArgumentException.class) {

      @Override
      protected void execute() throws IllegalArgumentException {
        doc.setLanguage("en");
        doc.setTranslation(1);
        getObjHandler();
      }
    }.evaluate();
    assertTrue("format not replacing placeholder 0", iae.getMessage().contains("'en'"));
    assertTrue("format not replacing placeholder 1", iae.getMessage().contains("'"
        + doc.getDocumentReference() + "'"));
  }

  @Test
  public void test_filter_classRef_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().filter((ClassReference) null);
      }
    }.evaluate();
  }

  @Test
  public void test_filter_field_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().filter((ClassField<String>) null, null);
      }
    }.evaluate();
  }

  @Test
  public void test_filter_values_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().filter(FIELD_MY_STRING, (List<String>) null);
      }
    }.evaluate();
  }

  @Test
  public void test_filter_values_nullElement() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().filter(FIELD_MY_STRING, Arrays.<String>asList("", null));
      }
    }.evaluate();
  }

  @Test
  public void test_filter_values_empty() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    addObj(classRef, null, null);
    addObj(classRef, field.getName(), null);
    addObj(classRef, field.getName(), "val");
    assertObjs(getObjHandler().filter(field, Collections.<String>emptyList()));
  }

  @Test
  public void test_filter_unique() throws Exception {
    ObjectHandler<XWikiDocument, BaseObject> handler = getObjHandler();
    handler.filter(classRef).filter(FIELD_MY_STRING, "val").filterAbsent(FIELD_MY_INT);
    assertEquals(3, handler.getQuery().size());
    handler.filter(classRef).filter(FIELD_MY_STRING, "val").filterAbsent(FIELD_MY_INT);
    assertEquals(3, handler.getQuery().size());
    handler.with(handler.getQuery());
    assertEquals(3, handler.getQuery().size());
  }

  @Test
  public void test_filterAbsent() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    BaseObject obj = addObj(classRef, field.getName(), null);
    assertObjs(getObjHandler().filterAbsent(field), obj);
    assertObjs(getObjHandler().filter(classRef).filterAbsent(field), obj);
    assertObjs(getObjHandler().filter(field, "val").filterAbsent(field));
  }

  @Test
  public void test_filterAbsent_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().filterAbsent(null);
      }
    }.evaluate();
  }

  @Test
  public void test_with() throws Exception {
    ObjectHandler<XWikiDocument, BaseObject> handler = getObjHandler();
    ObjectQuery<BaseObject> queryInit = new ObjectQuery<>();
    queryInit.add(new ClassRestriction<>(handler.getBridge(), classRef));
    queryInit.add(new FieldAbsentRestriction<>(handler.getBridge(), FIELD_MY_STRING));
    handler.with(queryInit);
    ObjectQuery<BaseObject> query = handler.getQuery();
    assertEquals(2, query.size());
    assertEquals(queryInit, query);
    queryInit.add(new ClassRestriction<>(handler.getBridge(), classRef2));
    assertEquals("query should be cloned in with", 2, query.size());
    handler.getQuery().add(new ClassRestriction<>(handler.getBridge(), classRef2));
    assertEquals("getQuery should return a clone", 2, query.size());
  }

  @Test
  public void test_with_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws NullPointerException {
        getObjHandler().with(null);
      }
    }.evaluate();
  }

  @Test
  public void test_fetch_emptyDoc() throws Exception {
    List<BaseObject> ret = getObjHandler().fetch().list();
    assertEquals(0, ret.size());
  }

  @Test
  public void test_fetch_oneObj() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    String val = "val";
    BaseObject obj = addObj(classRef, field.getName(), val);
    assertObjs(getObjHandler(), obj);
    assertObjs(getObjHandler().filter(classRef), obj);
    assertObjs(getObjHandler().filter(field, val), obj);
    assertObjs(getObjHandler().filter(field, Arrays.asList("", val)), obj);
    assertObjs(getObjHandler().filterAbsent(field));
  }

  @Test
  public void test_fetch_mutlipleObj() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    String val1 = "val1";
    String val2 = "val2";
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, field.getName(), val1);
    BaseObject obj3 = addObj(classRef, field.getName(), val1);
    BaseObject obj4 = addObj(classRef, "other", null);
    BaseObject obj5 = addObj(classRef, field.getName(), null);
    BaseObject obj6 = addObj(classRef, field.getName(), val2);
    assertObjs(getObjHandler(), obj1, obj3, obj4, obj5, obj6, obj2);
    assertObjs(getObjHandler().filter(classRef), obj1, obj3, obj4, obj5, obj6);
    assertObjs(getObjHandler().filterAbsent(field), obj1, obj4, obj5);
    assertObjs(getObjHandler().filter(field, val1), obj3);
    assertObjs(getObjHandler().filter(field, val2), obj6);
    assertObjs(getObjHandler().filter(field, Arrays.asList(val1, val2)), obj3, obj6);
    assertObjs(getObjHandler().filter(field, val1).filter(field, val2));
    assertObjs(getObjHandler().filter(field, "other"));
    assertObjs(getObjHandler().filter(classRef2), obj2);
    assertObjs(getObjHandler().filter(field, val1).filter(classRef2), obj3, obj2);
    assertObjs(getObjHandler().filter(classRef).filter(classRef2), obj1, obj3, obj4, obj5, obj6,
        obj2);
  }

  @Test
  public void test_fetch_mutlipleFields() throws Exception {
    ClassField<String> field1 = FIELD_MY_STRING;
    String val1 = "val";
    ClassField<Integer> field2 = FIELD_MY_INT;
    Integer val2 = 5;

    BaseObject obj1 = addObj(classRef, field1.getName(), val1);
    BaseObject obj2 = addObj(classRef, null, null);
    obj2.setIntValue(field2.getName(), val2);
    BaseObject obj3 = addObj(classRef, field1.getName(), val1);
    obj3.setIntValue(field2.getName(), val2);

    assertObjs(getObjHandler().filter(field1, val1), obj1, obj3);
    assertObjs(getObjHandler().filter(field2, val2), obj2, obj3);
    assertObjs(getObjHandler().filter(field1, val1).filter(field2, val2), obj3);
    assertObjs(getObjHandler().filterAbsent(field1), obj2);
    assertObjs(getObjHandler().filterAbsent(field1).filter(field2, val2), obj2);
    assertObjs(getObjHandler().filterAbsent(field2), obj1);
    assertObjs(getObjHandler().filterAbsent(field2).filter(field1, val1), obj1);
  }

  @Test
  public void test_fetch_clone() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    BaseObject ret = getObjHandler().fetch().first().get();
    ret.setStringValue(FIELD_MY_STRING.getName(), "val");
    ((BaseProperty) ret.getField(FIELD_MY_STRING.getName())).setValue("val");
    assertNull("manipulation on fetched object changed original", obj.get(
        FIELD_MY_STRING.getName()));
  }

  @Test
  public void test_fetchFirst() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    addObj(classRef2, null, null);
    addObj(classRef, null, null);
    Optional<BaseObject> ret = getObjHandler().fetch().first();
    assertTrue(ret.isPresent());
    assertEqualObjs(obj1, ret.get());
  }

  @Test
  public void test_fetchFirst_none() throws Exception {
    Optional<BaseObject> ret = getObjHandler().fetch().first();
    assertFalse(ret.isPresent());
  }

  @Test
  public void test_fetchFirst_noDoc() throws Exception {
    doc = null;
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().fetch().first();
      }
    }.evaluate();
  }

  @Test
  public void test_fetchNumber() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    addObj(classRef2, null, null);
    addObj(classRef, null, null);
    Optional<BaseObject> ret = getObjHandler().fetch().number(obj1.getNumber());
    assertTrue(ret.isPresent());
    assertEqualObjs(obj1, ret.get());
  }

  @Test
  public void test_fetchNumber_none() throws Exception {
    Optional<BaseObject> ret = getObjHandler().fetch().number(0);
    assertFalse(ret.isPresent());
  }

  @Test
  public void test_fetchNumber_noDoc() throws Exception {
    doc = null;
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().fetch().number(0);
      }
    }.evaluate();
  }

  @Test
  public void test_fetchList() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);
    BaseObject obj3 = addObj(classRef, null, null);
    List<BaseObject> ret = getObjHandler().fetch().list();
    assertEquals(3, ret.size());
    assertEqualObjs(obj1, ret.get(0));
    assertEqualObjs(obj3, ret.get(1));
    assertEqualObjs(obj2, ret.get(2));
  }

  @Test
  public void test_fetchList_noDoc() throws Exception {
    doc = null;
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().fetch().list();
      }
    }.evaluate();
  }

  @Test
  public void test_fetchList_immutability() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    new ExceptionAsserter<UnsupportedOperationException>(UnsupportedOperationException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().fetch().list().remove(0);
      }
    }.evaluate();
    assertObjs(getObjHandler(), obj);
  }

  @Test
  public void test_fetchMap() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);
    BaseObject obj3 = addObj(classRef, null, null);
    Map<ClassReference, List<BaseObject>> ret = getObjHandler().fetch().map();
    assertEquals(2, ret.size());
    assertEquals(2, ret.get(classRef).size());
    assertEqualObjs(obj1, ret.get(classRef).get(0));
    assertEqualObjs(obj3, ret.get(classRef).get(1));
    assertEquals(1, ret.get(classRef2).size());
    assertEqualObjs(obj2, ret.get(classRef2).get(0));
  }

  @Test
  public void test_fetchMap_noDoc() throws Exception {
    doc = null;
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().fetch().map();
      }
    }.evaluate();
  }

  @Test
  public void test_fetchMap_immutability() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    new ExceptionAsserter<UnsupportedOperationException>(UnsupportedOperationException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().fetch().map().remove(classRef);
      }
    }.evaluate();
    assertObjs(getObjHandler(), obj);
    new ExceptionAsserter<UnsupportedOperationException>(UnsupportedOperationException.class) {

      @Override
      protected void execute() throws Exception {
        getObjHandler().fetch().map().get(classRef).remove(0);
      }
    }.evaluate();
    assertObjs(getObjHandler(), obj);
  }

  @Test
  public void test_fetch_immutability() {
    ObjectHandler<XWikiDocument, BaseObject> handler = getObjHandler();
    handler.filter(classRef);
    assertEquals(1, handler.fetch().map().size());
    ObjectFetcher<XWikiDocument, BaseObject> fetcher = handler.fetch();
    handler.filter(classRef2);
    assertEquals(2, handler.fetch().map().size());
    assertEquals("fetcher was also mutated by second handler mutation", 1, fetcher.map().size());
  }

  @Test
  public void test_create() throws Exception {
    expectNewBaseObject(classRef.getDocRef(wikiRef));
    replayDefault();
    Map<ClassReference, BaseObject> ret = getObjHandler().filter(classRef).edit().create();
    verifyDefault();
    assertEquals(1, ret.size());
    assertEquals(classRef.getDocRef(wikiRef), ret.get(classRef).getXClassReference());
    assertObjs(getObjHandler(), ret.get(classRef));
  }

  @Test
  public void test_create_notClone() throws Exception {
    expectNewBaseObject(classRef.getDocRef(wikiRef));
    replayDefault();
    BaseObject ret = getObjHandler().filter(classRef).edit().create().get(classRef);
    verifyDefault();
    // manipulating created object also affects the doc
    ret.setStringValue(FIELD_MY_STRING.getName(), "asdf");
    assertObjs(getObjHandler(), ret);
  }

  @Test
  public void test_create_keyValue() throws Exception {
    BaseClass bClassMock = expectNewBaseObject(classRef.getDocRef(wikiRef));
    ClassField<String> field1 = FIELD_MY_STRING;
    expect(bClassMock.get(eq(field1.getName()))).andReturn(field1.getXField()).anyTimes();
    List<String> vals = Arrays.asList("val1", "val2");
    ClassField<Integer> field2 = FIELD_MY_INT;
    expect(bClassMock.get(eq(field2.getName()))).andReturn(field2.getXField()).anyTimes();
    int val = 2;
    expectNewBaseObject(classRef2.getDocRef(wikiRef));
    replayDefault();
    Map<ClassReference, BaseObject> ret = getObjHandler().filter(field1, vals).filter(field2,
        val).filter(classRef2).edit().create();
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(classRef.getDocRef(wikiRef), ret.get(classRef).getXClassReference());
    assertTrue(vals.contains(ret.get(classRef).getStringValue(field1.getName())));
    assertEquals(val, ret.get(classRef).getIntValue(field2.getName()));
    assertEquals(classRef2.getDocRef(wikiRef), ret.get(classRef2).getXClassReference());
    assertObjs(getObjHandler(), ret.get(classRef), ret.get(classRef2));
  }

  @Test
  public void test_create_none() throws Exception {
    replayDefault();
    Map<ClassReference, BaseObject> ret = getObjHandler().edit().create();
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
      getObjHandler().filter(classRef).edit().create();
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
        getObjHandler().edit().create();
      }
    }.evaluate();
  }

  @Test
  public void test_createIfNotExists_create() throws Exception {
    expectNewBaseObject(classRef.getDocRef(wikiRef));
    replayDefault();
    Map<ClassReference, BaseObject> ret = getObjHandler().filter(
        classRef).edit().createIfNotExists();
    verifyDefault();
    assertEquals(1, ret.size());
    assertEquals(classRef.getDocRef(wikiRef), ret.get(classRef).getXClassReference());
    assertObjs(getObjHandler(), ret.get(classRef));
  }

  @Test
  public void test_createIfNotExists_create_field() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    String val = "val";
    BaseObject obj = addObj(classRef, field.getName(), "otherval");
    BaseClass bClass = expectNewBaseObject(classRef.getDocRef(wikiRef));
    expect(bClass.get(field.getName())).andReturn(new StringClass()).once();
    replayDefault();
    Map<ClassReference, BaseObject> ret = getObjHandler().filter(field,
        val).edit().createIfNotExists();
    verifyDefault();
    assertEquals(1, ret.size());
    assertNotSame(obj, ret.get(classRef));
    assertEquals(classRef.getDocRef(wikiRef), ret.get(classRef).getXClassReference());
    assertEquals(val, ret.get(classRef).getStringValue(field.getName()));
    assertObjs(getObjHandler(), obj, ret.get(classRef));
  }

  @Test
  public void test_createIfNotExists_exists() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    replayDefault();
    Map<ClassReference, BaseObject> ret = getObjHandler().filter(
        classRef).edit().createIfNotExists();
    verifyDefault();
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(classRef));
    assertObjs(getObjHandler(), obj);
  }

  @Test
  public void test_createIfNotExists_exists_field() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    String val = "val";
    BaseObject obj = addObj(classRef, field.getName(), val);
    replayDefault();
    Map<ClassReference, BaseObject> ret = getObjHandler().filter(field,
        val).edit().createIfNotExists();
    verifyDefault();
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(classRef));
    assertObjs(getObjHandler(), obj);
  }

  @Test
  public void test_createIfNotExists_none() throws Exception {
    replayDefault();
    Map<ClassReference, BaseObject> ret = getObjHandler().edit().createIfNotExists();
    verifyDefault();
    assertEquals(0, ret.size());
  }

  @Test
  public void test_remove() {
    BaseObject obj = addObj(classRef, null, null);
    List<BaseObject> ret = getObjHandler().edit().remove();
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(0));
    assertObjs(getObjHandler());
  }

  @Test
  public void test_remove_classRef() {
    BaseObject obj = addObj(classRef, null, null);
    List<BaseObject> ret = getObjHandler().filter(classRef).edit().remove();
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(0));
    assertObjs(getObjHandler());
  }

  @Test
  public void test_remove_none() {
    BaseObject obj = addObj(classRef2, null, null);
    List<BaseObject> ret = getObjHandler().filter(classRef).edit().remove();
    assertEquals(0, ret.size());
    assertObjs(getObjHandler(), obj);
  }

  @Test
  public void test_remove_multiple() {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);
    BaseObject obj3 = addObj(classRef, null, null);
    List<BaseObject> ret = getObjHandler().filter(classRef).edit().remove();
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj3, ret.get(1));
    assertObjs(getObjHandler(), obj2);
  }

  @Test
  public void test_remove_keyValue() {
    ClassField<String> field = FIELD_MY_STRING;
    List<String> vals = Arrays.asList("val1", "val2");
    BaseObject obj1 = addObj(classRef, field.getName(), vals.get(0));
    BaseObject obj2 = addObj(classRef, null, null);
    BaseObject obj3 = addObj(classRef, field.getName(), vals.get(1));
    BaseObject obj4 = addObj(classRef2, field.getName(), vals.get(0));
    List<BaseObject> ret = getObjHandler().filter(field, vals).edit().remove();
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj3, ret.get(1));
    assertObjs(getObjHandler(), obj2, obj4);
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

  private static void assertObjs(ObjectHandler<?, BaseObject> objHandler, BaseObject... expObjs) {
    List<BaseObject> ret = objHandler.fetch().list();
    assertEquals("not same size, objs: " + ret, expObjs.length, ret.size());
    for (int i = 0; i < ret.size(); i++) {
      assertNotSame("obj not cloned at " + i, expObjs[i], ret.get(i));
      assertEqualObjs(expObjs[i], ret.get(i));
    }
  }

  private static void assertEqualObjs(BaseObject expObj, BaseObject actObj) {
    assertNotSame("obj not cloned", expObj, actObj);
    assertEquals(expObj.getDocumentReference(), actObj.getDocumentReference());
    assertEquals(expObj.getXClassReference(), actObj.getXClassReference());
    assertEquals(expObj.getNumber(), actObj.getNumber());
    assertEquals(expObj.getId(), actObj.getId());
    assertEquals("not same amount of fields set", expObj.getPropertyList().size(),
        actObj.getPropertyList().size());
    for (String propName : expObj.getPropertyList()) {
      try {
        BaseProperty expProp = (BaseProperty) expObj.get(propName);
        BaseProperty actProp = (BaseProperty) actObj.get(propName);
        assertEquals(expProp.getName(), actProp.getName());
        assertEquals(expProp.getValue(), actProp.getValue());
      } catch (XWikiException xwe) {
        throw new RuntimeException(xwe);
      }
    }
  }

}
