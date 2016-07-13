package com.celements.model.classes.fields;

import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.fields.ref.AttachmentReferenceField;
import com.celements.model.classes.fields.ref.DocumentReferenceField;
import com.celements.model.classes.fields.ref.EntityReferenceField;
import com.celements.model.classes.fields.ref.SpaceReferenceField;
import com.celements.model.classes.fields.ref.WikiReferenceField;
import com.xpn.xwiki.objects.classes.StringClass;

public class EntityReferenceFieldTest extends AbstractComponentTest {

  private EntityReferenceField<DocumentReference> field;

  Integer size = 5;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    field = new DocumentReferenceField.Builder(new DocumentReference("wiki", "class", "any"),
        "name").size(size).build();
  }

  @Test
  public void test_immutability() {
    assertImmutable(WikiReferenceField.class);
    assertImmutable(SpaceReferenceField.class);
    assertImmutable(DocumentReferenceField.class);
    assertImmutable(AttachmentReferenceField.class);
  }

  @Test
  public void test_getters() throws Exception {
    assertEquals(size, field.getSize());
  }

  @Test
  public void test_getXField() throws Exception {
    assertTrue(field.getXField() instanceof StringClass);
    StringClass xField = (StringClass) field.getXField();
    assertEquals(size, (Integer) xField.getSize());
  }

  @Test
  public void test_serialize() throws Exception {
    assertEquals("wiki:class.any", field.serialize(field.getClassRef()));
  }

  @Test
  public void test_resolve() throws Exception {
    assertEquals(field.getClassRef(), field.resolve("wiki:class.any"));
  }

}
