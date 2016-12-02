package com.celements.model.classes.fields;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.TestClassDefinition;
import com.celements.model.classes.fields.list.ListOfGroupsField;
import com.xpn.xwiki.objects.classes.GroupsClass;

public class ListOfGroupsFieldTest extends AbstractComponentTest {

  private ListOfGroupsField field;

  @Before
  public void prepareTest() throws Exception {
  }

  @Test
  public void test_getters_null() throws Exception {
    field = new ListOfGroupsField.Builder(TestClassDefinition.NAME, "name").build();
    assertNull(field.getUsesList());
  }

  @Test
  public void test_getters() throws Exception {
    field = new ListOfGroupsField.Builder(TestClassDefinition.NAME, "name").usesList(false).build();
    assertFalse(field.getUsesList());
  }

  @Test
  public void test_getXField() throws Exception {
    field = new ListOfGroupsField.Builder(TestClassDefinition.NAME, "name").usesList(true).build();
    assertTrue(field.getXField() instanceof GroupsClass);
    assertTrue(field.getUsesList());
  }

}
