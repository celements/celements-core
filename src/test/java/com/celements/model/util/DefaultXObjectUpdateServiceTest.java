package com.celements.model.util;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.model.access.IModelAccessFacade;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class DefaultXObjectUpdateServiceTest extends AbstractBridgedComponentTestCase {

  private DefaultXObjectUpdateService xObjUpdateService;
  private XWikiDocument doc;
  private DocumentReference classRef;
  
  private IModelAccessFacade accessModelMock;

  @Before
  public void setUp_DefaultXObjectUpdateServiceTest() throws Exception {
    accessModelMock = registerComponentMock(IModelAccessFacade.class);
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
    
    expect(accessModelMock.getOrCreateXObject(same(doc), eq(classRef))).andReturn(obj
        ).once();
    expect(accessModelMock.getProperty(same(obj), eq(fieldName))).andReturn("").once();
    accessModelMock.setProperty(same(obj), eq(fieldName), eq(value));
    expectLastCall().once();
    
    replayDefault();
    assertTrue(xObjUpdateService.updateFromMap(doc, fieldMap));
    verifyDefault();
  }

  @Test
  public void test_updateFromMap_aleadySet() throws Exception {
    Map<String, Object> fieldMap = new HashMap<>();
    String fieldName = "someField";
    String value = "asdf";
    fieldMap.put(getWebUtils().serializeRef(classRef, true) + "." + fieldName, value);
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    doc.addXObject(obj);
    
    expect(accessModelMock.getOrCreateXObject(same(doc), eq(classRef))).andReturn(obj
        ).once();
    expect(accessModelMock.getProperty(same(obj), eq(fieldName))).andReturn(value).once();
    
    replayDefault();
    assertFalse(xObjUpdateService.updateFromMap(doc, fieldMap));
    verifyDefault();
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
    
    expect(accessModelMock.getOrCreateXObject(same(doc), eq(classRef))).andReturn(obj
        ).once();
    expect(accessModelMock.getProperty(same(obj), eq(fieldName))).andReturn("").once();
    accessModelMock.setProperty(same(obj), eq(fieldName), eq(value));
    expectLastCall().once();
    accessModelMock.saveDocument(same(doc), eq("updated fields"), eq(true));
    expectLastCall().once();
    
    replayDefault();
    assertTrue(xObjUpdateService.updateFromMapAndSave(doc, fieldMap));
    verifyDefault();
  }

  @Test
  public void test_updateFromMapAndSave_empty() throws Exception {
    Map<String, Object> fieldMap = new HashMap<>();
    
    replayDefault();
    assertFalse(xObjUpdateService.updateFromMapAndSave(doc, fieldMap));
    verifyDefault();
  }
  
  private IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }
  
}
