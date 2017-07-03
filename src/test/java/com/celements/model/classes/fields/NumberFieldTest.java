package com.celements.model.classes.fields;

import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.AllowedReason.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.TestClassDefinition;
import com.celements.model.classes.fields.number.ByteField;
import com.celements.model.classes.fields.number.DoubleField;
import com.celements.model.classes.fields.number.FloatField;
import com.celements.model.classes.fields.number.IntField;
import com.celements.model.classes.fields.number.LongField;
import com.celements.model.classes.fields.number.NumberField;
import com.celements.model.classes.fields.number.ShortField;
import com.xpn.xwiki.objects.classes.NumberClass;

public class NumberFieldTest extends AbstractComponentTest {

  // test static definition
  private static final ClassField<Integer> STATIC_DEFINITION = new IntField.Builder(
      TestClassDefinition.NAME, "name").build();

  private NumberField<Integer> field;

  Integer size = 5;

  @Before
  public void prepareTest() throws Exception {
    assertNotNull(STATIC_DEFINITION);
    field = new IntField.Builder(TestClassDefinition.NAME, "name").size(size).build();
  }

  @Test
  public void test_immutability() {
    assertInstancesOf(NumberField.class, areImmutable(), allowingForSubclassing());
    assertImmutable(DoubleField.class);
    assertImmutable(FloatField.class);
    assertImmutable(LongField.class);
    assertImmutable(IntField.class);
    assertImmutable(ShortField.class);
    assertImmutable(ByteField.class);
  }

  @Test
  public void test_getters() throws Exception {
    assertEquals(size, field.getSize());
  }

  @Test
  public void test_getXField() throws Exception {
    assertTrue(field.getXField() instanceof NumberClass);
    NumberClass xField = (NumberClass) field.getXField();
    assertEquals(size, (Integer) xField.getSize());
  }

}
