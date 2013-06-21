package com.celements.rights;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.rights.CelementsRightServiceImpl;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.user.api.XWikiRightNotFoundException;

public class CelementsRightServiceImplTest extends AbstractBridgedComponentTestCase {
  CelementsRightServiceImpl rightService;
  XWiki xwiki;

  @Before
  public void setUp_CelementsRightServiceImplTest() throws Exception {
    rightService = new CelementsRightServiceImpl();
    xwiki = createMock(XWiki.class);
    getContext().setWiki(xwiki);
  }

  @Test
  public void testCheckRight_publishNotActive() throws XWikiRightNotFoundException, 
      XWikiException {
    XWikiGroupService gs = createMock(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(getContext()))).andReturn(gs).anyTimes();
    Collection<DocumentReference> groupsList = new ArrayList<DocumentReference>();
    groupsList.add(new DocumentReference(getContext().getDatabase(), "XWiki", 
        "XWikiAllGroup"));
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext(
        ).getDatabase(), "XWiki", "user")), eq(0), eq(0), same(getContext()))
        ).andReturn(groupsList).anyTimes();
    Collection<DocumentReference> emptyGroupsList = Collections.emptyList();
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext(
        ).getDatabase(), "XWiki", "XWikiAllGroup")), eq(0), eq(0), same(getContext()))
        ).andReturn(emptyGroupsList).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    expect(xwiki.getWebPreferenceAsInt(eq("publishdate_active"), eq(-1), same(getContext(
        )))).andReturn(0).anyTimes();
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(
        ), "TestSpace", "TestDoc"));
    BaseObject rightObj = new BaseObject();
    rightObj.setXClassReference(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiRights"));
    rightObj.setStringValue("users", "XWiki.user");
    rightObj.setStringValue("levels", "view,edit");
    rightObj.setIntValue("allow", 1);
    doc.addXObject(rightObj);
    replay(gs, xwiki);
    assertTrue(rightService.checkRight("XWiki.user", doc, "view", true, true, false, 
        getContext()));
    verify(gs, xwiki);
  }

  @Test
  public void testCheckRight_publishActive_defaultNoObject(
      ) throws XWikiRightNotFoundException, XWikiException {
    XWikiGroupService gs = createMock(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(getContext()))).andReturn(gs).anyTimes();
    Collection<DocumentReference> groupsList = new ArrayList<DocumentReference>();
    groupsList.add(new DocumentReference(getContext().getDatabase(), "XWiki", 
        "XWikiAllGroup"));
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext(
        ).getDatabase(), "XWiki", "user")), eq(0), eq(0), same(getContext()))
        ).andReturn(groupsList).anyTimes();
    Collection<DocumentReference> emptyGroupsList = Collections.emptyList();
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext(
        ).getDatabase(), "XWiki", "XWikiAllGroup")), eq(0), eq(0), same(getContext()))
        ).andReturn(emptyGroupsList).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    expect(xwiki.getWebPreferenceAsInt(eq("publishdate_active"), eq(-1), same(getContext(
        )))).andReturn(1).anyTimes();
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(
        ), "TestSpace", "TestDoc"));
    BaseObject rightObj = new BaseObject();
    rightObj.setXClassReference(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiRights"));
    rightObj.setStringValue("users", "XWiki.user");
    rightObj.setStringValue("levels", "view");
    rightObj.setIntValue("allow", 1);
    doc.addXObject(rightObj);
    replay(gs, xwiki);
    assertTrue(rightService.checkRight("XWiki.user", doc, "view", true, true, false, 
        getContext()));
    verify(gs, xwiki);
  }

  @Test
  public void testCheckRight_publishActive_unpublished(
      ) throws XWikiRightNotFoundException, XWikiException {
    XWikiGroupService gs = createMock(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(getContext()))).andReturn(gs).anyTimes();
    Collection<DocumentReference> groupsList = new ArrayList<DocumentReference>();
    groupsList.add(new DocumentReference(getContext().getDatabase(), "XWiki", 
        "XWikiAllGroup"));
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext(
        ).getDatabase(), "XWiki", "user")), eq(0), eq(0), same(getContext()))
        ).andReturn(groupsList).anyTimes();
    Collection<DocumentReference> emptyGroupsList = Collections.emptyList();
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext(
        ).getDatabase(), "XWiki", "XWikiAllGroup")), eq(0), eq(0), same(getContext()))
        ).andReturn(emptyGroupsList).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    expect(xwiki.getWebPreferenceAsInt(eq("publishdate_active"), eq(-1), same(getContext(
        )))).andReturn(1).anyTimes();
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(
        ), "TestSpace", "TestDoc"));
    BaseObject rightObj = new BaseObject();
    rightObj.setXClassReference(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiRights"));
    rightObj.setStringValue("users", "XWiki.user");
    rightObj.setStringValue("levels", "view");
    rightObj.setIntValue("allow", 1);
    doc.addXObject(rightObj);
    Calendar gc = GregorianCalendar.getInstance();
    BaseObject obj = new BaseObject();
    gc.add(GregorianCalendar.HOUR, 1);
    obj.setDateValue("publishDate", gc.getTime());
    obj.setXClassReference(rightService.getPublicationClassReference());
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
    Collection<DocumentReference> groupsList = new ArrayList<DocumentReference>();
    groupsList.add(new DocumentReference(getContext().getDatabase(), "XWiki", 
        "XWikiAllGroup"));
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext(
        ).getDatabase(), "XWiki", "user")), eq(0), eq(0), same(getContext()))
        ).andReturn(groupsList).anyTimes();
    Collection<DocumentReference> emptyGroupsList = Collections.emptyList();
    expect(gs.getAllGroupsReferencesForMember(eq(new DocumentReference(getContext(
        ).getDatabase(), "XWiki", "XWikiAllGroup")), eq(0), eq(0), same(getContext()))
        ).andReturn(emptyGroupsList).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    expect(xwiki.getWebPreferenceAsInt(eq("publishdate_active"), eq(-1), same(getContext(
        )))).andReturn(1).anyTimes();
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(
        ), "TestSpace", "TestDoc"));
    BaseObject rightObj = new BaseObject();
    rightObj.setXClassReference(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiRights"));
    rightObj.setStringValue("users", "XWiki.user");
    rightObj.setStringValue("levels", "view");
    rightObj.setIntValue("allow", 1);
    doc.addXObject(rightObj);
    Calendar gc = GregorianCalendar.getInstance();
    BaseObject obj = new BaseObject();
    gc.add(GregorianCalendar.HOUR, -1);
    obj.setDateValue("publishDate", gc.getTime());
    gc.add(GregorianCalendar.HOUR, 1);
    gc.add(GregorianCalendar.HOUR, 1);
    obj.setDateValue("unpublishDate", gc.getTime());
    obj.setXClassReference(rightService.getPublicationClassReference());
    doc.addXObject(obj);
    replay(gs, xwiki);
    assertTrue(rightService.checkRight("XWiki.user", doc, "view", true, true, false, 
        getContext()));
    verify(gs, xwiki);
  }

  @Test
  public void testGetPublishObject_null() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(
        ), "Space", "Doc"));
    assertNull(rightService.getPublishObject(doc));
  }

  @Test
  public void testGetPublishObject_hasObj() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(
        ), "Space", "Doc"));
    BaseObject obj = new BaseObject();
    obj.setXClassReference(rightService.getPublicationClassReference());
    doc.addXObject(obj);
    assertEquals(obj, rightService.getPublishObject(doc));
  }

  @Test
  public void testIsPublishActive_notSet() {
    expect(xwiki.getWebPreferenceAsInt(eq("publishdate_active"), eq(-1), same(getContext(
        )))).andReturn(-1).once();
    expect(xwiki.getXWikiPreferenceAsInt(eq("publishdate_active"), 
        eq("celements.publishdate.active"), eq(0), same(getContext()))).andReturn(0
        ).once();
    replay(xwiki);
    assertEquals(false, rightService.isPublishActive(getContext()));
    verify(xwiki);
  }

  @Test
  public void testIsPublishActive_false() {
    expect(xwiki.getWebPreferenceAsInt(eq("publishdate_active"), eq(-1), same(getContext(
        )))).andReturn(0).once();
    replay(xwiki);
    assertEquals(false, rightService.isPublishActive(getContext()));
    verify(xwiki);
  }

  @Test
  public void testIsPublishActive_true() {
    expect(xwiki.getWebPreferenceAsInt(eq("publishdate_active"), eq(-1), same(getContext(
        )))).andReturn(1).once();
    replay(xwiki);
    assertEquals(true, rightService.isPublishActive(getContext()));
    verify(xwiki);
  }

  @Test
  public void testIsRestrictedRightsAction_view() {
    assertTrue(rightService.isRestrictedRightsAction("view"));
  }

  @Test
  public void testIsRestrictedRightsAction_comment() {
    assertTrue(rightService.isRestrictedRightsAction("comment"));
  }

  @Test
  public void testIsRestrictedRightsAction_admin() {
    assertFalse(rightService.isRestrictedRightsAction("admin"));
  }

  @Test
  public void testIsAfterStart_empty() {
    assertEquals(true, rightService.isAfterStart(new BaseObject()));
  }

  @Test
  public void testIsAfterStart_beforeStart() {
    BaseObject obj = new BaseObject();
    Calendar gc = GregorianCalendar.getInstance();
    gc.add(GregorianCalendar.HOUR, 1);
    obj.setDateValue("publishDate", gc.getTime());
    assertFalse(rightService.isAfterStart(obj));
  }

  @Test
  public void testIsAfterStart_afterStart() {
    BaseObject obj = new BaseObject();
    Calendar gc = GregorianCalendar.getInstance();
    gc.add(GregorianCalendar.HOUR, -1);
    obj.setDateValue("publishDate", gc.getTime());
    assertTrue(rightService.isAfterStart(obj));
  }

  @Test
  public void testIsBeforeEnd_empty() {
    assertEquals(true, rightService.isBeforeEnd(new BaseObject()));
  }

  @Test
  public void testIsBeforeEnd_beforeEnd() {
    BaseObject obj = new BaseObject();
    Calendar gc = GregorianCalendar.getInstance();
    gc.add(GregorianCalendar.HOUR, 1);
    obj.setDateValue("unpublishDate", gc.getTime());
    assertTrue(rightService.isBeforeEnd(obj));
  }

  @Test
  public void testIsBeforeEnd_afterEnd() {
    BaseObject obj = new BaseObject();
    Calendar gc = GregorianCalendar.getInstance();
    gc.add(GregorianCalendar.HOUR, -1);
    obj.setDateValue("unpublishDate", gc.getTime());
    assertFalse(rightService.isBeforeEnd(obj));
  }
}
