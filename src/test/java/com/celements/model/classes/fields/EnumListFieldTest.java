package com.celements.model.classes.fields;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.fields.list.EnumListField;
import com.celements.model.util.ClassFieldValue;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StaticListClass;
import com.xpn.xwiki.web.Utils;

public class EnumListFieldTest extends AbstractComponentTest {

  private enum TestEnum {
    A, B, C, D;
  }

  private EnumListField<TestEnum> field;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    field = new EnumListField<>(new DocumentReference("wiki", "class", "any"), "name",
        TestEnum.class);
  }

  @Test
  public void test_getXField() throws Exception {
    assertTrue(field.getXField() instanceof StaticListClass);
    StaticListClass xField = (StaticListClass) field.getXField();
    assertEquals("A|B|C|D", xField.getValues());
  }

  @Test
  public void test_resolve_serialize() throws Exception {
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(field.getClassRef());
    TestEnum value = TestEnum.B;
    field.setMultiSelect(false);

    BaseClass bClass = expectNewBaseObject(field.getClassRef());
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, Arrays.asList(value)));
    List<TestEnum> ret = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertEquals(Arrays.asList(value), ret);
    assertEquals(value.name(), modelAccess.getXObject(doc, field.getClassRef()).getStringValue(
        field.getName()));
  }

  @Test
  public void test_resolve_serialize_multiselect() throws Exception {
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(field.getClassRef());
    List<TestEnum> value = Arrays.asList(TestEnum.B, TestEnum.D);
    field.setMultiSelect(true);

    BaseClass bClass = expectNewBaseObject(field.getClassRef());
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, value));
    List<TestEnum> ret = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertEquals(value, ret);
    assertEquals(Arrays.asList("B", "D"), modelAccess.getXObject(doc,
        field.getClassRef()).getListValue(field.getName()));
  }

}
