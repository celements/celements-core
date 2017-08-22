package com.celements.model.access.object;

import static com.celements.model.classes.TestClassDefinition.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.object.ObjectBridge;
import com.celements.model.object.restriction.ClassRestriction;
import com.celements.model.object.restriction.FieldAbsentRestriction;
import com.celements.model.object.restriction.ObjectQuery;
import com.celements.model.object.restriction.ObjectQueryBuilder;
import com.celements.model.object.xwiki.XWikiObjectBridge;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class QueryBuilderTest extends AbstractComponentTest {

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

  private XWikiObjectEditor newBuilder() {
    return XWikiObjectEditor.on(doc);
  }

  @Test
  public void test_filter_classRef_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        newBuilder().filter((ClassReference) null);
      }
    }.evaluate();
  }

  @Test
  public void test_filter_field_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        newBuilder().filter((ClassField<String>) null, (String) null);
      }
    }.evaluate();
  }

  @Test
  public void test_filter_values_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        newBuilder().filter(FIELD_MY_STRING, (List<String>) null);
      }
    }.evaluate();
  }

  @Test
  public void test_filter_values_nullElement() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        newBuilder().filter(FIELD_MY_STRING, Arrays.<String>asList("", null));
      }
    }.evaluate();
  }

  @Test
  public void test_filter_unique() throws Exception {
    ObjectQueryBuilder<?, BaseObject> builder = newBuilder();
    builder.filter(classRef).filter(FIELD_MY_STRING, "val").filterAbsent(FIELD_MY_INT);
    assertEquals(3, builder.getQuery().getRestrictions().size());
    builder.filter(classRef).filter(FIELD_MY_STRING, "val").filterAbsent(FIELD_MY_INT);
    assertEquals(3, builder.getQuery().getRestrictions().size());
    builder.with(builder.getQuery());
    assertEquals(3, builder.getQuery().getRestrictions().size());
  }

  @Test
  public void test_filterAbsent_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws Exception {
        newBuilder().filterAbsent(null);
      }
    }.evaluate();
  }

  @Test
  public void test_with() throws Exception {
    XWikiObjectEditor builder = newBuilder();
    ObjectQuery<BaseObject> queryInit = new ObjectQuery<>();
    queryInit.add(new ClassRestriction<>(getBridge(), classRef));
    queryInit.add(new FieldAbsentRestriction<>(getBridge(), FIELD_MY_STRING));
    builder.with(queryInit);
    ObjectQuery<BaseObject> query = builder.getQuery();
    assertEquals(2, query.getRestrictions().size());
    assertEquals(queryInit, query);
    queryInit.add(new ClassRestriction<>(getBridge(), classRef2));
    assertEquals("query should be cloned in with", 2, query.getRestrictions().size());
    builder.getQuery().add(new ClassRestriction<>(getBridge(), classRef2));
    assertEquals("getQuery should return a clone", 2, query.getRestrictions().size());
  }

  @Test
  public void test_with_null() throws Exception {
    new ExceptionAsserter<NullPointerException>(NullPointerException.class) {

      @Override
      protected void execute() throws NullPointerException {
        newBuilder().with(null);
      }
    }.evaluate();
  }

  private XWikiObjectBridge getBridge() {
    return (XWikiObjectBridge) Utils.getComponent(ObjectBridge.class, XWikiObjectBridge.NAME);
  }

}
