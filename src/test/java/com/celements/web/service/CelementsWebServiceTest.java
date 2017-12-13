package com.celements.web.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.web.classes.oldcore.IOldCoreClassDef;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;

public class CelementsWebServiceTest extends AbstractComponentTest {

  private CelementsWebService service;
  private IModelAccessFacade modelAccessMock;
  private IOldCoreClassDef rightsClass;

  @Before
  public void prepareTest() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    rightsClass = getComponentManager().lookup(IOldCoreClassDef.class,
        XWikiRightsClass.CLASS_DEF_HINT);
    service = (CelementsWebService) getComponentManager().lookup(ICelementsWebServiceRole.class);
  }

  @Test
  public void testSetRightsOnUserDoc_twoObjects() throws Exception {
    String docName = "Space.Doc";
    String defaultLevels = "view,edit,delete";
    String adminGroup = "XWiki.XWikiAdminGroup";
    List<BaseObject> twoObj = new ArrayList<>();
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject obj0 = new BaseObject();
    obj0.setXClassReference(rightsClass.getClassRef());
    BaseObject obj1 = new BaseObject();
    obj1.setXClassReference(rightsClass.getClassRef());
    obj1.setStringValue(XWikiRightsClass.FIELD_GROUPS.getName(), "XWiki.XWikiAllGroup");
    doc.addXObject(obj0);
    doc.addXObject(obj1);
    twoObj.add(obj0);
    twoObj.add(obj1);
    BaseClass bClass = new BaseClass();
    bClass.addBooleanField(XWikiRightsClass.FIELD_ALLOW.getName(), "Allow", "yesno");
    bClass.addLevelsField(XWikiRightsClass.FIELD_LEVELS.getName(), "Levels");
    bClass.addUsersField(XWikiRightsClass.FIELD_USERS.getName(), "Users");
    bClass.addGroupsField(XWikiRightsClass.FIELD_GROUPS.getName(), "Groups");
    expect(getWikiMock().getXClass(eq(rightsClass.getClassRef()), same(getContext()))).andReturn(
        bClass).anyTimes();
    expect(modelAccessMock.getDocument(eq(docRef))).andReturn(doc);
    expect(modelAccessMock.getXObjects(doc, rightsClass.getClassRef())).andReturn(twoObj);
    modelAccessMock.saveDocument(same(doc), (String) anyObject());
    expectLastCall();
    replayDefault();
    service.setRightsOnUserDoc(docName);
    verifyDefault();
    assertEquals(1, obj0.getIntValue(XWikiRightsClass.FIELD_ALLOW.getName()));
    assertEquals(defaultLevels, obj0.getStringValue(XWikiRightsClass.FIELD_LEVELS.getName()));
    assertEquals(docName, obj0.getStringValue(XWikiRightsClass.FIELD_USERS.getName()));
    assertEquals("", obj0.getStringValue(XWikiRightsClass.FIELD_GROUPS.getName()));
    assertEquals(1, obj1.getIntValue(XWikiRightsClass.FIELD_ALLOW.getName()));
    assertEquals(defaultLevels, obj1.getStringValue(XWikiRightsClass.FIELD_LEVELS.getName()));
    assertEquals("", obj1.getStringValue(XWikiRightsClass.FIELD_USERS.getName()));
    assertEquals(adminGroup, obj1.getStringValue(XWikiRightsClass.FIELD_GROUPS.getName()));
  }

  @Test
  public void testSetRightsOnUserDoc_manyObjects() throws Exception {
    String docName = "Space.Doc";
    String defaultLevels = "view,edit,delete";
    String adminGroup = "XWiki.XWikiAdminGroup";
    String otherUsers = "XWiki.OtherUser,XWiki.ThirdUser";
    String otherLevels = "view,delete";
    String otherGroups = "XWiki.XWikiAllGroup";
    List<BaseObject> objListe = new ArrayList<>();
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space", "Doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject obj0 = new BaseObject();
    obj0.setXClassReference(rightsClass.getClassRef());
    BaseObject obj1 = new BaseObject();
    obj1.setXClassReference(rightsClass.getClassRef());
    obj1.setStringValue(XWikiRightsClass.FIELD_GROUPS.getName(), otherGroups);
    BaseObject obj2 = new BaseObject();
    obj2.setXClassReference(rightsClass.getClassRef());
    obj2.setStringValue(XWikiRightsClass.FIELD_LEVELS.getName(), otherLevels);
    obj2.setStringValue(XWikiRightsClass.FIELD_GROUPS.getName(), otherGroups);
    BaseObject obj3 = new BaseObject();
    obj3.setXClassReference(rightsClass.getClassRef());
    obj3.setStringValue(XWikiRightsClass.FIELD_USERS.getName(), otherUsers);
    obj3.setStringValue(XWikiRightsClass.FIELD_GROUPS.getName(), otherGroups);
    BaseObject obj4 = new BaseObject();
    obj4.setXClassReference(rightsClass.getClassRef());
    obj4.setIntValue(XWikiRightsClass.FIELD_ALLOW.getName(), 0);
    obj4.setStringValue(XWikiRightsClass.FIELD_LEVELS.getName(), otherLevels);
    obj4.setStringValue(XWikiRightsClass.FIELD_USERS.getName(), otherUsers);
    obj4.setStringValue(XWikiRightsClass.FIELD_GROUPS.getName(), otherGroups);
    doc.addXObject(obj0);
    doc.addXObject(obj1);
    doc.addXObject(obj2);
    doc.addXObject(obj3);
    doc.addXObject(obj4);
    objListe.add(obj0);
    objListe.add(obj1);
    objListe.add(obj2);
    objListe.add(obj3);
    objListe.add(obj4);
    BaseClass bClass = new BaseClass();
    bClass.addBooleanField(XWikiRightsClass.FIELD_ALLOW.getName(), "Allow", "yesno");
    bClass.addLevelsField(XWikiRightsClass.FIELD_LEVELS.getName(), "Levels");
    bClass.addUsersField(XWikiRightsClass.FIELD_USERS.getName(), "Users");
    bClass.addGroupsField(XWikiRightsClass.FIELD_GROUPS.getName(), "Groups");
    expect(getWikiMock().getXClass(eq(rightsClass.getClassRef()), same(getContext()))).andReturn(
        bClass).anyTimes();
    expect(modelAccessMock.getDocument(eq(docRef))).andReturn(doc);
    expect(modelAccessMock.getXObjects(doc, rightsClass.getClassRef())).andReturn(objListe);
    modelAccessMock.saveDocument(same(doc), (String) anyObject());
    expectLastCall();
    replayDefault();
    service.setRightsOnUserDoc(docName);
    verifyDefault();
    assertEquals(1, obj0.getIntValue(XWikiRightsClass.FIELD_ALLOW.getName()));
    assertEquals(defaultLevels, obj0.getStringValue(XWikiRightsClass.FIELD_LEVELS.getName()));
    assertEquals(docName, obj0.getStringValue(XWikiRightsClass.FIELD_USERS.getName()));
    assertEquals("", obj0.getStringValue(XWikiRightsClass.FIELD_GROUPS.getName()));
    assertEquals(1, obj1.getIntValue(XWikiRightsClass.FIELD_ALLOW.getName()));
    assertEquals(defaultLevels, obj1.getStringValue(XWikiRightsClass.FIELD_LEVELS.getName()));
    assertEquals("", obj1.getStringValue(XWikiRightsClass.FIELD_USERS.getName()));
    assertEquals(adminGroup, obj1.getStringValue(XWikiRightsClass.FIELD_GROUPS.getName()));
    assertEquals(otherLevels, obj2.getStringValue(XWikiRightsClass.FIELD_LEVELS.getName()));
    assertEquals(otherGroups, obj2.getStringValue(XWikiRightsClass.FIELD_GROUPS.getName()));
    assertEquals(otherUsers, obj3.getStringValue(XWikiRightsClass.FIELD_USERS.getName()));
    assertEquals(otherGroups, obj3.getStringValue(XWikiRightsClass.FIELD_GROUPS.getName()));
    assertEquals(0, obj4.getIntValue(XWikiRightsClass.FIELD_ALLOW.getName()));
    assertEquals(otherLevels, obj4.getStringValue(XWikiRightsClass.FIELD_LEVELS.getName()));
    assertEquals(otherUsers, obj4.getStringValue(XWikiRightsClass.FIELD_USERS.getName()));
    assertEquals(otherGroups, obj4.getStringValue(XWikiRightsClass.FIELD_GROUPS.getName()));
  }

  @Test
  public void testEncodeUrlToUtf8() throws Exception {
    String url = "http://www.üparties.ch";
    replayDefault();
    String ret = service.encodeUrlToUtf8(url);
    verifyDefault();
    assertEquals("http://www.%C3%BCparties.ch", ret);
  }

  @Test
  public void testEncodeUrlToUtf8_withSlashes() throws Exception {
    String url = "http://www.üparties.ch/test1/test2";
    replayDefault();
    String ret = service.encodeUrlToUtf8(url);
    verifyDefault();
    assertEquals("http://www.%C3%BCparties.ch/test1/test2", ret);
  }

}
