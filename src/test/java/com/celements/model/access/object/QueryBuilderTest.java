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
import com.celements.model.access.object.restriction.ClassRestriction;
import com.celements.model.access.object.restriction.FieldAbsentRestriction;
import com.celements.model.access.object.restriction.ObjectQuery;
import com.celements.model.access.object.xwiki.XWikiObjectEditor;
import com.celements.model.access.object.xwiki.XWikiObjectEditor.Builder;
import com.celements.model.access.object.xwiki.XWikiObjectFetcher;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
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

  private Builder newBuilder() {
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
        newBuilder().filter((ClassField<String>) null, null);
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
    Builder builder = newBuilder();
    builder.filter(classRef).filter(FIELD_MY_STRING, "val").filterAbsent(FIELD_MY_INT);
    assertEquals(3, builder.buildQuery().size());
    builder.filter(classRef).filter(FIELD_MY_STRING, "val").filterAbsent(FIELD_MY_INT);
    assertEquals(3, builder.buildQuery().size());
    builder.with(builder.buildQuery());
    assertEquals(3, builder.buildQuery().size());
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
    Builder builder = newBuilder();
    ObjectQuery<BaseObject> queryInit = new ObjectQuery<>();
    queryInit.add(new ClassRestriction<>(builder.getBridge(), classRef));
    queryInit.add(new FieldAbsentRestriction<>(builder.getBridge(), FIELD_MY_STRING));
    builder.with(queryInit);
    ObjectQuery<BaseObject> query = builder.buildQuery();
    assertEquals(2, query.size());
    assertEquals(queryInit, query);
    queryInit.add(new ClassRestriction<>(builder.getBridge(), classRef2));
    assertEquals("query should be cloned in with", 2, query.size());
    builder.buildQuery().add(new ClassRestriction<>(builder.getBridge(), classRef2));
    assertEquals("getQuery should return a clone", 2, query.size());
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

  @Test
  public void test_builder_immutability() throws Exception {
    Builder builder = newBuilder();
    builder.filter(classRef);
    XWikiObjectFetcher fetcher = builder.fetch();
    XWikiObjectEditor editor = builder.edit();
    builder.filter(classRef2);
    assertEquals(1, fetcher.getQuery().size());
    assertEquals(1, editor.getQuery().size());
    assertEquals(1, editor.fetch().getQuery().size());
  }

}
