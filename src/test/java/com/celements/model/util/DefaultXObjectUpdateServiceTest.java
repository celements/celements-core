package com.celements.model.util;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.web.Utils;

public class DefaultXObjectUpdateServiceTest extends AbstractBridgedComponentTestCase {

  private DefaultXObjectUpdateService xObjUpdateService;
  private XWikiDocument doc;
  private DocumentReference classRef;

  @Before
  public void setUp_DefaultXObjectUpdateServiceTest() throws Exception {
    xObjUpdateService = (DefaultXObjectUpdateService) Utils.getComponent(
        IXObjectUpdateRole.class);
    doc = new XWikiDocument(new DocumentReference("xwikidb", "space", "doc"));
    classRef = new DocumentReference("xwikidb", "class", "any");
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
    
    expectPropertyClass(this, classRef, fieldName, new StringClass());
    
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
    
    BaseClass bClass = expectNewBaseObject(this, classRef);    
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
  }

  @Test
  public void test_updateFromMapAndSave() throws Exception {
    Map<String, Object> fieldMap = new HashMap<>();
    String fieldName = "someField";
    String value = "asdf";
    fieldMap.put(getWebUtils().serializeRef(classRef, true) + "." + fieldName, value);
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    doc.addXObject(obj);
    
    expectPropertyClass(this, classRef, fieldName, new StringClass());
    getWikiMock().saveDocument(same(doc), eq("updated fields"), eq(true), 
        same(getContext()));
    expectLastCall().once();
    
    replayDefault();
    assertTrue(xObjUpdateService.updateFromMapAndSave(doc, fieldMap));
    verifyDefault();
  }

  @Test
  public void test_updateFromMapAndSave_noSave() throws Exception {
    Map<String, Object> fieldMap = new HashMap<>();
    
    replayDefault();
    assertFalse(xObjUpdateService.updateFromMapAndSave(doc, fieldMap));
    verifyDefault();
  }
  
  private IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }
  
}
