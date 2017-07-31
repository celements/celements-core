package com.celements.model.access.object;

import static com.celements.model.classes.TestClassDefinition.*;
import static com.google.common.base.MoreObjects.*;
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
import com.celements.model.access.object.xwiki.XWikiObjectFetcher;
import com.celements.model.access.object.xwiki.XWikiObjectFetcher.Builder;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.web.Utils;

public class ObjectFetcherTest extends AbstractComponentTest {

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

  private Builder newBuilder() {
    return XWikiObjectFetcher.on(doc);
  }

  @Test
  public void test_nullDoc() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        XWikiObjectFetcher.on(null);
      }
    }.evaluate();
  }

  @Test
  public void test_isTranslation() throws Exception {
    IllegalArgumentException iae = new ExceptionAsserter<IllegalArgumentException>(
        IllegalArgumentException.class) {

      @Override
      protected void execute() throws IllegalArgumentException {
        doc.setLanguage("en");
        doc.setTranslation(1);
        newBuilder().fetch();
      }
    }.evaluate();
    assertTrue("format not replacing placeholder 0", iae.getMessage().contains("'en'"));
    assertTrue("format not replacing placeholder 1", iae.getMessage().contains("'"
        + doc.getDocumentReference() + "'"));
  }

  @Test
  public void test_fetch_emptyDoc() throws Exception {
    assertObjs(newBuilder());
  }

  @Test
  public void test_fetch_clone() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    BaseObject ret = newBuilder().fetch().first().get();
    assertEqualObjs(obj, ret);
  }

  @Test
  public void test_fetch_oneObj() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    String val = "val";
    BaseObject obj = addObj(classRef, field, val);

    assertObjs(newBuilder(), obj);
    assertObjs(newBuilder().filter(classRef), obj);
    assertObjs(newBuilder().filter(field, val), obj);
    assertObjs(newBuilder().filter(field, Arrays.asList("asdf", val)), obj);
    assertObjs(newBuilder().filterAbsent(field));
  }

  @Test
  public void test_fetch_class() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);

    assertObjs(newBuilder().filter(classRef), obj1);
    assertObjs(newBuilder().filter(classRef2), obj2);
    assertObjs(newBuilder().filter(classRef).filter(classRef2), obj1, obj2);
  }

  @Test
  public void test_fetch_values_empty() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    addObj(classRef, null, null);
    addObj(classRef, field, null);
    addObj(classRef, field, "val");
    assertObjs(newBuilder().filter(field, Collections.<String>emptyList()));
  }

  @Test
  public void test_fetch_absent() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    BaseObject obj = addObj(classRef, field, null);
    assertObjs(newBuilder().filterAbsent(field), obj);
    assertObjs(newBuilder().filter(classRef).filterAbsent(field), obj);
    assertObjs(newBuilder().filter(field, "val").filterAbsent(field));
  }

  @Test
  public void test_fetch_field_and() throws Exception {
    ClassField<String> field1 = FIELD_MY_STRING;
    String val1 = "val";
    ClassField<Integer> field2 = FIELD_MY_INT;
    Integer val2 = 5;

    BaseObject obj1 = addObj(classRef, field1, val1);
    BaseObject obj2 = addObj(classRef, field2, val2);
    BaseObject obj3 = addObj(classRef, field1, val1);
    obj3.setIntValue(field2.getName(), val2);

    assertObjs(newBuilder(), obj1, obj2, obj3);
    assertObjs(newBuilder().filter(field1, val1), obj1, obj3);
    assertObjs(newBuilder().filter(field2, val2), obj2, obj3);
    assertObjs(newBuilder().filter(field1, val1).filter(field2, val2), obj3);
    assertObjs(newBuilder().filterAbsent(field1), obj2);
    assertObjs(newBuilder().filterAbsent(field1).filter(field2, val2), obj2);
    assertObjs(newBuilder().filterAbsent(field2), obj1);
    assertObjs(newBuilder().filterAbsent(field2).filter(field1, val1), obj1);
  }

  @Test
  public void test_fetch_field_or() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    String val1 = "val1";
    String val2 = "val2";

    addObj(classRef, null, null);
    addObj(classRef, field, "asdf");
    BaseObject obj1 = addObj(classRef, field, val1);
    BaseObject obj2 = addObj(classRef, field, val2);

    assertObjs(newBuilder().filter(field, val1), obj1);
    assertObjs(newBuilder().filter(field, val2), obj2);
    assertObjs(newBuilder().filter(field, Arrays.asList(val1, val2)), obj1, obj2);
  }

  @Test
  public void test_fetch_combined() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    String val1 = "val1";
    String val2 = "val2";
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, field, val1);
    BaseObject obj3 = addObj(classRef, field, val1);
    BaseObject obj4 = addObj(classRef, FIELD_MY_INT, null);
    BaseObject obj5 = addObj(classRef, field, null);
    BaseObject obj6 = addObj(classRef, field, val2);
    assertObjs(newBuilder(), obj1, obj3, obj4, obj5, obj6, obj2);
    assertObjs(newBuilder().filter(classRef), obj1, obj3, obj4, obj5, obj6);
    assertObjs(newBuilder().filterAbsent(field), obj1, obj4, obj5);
    assertObjs(newBuilder().filter(field, val1), obj3);
    assertObjs(newBuilder().filter(field, val2), obj6);
    assertObjs(newBuilder().filter(field, Arrays.asList(val1, val2)), obj3, obj6);
    assertObjs(newBuilder().filter(field, val1).filter(field, val2));
    assertObjs(newBuilder().filter(field, "other"));
    assertObjs(newBuilder().filter(classRef2), obj2);
    assertObjs(newBuilder().filter(field, val1).filter(classRef2), obj3, obj2);
    assertObjs(newBuilder().filter(classRef).filter(classRef2), obj1, obj3, obj4, obj5, obj6, obj2);
  }

  @Test
  public void test_fetchFirst() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    addObj(classRef2, null, null);
    addObj(classRef, null, null);
    Optional<BaseObject> ret = newBuilder().fetch().first();
    assertTrue(ret.isPresent());
    assertEqualObjs(obj1, ret.get());
  }

  @Test
  public void test_fetchFirst_none() throws Exception {
    Optional<BaseObject> ret = newBuilder().fetch().first();
    assertFalse(ret.isPresent());
  }

  @Test
  public void test_fetch_number() throws Exception {
    assertObjs(newBuilder().filter(-1));
    assertObjs(newBuilder().filter(0));
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef, null, null);
    assertObjs(newBuilder().filter(-1));
    assertObjs(newBuilder().filter(0), obj1);
    assertObjs(newBuilder().filter(1), obj2);
  }

  @Test
  public void test_fetch_number_multiple() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);
    addObj(classRef, null, null);
    addObj(classRef2, null, null);
    assertObjs(newBuilder().filter(obj1.getNumber()), obj1, obj2);
    assertObjs(newBuilder().filter(obj1.getNumber()).filter(classRef), obj1);
    assertObjs(newBuilder().filter(obj1.getNumber()).filter(classRef2), obj2);
  }

  @Test
  public void test_fetchList() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);
    BaseObject obj3 = addObj(classRef, null, null);
    List<BaseObject> ret = newBuilder().fetch().list();
    assertEquals(3, ret.size());
    assertEqualObjs(obj1, ret.get(0));
    assertEqualObjs(obj3, ret.get(1));
    assertEqualObjs(obj2, ret.get(2));
  }

  @Test
  public void test_fetchList_immutability() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    new ExceptionAsserter<UnsupportedOperationException>(UnsupportedOperationException.class) {

      @Override
      protected void execute() throws Exception {
        newBuilder().fetch().list().remove(0);
      }
    }.evaluate();
    assertObjs(newBuilder(), obj);
  }

  @Test
  public void test_fetchMap() throws Exception {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);
    BaseObject obj3 = addObj(classRef, null, null);
    Map<ClassReference, List<BaseObject>> ret = newBuilder().fetch().map();
    assertEquals(2, ret.size());
    assertEquals(2, ret.get(classRef).size());
    assertEqualObjs(obj1, ret.get(classRef).get(0));
    assertEqualObjs(obj3, ret.get(classRef).get(1));
    assertEquals(1, ret.get(classRef2).size());
    assertEqualObjs(obj2, ret.get(classRef2).get(0));
  }

  @Test
  public void test_fetchMap_immutability() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    new ExceptionAsserter<UnsupportedOperationException>(UnsupportedOperationException.class) {

      @Override
      protected void execute() throws Exception {
        newBuilder().fetch().map().remove(classRef);
      }
    }.evaluate();
    assertObjs(newBuilder(), obj);
    new ExceptionAsserter<UnsupportedOperationException>(UnsupportedOperationException.class) {

      @Override
      protected void execute() throws Exception {
        newBuilder().fetch().map().get(classRef).remove(0);
      }
    }.evaluate();
    assertObjs(newBuilder(), obj);
  }

  private <T> BaseObject addObj(ClassReference classRef, ClassField<T> field, T value) {
    BaseObject obj = createObj(classRef, field, value);
    doc.addXObject(obj);
    return obj;
  }

  private <T> BaseObject createObj(ClassReference classRef, ClassField<T> field, T value) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef.getDocRef(wikiRef));
    if (field != null) {
      if (field.getType() == String.class) {
        obj.setStringValue(field.getName(), (String) value);
      } else if (field.getType() == Integer.class) {
        obj.setIntValue(field.getName(), firstNonNull((Integer) value, 0));
      }
    }
    return obj;
  }

  private static void assertObjs(Builder builder, BaseObject... expObjs) {
    List<BaseObject> ret = builder.fetch().list();
    assertEquals("not same size, objs: " + ret, expObjs.length, ret.size());
    for (int i = 0; i < ret.size(); i++) {
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
