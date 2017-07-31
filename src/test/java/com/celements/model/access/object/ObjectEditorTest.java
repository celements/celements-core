package com.celements.model.access.object;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.model.classes.TestClassDefinition.*;
import static com.google.common.base.MoreObjects.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
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
import com.celements.model.access.object.xwiki.XWikiObjectEditor;
import com.celements.model.access.object.xwiki.XWikiObjectEditor.Builder;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.web.Utils;

public class ObjectEditorTest extends AbstractComponentTest {

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
    return XWikiObjectEditor.on(doc);
  }

  @Test
  public void test_nullDoc() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        XWikiObjectEditor.on(null);
      }
    }.evaluate();
  }

  @Test
  public void test_fetch_noClone() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    BaseObject ret = newBuilder().edit().fetch().first().get();
    assertSame(obj, ret);
  }

  @Test
  public void test_isTranslation() throws Exception {
    IllegalArgumentException iae = new ExceptionAsserter<IllegalArgumentException>(
        IllegalArgumentException.class) {

      @Override
      protected void execute() throws IllegalArgumentException {
        doc.setLanguage("en");
        doc.setTranslation(1);
        newBuilder().edit();
      }
    }.evaluate();
    assertTrue("format not replacing placeholder 0", iae.getMessage().contains("'en'"));
    assertTrue("format not replacing placeholder 1", iae.getMessage().contains("'"
        + doc.getDocumentReference() + "'"));
  }

  @Test
  public void test_create() throws Exception {
    expectNewBaseObject(classRef.getDocRef(wikiRef));
    replayDefault();
    Map<ClassReference, BaseObject> ret = newBuilder().filter(classRef).edit().create();
    verifyDefault();
    assertEquals(1, ret.size());
    assertEquals(classRef.getDocRef(wikiRef), ret.get(classRef).getXClassReference());
    assertObjs(newBuilder(), ret.get(classRef));
  }

  @Test
  public void test_create_notClone() throws Exception {
    expectNewBaseObject(classRef.getDocRef(wikiRef));
    replayDefault();
    BaseObject ret = newBuilder().filter(classRef).edit().create().get(classRef);
    verifyDefault();
    // manipulating created object also affects the doc
    ret.setStringValue(FIELD_MY_STRING.getName(), "asdf");
    assertObjs(newBuilder(), ret);
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
    Map<ClassReference, BaseObject> ret = newBuilder().filter(field1, vals).filter(field2,
        val).filter(classRef2).edit().create();
    verifyDefault();
    assertEquals(2, ret.size());
    assertEquals(classRef.getDocRef(wikiRef), ret.get(classRef).getXClassReference());
    assertTrue(vals.contains(ret.get(classRef).getStringValue(field1.getName())));
    assertEquals(val, ret.get(classRef).getIntValue(field2.getName()));
    assertEquals(classRef2.getDocRef(wikiRef), ret.get(classRef2).getXClassReference());
    assertObjs(newBuilder(), ret.get(classRef), ret.get(classRef2));
  }

  @Test
  public void test_create_none() throws Exception {
    replayDefault();
    Map<ClassReference, BaseObject> ret = newBuilder().edit().create();
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
      newBuilder().filter(classRef).edit().create();
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
        newBuilder().edit().create();
      }
    }.evaluate();
  }

  @Test
  public void test_createIfNotExists_create() throws Exception {
    expectNewBaseObject(classRef.getDocRef(wikiRef));
    replayDefault();
    Map<ClassReference, BaseObject> ret = newBuilder().filter(classRef).edit().createIfNotExists();
    verifyDefault();
    assertEquals(1, ret.size());
    assertEquals(classRef.getDocRef(wikiRef), ret.get(classRef).getXClassReference());
    assertObjs(newBuilder(), ret.get(classRef));
  }

  @Test
  public void test_createIfNotExists_create_field() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    String val = "val";
    BaseObject obj = addObj(classRef, field, "otherval");
    BaseClass bClass = expectNewBaseObject(classRef.getDocRef(wikiRef));
    expect(bClass.get(field.getName())).andReturn(new StringClass()).once();
    replayDefault();
    Map<ClassReference, BaseObject> ret = newBuilder().filter(field,
        val).edit().createIfNotExists();
    verifyDefault();
    assertEquals(1, ret.size());
    assertNotSame(obj, ret.get(classRef));
    assertEquals(classRef.getDocRef(wikiRef), ret.get(classRef).getXClassReference());
    assertEquals(val, ret.get(classRef).getStringValue(field.getName()));
    assertObjs(newBuilder(), obj, ret.get(classRef));
  }

  @Test
  public void test_createIfNotExists_exists() throws Exception {
    BaseObject obj = addObj(classRef, null, null);
    replayDefault();
    Map<ClassReference, BaseObject> ret = newBuilder().filter(classRef).edit().createIfNotExists();
    verifyDefault();
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(classRef));
    assertObjs(newBuilder(), obj);
  }

  @Test
  public void test_createIfNotExists_exists_field() throws Exception {
    ClassField<String> field = FIELD_MY_STRING;
    String val = "val";
    BaseObject obj = addObj(classRef, field, val);
    replayDefault();
    Map<ClassReference, BaseObject> ret = newBuilder().filter(field,
        val).edit().createIfNotExists();
    verifyDefault();
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(classRef));
    assertObjs(newBuilder(), obj);
  }

  @Test
  public void test_createIfNotExists_none() throws Exception {
    replayDefault();
    Map<ClassReference, BaseObject> ret = newBuilder().edit().createIfNotExists();
    verifyDefault();
    assertEquals(0, ret.size());
  }

  @Test
  public void test_remove() {
    BaseObject obj = addObj(classRef, null, null);
    List<BaseObject> ret = newBuilder().edit().remove();
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(0));
    assertObjs(newBuilder());
  }

  @Test
  public void test_remove_classRef() {
    BaseObject obj = addObj(classRef, null, null);
    List<BaseObject> ret = newBuilder().filter(classRef).edit().remove();
    assertEquals(1, ret.size());
    assertSame(obj, ret.get(0));
    assertObjs(newBuilder());
  }

  @Test
  public void test_remove_none() {
    BaseObject obj = addObj(classRef2, null, null);
    List<BaseObject> ret = newBuilder().filter(classRef).edit().remove();
    assertEquals(0, ret.size());
    assertObjs(newBuilder(), obj);
  }

  @Test
  public void test_remove_multiple() {
    BaseObject obj1 = addObj(classRef, null, null);
    BaseObject obj2 = addObj(classRef2, null, null);
    BaseObject obj3 = addObj(classRef, null, null);
    List<BaseObject> ret = newBuilder().filter(classRef).edit().remove();
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj3, ret.get(1));
    assertObjs(newBuilder(), obj2);
  }

  @Test
  public void test_remove_keyValue() {
    ClassField<String> field = FIELD_MY_STRING;
    List<String> vals = Arrays.asList("val1", "val2");
    BaseObject obj1 = addObj(classRef, field, vals.get(0));
    BaseObject obj2 = addObj(classRef, null, null);
    BaseObject obj3 = addObj(classRef, field, vals.get(1));
    BaseObject obj4 = addObj(classRef2, field, vals.get(0));
    List<BaseObject> ret = newBuilder().filter(field, vals).edit().remove();
    assertEquals(2, ret.size());
    assertSame(obj1, ret.get(0));
    assertSame(obj3, ret.get(1));
    assertObjs(newBuilder(), obj2, obj4);
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
    List<BaseObject> ret = builder.edit().fetch().list();
    assertEquals("not same size, objs: " + ret, expObjs.length, ret.size());
    for (int i = 0; i < ret.size(); i++) {
      assertSame(expObjs[i], ret.get(i));
    }
  }

}
