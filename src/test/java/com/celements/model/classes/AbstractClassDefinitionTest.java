package com.celements.model.classes;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class AbstractClassDefinitionTest extends AbstractComponentTest {

  private ClassDefinition testClass;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    testClass = Utils.getComponent(ClassDefinition.class, TestClassDefinition.NAME);
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

}
