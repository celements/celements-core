package com.celements.rights;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.test.AbstractComponentTest;
import com.celements.web.classcollections.DocumentDetailsClasses;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.user.api.XWikiRightNotFoundException;
import com.xpn.xwiki.web.Utils;

public class CelementsRightServiceImplTest extends AbstractComponentTest {

  CelementsRightServiceImpl rightService;
  XWiki xwiki;

  @Before
  public void setUp_CelementsRightServiceImplTest() throws Exception {
    rightService = new CelementsRightServiceImpl();
    xwiki = createMock(XWiki.class);
    getContext().setWiki(xwiki);
  }

  @Test
  public void testCheckRight_publishNotActive() throws XWikiRightNotFoundException, XWikiException {
    XWikiGroupService gs = createMock(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(getContext()))).andReturn(gs).anyTimes();
    Collection<DocumentReference> groupsList = new ArrayList<>();
    groupsList.add(new DocumentReference(getContext().getDatabase(), "XWiki", "XWikiAllGroup"));
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext().getDatabase(),
        "XWiki", "user")), eq(0), eq(0), same(getContext()))).andReturn(groupsList).anyTimes();
    Collection<DocumentReference> emptyGroupsList = Collections.emptyList();
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext().getDatabase(),
        "XWiki", "XWikiAllGroup")), eq(0), eq(0), same(getContext()))).andReturn(
            emptyGroupsList).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(),
        "TestSpace", "TestDoc"));
    getContext().setDoc(doc);
    expect(xwiki.getSpacePreference(eq("publishdate_active"), eq("TestSpace"), eq("-1"), same(
        getContext()))).andReturn("0").anyTimes();
    BaseObject rightObj = new BaseObject();
    rightObj.setXClassReference(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiRights"));
    rightObj.setStringValue("users", "XWiki.user");
    rightObj.setStringValue("levels", "view,edit");
    rightObj.setIntValue("allow", 1);
    doc.addXObject(rightObj);
    replay(gs, xwiki);
    assertTrue(rightService.checkRight("XWiki.user", doc, "view", true, true, false, getContext()));
    verify(gs, xwiki);
  }

  @Test
  public void testCheckRight_publishActive_defaultNoObject() throws XWikiRightNotFoundException,
      XWikiException {
    XWikiGroupService gs = createMock(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(getContext()))).andReturn(gs).anyTimes();
    Collection<DocumentReference> groupsList = new ArrayList<>();
    groupsList.add(new DocumentReference(getContext().getDatabase(), "XWiki", "XWikiAllGroup"));
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext().getDatabase(),
        "XWiki", "user")), eq(0), eq(0), same(getContext()))).andReturn(groupsList).anyTimes();
    Collection<DocumentReference> emptyGroupsList = Collections.emptyList();
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext().getDatabase(),
        "XWiki", "XWikiAllGroup")), eq(0), eq(0), same(getContext()))).andReturn(
            emptyGroupsList).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    expect(xwiki.getSpacePreference(eq("publishdate_active"), eq("TestSpace"), eq("-1"), same(
        getContext()))).andReturn("1").anyTimes();
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(),
        "TestSpace", "TestDoc"));
    getContext().setDoc(doc);
    BaseObject rightObj = new BaseObject();
    rightObj.setXClassReference(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiRights"));
    rightObj.setStringValue("users", "XWiki.user");
    rightObj.setStringValue("levels", "view");
    rightObj.setIntValue("allow", 1);
    doc.addXObject(rightObj);
    replay(gs, xwiki);
    assertTrue(rightService.checkRight("XWiki.user", doc, "view", true, true, false, getContext()));
    verify(gs, xwiki);
  }

  @Test
  public void testCheckRight_publishActive_unpublished() throws XWikiRightNotFoundException,
      XWikiException {
    XWikiGroupService gs = createMock(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(getContext()))).andReturn(gs).anyTimes();
    Collection<DocumentReference> groupsList = new ArrayList<>();
    groupsList.add(new DocumentReference(getContext().getDatabase(), "XWiki", "XWikiAllGroup"));
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext().getDatabase(),
        "XWiki", "user")), eq(0), eq(0), same(getContext()))).andReturn(groupsList).anyTimes();
    Collection<DocumentReference> emptyGroupsList = Collections.emptyList();
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext().getDatabase(),
        "XWiki", "XWikiAllGroup")), eq(0), eq(0), same(getContext()))).andReturn(
            emptyGroupsList).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    expect(xwiki.getSpacePreference(eq("publishdate_active"), eq("TestSpace"), eq("-1"), same(
        getContext()))).andReturn("1").anyTimes();
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(),
        "TestSpace", "TestDoc"));
    getContext().setDoc(doc);
    BaseObject rightObj = new BaseObject();
    rightObj.setXClassReference(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiRights"));
    rightObj.setStringValue("users", "XWiki.user");
    rightObj.setStringValue("levels", "view");
    rightObj.setIntValue("allow", 1);
    doc.addXObject(rightObj);
    Calendar gc = Calendar.getInstance();
    BaseObject obj = new BaseObject();
    gc.add(Calendar.HOUR, 1);
    obj.setDateValue("publishDate", gc.getTime());
    obj.setXClassReference(getPublicationClassReference());
    doc.addXObject(obj);
    replay(gs, xwiki);
    assertFalse(rightService.checkRight("XWiki.user", doc, "view", true, true, false,
        getContext()));
    verify(gs, xwiki);
  }

  @Test
  public void testCheckRight_publishActive_published() throws XWikiRightNotFoundException,
      XWikiException {
    XWikiGroupService gs = createMock(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(getContext()))).andReturn(gs).anyTimes();
    Collection<DocumentReference> groupsList = new ArrayList<>();
    groupsList.add(new DocumentReference(getContext().getDatabase(), "XWiki", "XWikiAllGroup"));
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext().getDatabase(),
        "XWiki", "user")), eq(0), eq(0), same(getContext()))).andReturn(groupsList).anyTimes();
    Collection<DocumentReference> emptyGroupsList = Collections.emptyList();
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext().getDatabase(),
        "XWiki", "XWikiAllGroup")), eq(0), eq(0), same(getContext()))).andReturn(
            emptyGroupsList).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    expect(xwiki.getSpacePreference(eq("publishdate_active"), eq("TestSpace"), eq("-1"), same(
        getContext()))).andReturn("1").anyTimes();
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(),
        "TestSpace", "TestDoc"));
    getContext().setDoc(doc);
    BaseObject rightObj = new BaseObject();
    rightObj.setXClassReference(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiRights"));
    rightObj.setStringValue("users", "XWiki.user");
    rightObj.setStringValue("levels", "view");
    rightObj.setIntValue("allow", 1);
    doc.addXObject(rightObj);
    Calendar gc = Calendar.getInstance();
    BaseObject obj = new BaseObject();
    gc.add(Calendar.HOUR, -1);
    obj.setDateValue("publishDate", gc.getTime());
    gc.add(Calendar.HOUR, 1);
    gc.add(Calendar.HOUR, 1);
    obj.setDateValue("unpublishDate", gc.getTime());
    obj.setXClassReference(getPublicationClassReference());
    doc.addXObject(obj);
    replay(gs, xwiki);
    assertTrue(rightService.checkRight("XWiki.user", doc, "view", true, true, false, getContext()));
    verify(gs, xwiki);
  }

  private DocumentReference getPublicationClassReference() {
    return ((DocumentDetailsClasses) Utils.getComponent(IClassCollectionRole.class,
        DocumentDetailsClasses.NAME)).getDocumentPublicationClassRef(getContext().getDatabase());
  }

}
