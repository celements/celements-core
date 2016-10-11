package com.celements.model.classes;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;
import com.xpn.xwiki.web.Utils;

public class AbstractClassDefinitionTest extends AbstractComponentTest {

  private ClassDefinition testClass;

  @Before
  public void prepareTest() throws Exception {
    testClass = Utils.getComponent(ClassDefinition.class, TestClassDefinition.NAME);
  }

  @Test
  public void test_getClassRef() throws Exception {
    assertEquals(new DocumentReference(getContext().getDatabase(), "Classes", "TestClass"),
        testClass.getClassRef());
    WikiReference wiki = new WikiReference("asdf");
    assertEquals(new DocumentReference(wiki.getName(), "Classes", "TestClass"),
        testClass.getClassRef(wiki));
  }

  @Test
  public void test_getClassRef_cloneWiki_setChild() throws Exception {
    DocumentReference docRef = new DocumentReference("asdf", "Classes", "myDocName");
    SpaceReference origSpaceRef = (SpaceReference) docRef.extractReference(EntityType.SPACE);
    DocumentReference classRef = testClass.getClassRef(docRef.getWikiReference());
    assertEquals(new DocumentReference("asdf", "Classes", "TestClass"), classRef);
    assertEquals("myDocName", docRef.getName());
    assertSame("WikiRef must be cloned because of setChild", origSpaceRef,
        docRef.getWikiReference().getChild());
  }

  @Test
  public void test_isBlacklisted_false() throws Exception {
    replayDefault();
    assertFalse(testClass.isBlacklisted());
    verifyDefault();
  }

  @Test
  public void test_isBlacklisted_true() throws Exception {
    getConfigurationSource().setProperty(ClassDefinition.CFG_SRC_KEY, Arrays.asList("asdf",
        testClass.getName()));
    replayDefault();
    assertTrue(testClass.isBlacklisted());
    verifyDefault();
  }

  @Test
  public void test_getFields() throws Exception {
    replayDefault();
    assertEquals(4, testClass.getFields().size());
    assertTrue(testClass.getFields().contains(TestClassDefinition.FIELD_MY_BOOL));
    assertTrue(testClass.getFields().contains(TestClassDefinition.FIELD_MY_DOCREF));
    assertTrue(testClass.getFields().contains(TestClassDefinition.FIELD_MY_STRING));
    assertTrue(testClass.getFields().contains(TestClassDefinition.FIELD_MY_INT));
    verifyDefault();
  }

  @Test
  public void test_getField() throws Exception {
    replayDefault();
    Optional<ClassField<?>> field = testClass.getField(TestClassDefinition.FIELD_MY_INT.getName());
    verifyDefault();
    assertTrue(field.isPresent());
    assertSame(TestClassDefinition.FIELD_MY_INT, field.get());
  }

  @Test
  public void test_getField_absent() throws Exception {
    replayDefault();
    Optional<ClassField<?>> field = testClass.getField("asdf");
    verifyDefault();
    assertFalse(field.isPresent());
  }

  @Test
  public void test_getField_generic() throws Exception {
    replayDefault();
    Optional<ClassField<Integer>> field = testClass.getField(
        TestClassDefinition.FIELD_MY_INT.getName(), Integer.class);
    assertTrue(field.isPresent());
    assertSame(TestClassDefinition.FIELD_MY_INT, field.get());
    verifyDefault();
  }

  @Test
  public void test_getField_generic_wrongClass() throws Exception {
    replayDefault();
    Optional<ClassField<String>> field = testClass.getField(
        TestClassDefinition.FIELD_MY_INT.getName(), String.class);
    assertFalse(field.isPresent());
    verifyDefault();
  }

  @Test
  public void test_toString() throws Exception {
    assertEquals(TestClassDefinition.SPACE_NAME + "." + TestClassDefinition.DOC_NAME,
        testClass.toString());
  }

}
