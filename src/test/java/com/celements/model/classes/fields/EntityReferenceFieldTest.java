package com.celements.model.classes.fields;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.EntityReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.TestClassDefinition;
import com.celements.model.classes.fields.ref.EntityReferenceField;
import com.celements.model.classes.fields.ref.ReferenceField;

public class EntityReferenceFieldTest extends AbstractComponentTest {

  private ReferenceField<EntityReference> field;

  @Before
  public void prepareTest() throws Exception {
    field = new EntityReferenceField.Builder(TestClassDefinition.NAME, "name").build();
  }

  @Test
  public void test_immutability() {
    assertImmutable(EntityReferenceField.class);
  }

  @Test
  public void test_resolve() throws Exception {
    assertEquals(field.getClassDef().getClassRef(), field.resolve(getClassDefFN()));
  }

  private String getClassDefFN() {
    return getContext().getDatabase() + ":" + field.getClassDef().toString();
  }

}
