package com.celements.model.util;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.TestClassDefinition;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.web.Utils;

public class DefaultXObjectUpdateServiceTest extends AbstractComponentTest {

  private IXObjectUpdateRole xObjUpdateService;
  private XWikiDocument doc;
  private DocumentReference classRef;

  private IModelAccessFacade modelAccessMock;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    init(false);
    doc = new XWikiDocument(new DocumentReference("xwikidb", "space", "doc"));
    classRef = new DocumentReference("xwikidb", "class", "any");
  }

  private void init(boolean withModelAccessMock) throws ComponentRepositoryException {
    if (withModelAccessMock) {
      modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    }
    ComponentDescriptor<IXObjectUpdateRole> descr = Utils.getComponentManager().getComponentDescriptor(
        IXObjectUpdateRole.class, "default");
    Utils.getComponentManager().registerComponent(descr);
    xObjUpdateService = Utils.getComponent(IXObjectUpdateRole.class);
  }

  @Test
  public void test_updateFromMap() throws Exception {
    Map<String, Object> fieldMap = new HashMap<>();
    String fieldName = "someField";
    String value = "asdf";
    fieldMap.put(getWebUtils().serializeRef(classRef, true) + "." + fieldName, value);
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    doc.addXObject(obj);

    expectPropertyClass(classRef, fieldName, new StringClass());

    replayDefault();
    assertTrue(xObjUpdateService.updateFromMap(doc, fieldMap));
    verifyDefault();

    assertEquals(1, doc.getXObjects(classRef).size());
    assertEquals(value, obj.getStringValue(fieldName));
  }

  @Test
  public void test_updateFromMap_newObj() throws Exception {
    Map<String, Object> fieldMap = new HashMap<>();
    String fieldName = "someField";
    String value = "asdf";
    fieldMap.put(getWebUtils().serializeRef(classRef, true) + "." + fieldName, value);

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, fieldName, new StringClass());

    replayDefault();
    assertTrue(xObjUpdateService.updateFromMap(doc, fieldMap));
    verifyDefault();

    assertEquals(1, doc.getXObjects(classRef).size());
    assertEquals(value, doc.getXObject(classRef).getStringValue(fieldName));
  }

  @Test
  public void test_updateFromMap_aleadySet() throws Exception {
    Map<String, Object> fieldMap = new HashMap<>();
    String fieldName = "someField";
    String value = "asdf";
    fieldMap.put(getWebUtils().serializeRef(classRef, true) + "." + fieldName, value);
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    obj.setStringValue(fieldName, value);
    doc.addXObject(obj);

    replayDefault();
    assertFalse(xObjUpdateService.updateFromMap(doc, fieldMap));
    verifyDefault();

    assertEquals(1, doc.getXObjects(classRef).size());
    assertEquals(value, obj.getStringValue(fieldName));
  }

  @Test
  public void test_updateFromMap_empty() throws Exception {
    Map<String, Object> fieldMap = new HashMap<>();

    replayDefault();
    assertFalse(xObjUpdateService.updateFromMap(doc, fieldMap));
    verifyDefault();

    assertNull(doc.getXObject(classRef));
  }

  @Test
  public void test_updateFromMap_invalidClassName() throws Exception {
    Map<String, Object> fieldMap = new HashMap<>();
    fieldMap.put("invalidString", "asdf");

    replayDefault();
    try {
      xObjUpdateService.updateFromMap(doc, fieldMap);
      fail("should throw IllegalArgumentException, invalid fieldMap entry");
    } catch (IllegalArgumentException iae) {
      // expected
    }
    verifyDefault();

    assertNull(doc.getXObject(classRef));
  }

  @Test
  public void test_update_empty() throws Exception {
    Set<ClassFieldValue<?>> fieldValues = new HashSet<>();

    replayDefault();
    assertFalse(xObjUpdateService.update(doc, fieldValues));
    verifyDefault();
  }

  @Test
  public void test_update() throws Exception {
    init(true);
    ClassFieldValue<String> fieldVal1 = new ClassFieldValue<>(TestClassDefinition.FIELD_MY_STRING,
        "val");
    ClassFieldValue<Boolean> fieldVal2 = new ClassFieldValue<>(TestClassDefinition.FIELD_MY_BOOL,
        true);
    ClassFieldValue<Integer> fieldVal3 = new ClassFieldValue<>(TestClassDefinition.FIELD_MY_INT, 1);

    expect(modelAccessMock.getProperty(same(doc), eq(fieldVal1.getField()))).andReturn(null).once();
    expect(modelAccessMock.setProperty(same(doc), eq(fieldVal1))).andReturn(true).once();
    expect(modelAccessMock.getProperty(same(doc), eq(fieldVal2.getField()))).andReturn(
        false).once();
    expect(modelAccessMock.setProperty(same(doc), eq(fieldVal2))).andReturn(true).once();
    expect(modelAccessMock.getProperty(same(doc), eq(fieldVal3.getField()))).andReturn(1).once();

    replayDefault();
    assertTrue(xObjUpdateService.update(doc, ImmutableSet.<ClassFieldValue<?>>of(fieldVal1,
        fieldVal2, fieldVal3)));
    verifyDefault();
  }

  private IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
