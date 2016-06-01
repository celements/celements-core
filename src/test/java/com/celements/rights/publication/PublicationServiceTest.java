package com.celements.rights.publication;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class PublicationServiceTest extends AbstractBridgedComponentTestCase {

  private PublicationService pubService;

  @Before
  public void setUp_PublicationServiceTest() throws Exception {
    pubService = (PublicationService) Utils.getComponent(IPublicationServiceRole.class);
  }

  @Test
  public void testGetPublishObject_null() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(), "Space",
        "Doc"));
    assertNotNull(pubService.getPublishObjects(doc));
  }

  @Test
  public void testGetPublishObject_hasObj() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(), "Space",
        "Doc"));
    BaseObject obj = new BaseObject();
    obj.setXClassReference(pubService.getPublicationClassReference());
    doc.addXObject(obj);
    assertEquals(obj, pubService.getPublishObjects(doc).get(0));
  }

  @Test
  public void testGetPublishObject_hasObjs() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(), "Space",
        "Doc"));
    BaseObject obj1 = new BaseObject();
    obj1.setXClassReference(pubService.getPublicationClassReference());
    doc.addXObject(obj1);
    BaseObject obj2 = new BaseObject();
    obj2.setXClassReference(pubService.getPublicationClassReference());
    doc.addXObject(obj2);
    assertEquals(2, pubService.getPublishObjects(doc).size());
  }

  @Test
  public void testGetPublishObject_nullObjs() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(), "Space",
        "Doc"));
    BaseObject obj = new BaseObject();
    obj.setXClassReference(pubService.getPublicationClassReference());
    doc.setXObject(3, obj);
    assertEquals(1, pubService.getPublishObjects(doc).size());
  }

  @Test
  public void testIsPublished_noLimits() {
    assertTrue("null document", pubService.isPublished(null));
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(), "Space",
        "Doc"));
    assertTrue("document without objects", pubService.isPublished(doc));
  }

  @Test
  public void testIsPublished_noLimits_umpublished() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(), "Space",
        "Doc"));
    BaseObject obj1 = new BaseObject();
    Calendar gc = GregorianCalendar.getInstance();
    gc.add(GregorianCalendar.HOUR, 1);
    obj1.setDateValue("publishDate", gc.getTime());
    obj1.setXClassReference(pubService.getPublicationClassReference());
    doc.addXObject(obj1);
    BaseObject obj2 = new BaseObject();
    gc.add(GregorianCalendar.HOUR, -2);
    obj2.setDateValue("unpublishDate", gc.getTime());
    gc.add(GregorianCalendar.HOUR, -2);
    obj2.setDateValue("publishDate", gc.getTime());
    obj2.setXClassReference(pubService.getPublicationClassReference());
    doc.addXObject(obj2);
    assertFalse(doc.getXObjects(pubService.getPublicationClassReference()).isEmpty());
    assertFalse(pubService.isPublished(doc));
  }

  @Test
  public void testIsPublished_noLimits_published() {
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(), "Space",
        "Doc"));
    BaseObject obj1 = new BaseObject();
    obj1.setXClassReference(pubService.getPublicationClassReference());
    BaseObject obj3 = new BaseObject();
    obj3.setXClassReference(pubService.getPublicationClassReference());
    Calendar gc = GregorianCalendar.getInstance();
    gc.add(GregorianCalendar.HOUR, 1);
    obj3.setDateValue("unpublishDate", gc.getTime());
    obj1.setDateValue("publishDate", gc.getTime());
    BaseObject obj2 = new BaseObject();
    obj2.setXClassReference(pubService.getPublicationClassReference());
    gc.add(GregorianCalendar.HOUR, -2);
    obj2.setDateValue("unpublishDate", gc.getTime());
    obj3.setDateValue("publishDate", gc.getTime());
    gc.add(GregorianCalendar.HOUR, -2);
    obj2.setDateValue("publishDate", gc.getTime());
    doc.addXObject(obj1);
    doc.addXObject(obj2);
    doc.addXObject(obj3);
    assertFalse(doc.getXObjects(pubService.getPublicationClassReference()).isEmpty());
    assertTrue(pubService.isPublished(doc));
  }

  @Test
  public void testIsPublishActive_docNull() {
    expect(getWikiMock().getSpacePreference(eq("publishdate_active"), same((String) null), eq("-1"),
        same(getContext()))).andReturn("-1").once();
    expect(getWikiMock().getXWikiPreference(eq("publishdate_active"), eq(
        "celements.publishdate.active"), eq("0"), same(getContext()))).andReturn("0").once();
    replayDefault();
    assertEquals(false, pubService.isPublishActive());
    verifyDefault();
  }

  @Test
  public void testIsPublishActive_notSet() {
    expect(getWikiMock().getSpacePreference(eq("publishdate_active"), eq("TestSpace"), eq("-1"),
        same(getContext()))).andReturn("-1").once();
    expect(getWikiMock().getXWikiPreference(eq("publishdate_active"), eq(
        "celements.publishdate.active"), eq("0"), same(getContext()))).andReturn("0").once();
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(),
        "TestSpace", "TestDoc"));
    getContext().setDoc(doc);
    replayDefault();
    assertEquals(false, pubService.isPublishActive());
    verifyDefault();
  }

  @Test
  public void testIsPublishActive_false() {
    expect(getWikiMock().getSpacePreference(eq("publishdate_active"), eq("TestSpace"), eq("-1"),
        same(getContext()))).andReturn("0").once();
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(),
        "TestSpace", "TestDoc"));
    getContext().setDoc(doc);
    replayDefault();
    assertEquals(false, pubService.isPublishActive());
    verifyDefault();
  }

  @Test
  public void testIsPublishActive_true() {
    expect(getWikiMock().getSpacePreference(eq("publishdate_active"), eq("TestSpace"), eq("-1"),
        same(getContext()))).andReturn("1").once();
    XWikiDocument doc = new XWikiDocument(new DocumentReference(getContext().getDatabase(),
        "TestSpace", "TestDoc"));
    getContext().setDoc(doc);
    replayDefault();
    assertEquals(true, pubService.isPublishActive());
    verifyDefault();
  }

  @Test
  public void testIsRestrictedRightsAction_view() {
    assertTrue(pubService.isRestrictedRightsAction("view"));
  }

  @Test
  public void testIsRestrictedRightsAction_comment() {
    assertTrue(pubService.isRestrictedRightsAction("comment"));
  }

  @Test
  public void testIsRestrictedRightsAction_admin() {
    assertFalse(pubService.isRestrictedRightsAction("admin"));
  }

  @Test
  public void testIsAfterStart_empty() {
    assertEquals(true, pubService.isAfterStart(new BaseObject()));
  }

  @Test
  public void testIsAfterStart_beforeStart() {
    BaseObject obj = new BaseObject();
    Calendar gc = GregorianCalendar.getInstance();
    gc.add(GregorianCalendar.HOUR, 1);
    obj.setDateValue("publishDate", gc.getTime());
    assertFalse(pubService.isAfterStart(obj));
  }

  @Test
  public void testIsAfterStart_afterStart() {
    BaseObject obj = new BaseObject();
    Calendar gc = GregorianCalendar.getInstance();
    gc.add(GregorianCalendar.HOUR, -1);
    obj.setDateValue("publishDate", gc.getTime());
    assertTrue(pubService.isAfterStart(obj));
  }

  @Test
  public void testIsBeforeEnd_empty() {
    assertEquals(true, pubService.isBeforeEnd(new BaseObject()));
  }

  @Test
  public void testIsBeforeEnd_beforeEnd() {
    BaseObject obj = new BaseObject();
    Calendar gc = GregorianCalendar.getInstance();
    gc.add(GregorianCalendar.HOUR, 1);
    obj.setDateValue("unpublishDate", gc.getTime());
    assertTrue(pubService.isBeforeEnd(obj));
  }

  @Test
  public void testIsBeforeEnd_afterEnd() {
    BaseObject obj = new BaseObject();
    Calendar gc = GregorianCalendar.getInstance();
    gc.add(GregorianCalendar.HOUR, -1);
    obj.setDateValue("unpublishDate", gc.getTime());
    assertFalse(pubService.isBeforeEnd(obj));
  }

  @Test
  public void testIsPubUnpubOverride_nothingSet() {
    assertFalse(pubService.isPubUnpubOverride());
  }

  @Test
  public void testIsPubUnpubOverride_wrongSet() {
    getExecutionContext().setProperty(IPublicationServiceRole.OVERRIDE_PUB_CHECK,
        "test wrong type");
    assertFalse(pubService.isPubUnpubOverride());
  }

  @Test
  public void testIsPubUnpubOverride_pub() {
    getExecutionContext().setProperty(IPublicationServiceRole.OVERRIDE_PUB_CHECK,
        EPubUnpub.PUBLISHED);
    assertTrue(pubService.isPubUnpubOverride());
  }

  @Test
  public void testIsPubUnpubOverride_unpub() {
    getExecutionContext().setProperty(IPublicationServiceRole.OVERRIDE_PUB_CHECK,
        EPubUnpub.UNPUBLISHED);
    assertTrue(pubService.isPubUnpubOverride());
  }

  @Test
  public void testIsPubOverride_nothing() {
    assertFalse(pubService.isPubOverride());
  }

  @Test
  public void testIsPubOverride_unpub() {
    getExecutionContext().setProperty(IPublicationServiceRole.OVERRIDE_PUB_CHECK,
        EPubUnpub.UNPUBLISHED);
    assertFalse(pubService.isPubOverride());
  }

  @Test
  public void testIsPubOverride_pub() {
    getExecutionContext().setProperty(IPublicationServiceRole.OVERRIDE_PUB_CHECK,
        EPubUnpub.PUBLISHED);
    assertTrue(pubService.isPubOverride());
  }

  @Test
  public void testIsUnpubOverride_nothing() {
    assertFalse(pubService.isUnpubOverride());
  }

  @Test
  public void testIsUnpubOverride_unpub() {
    getExecutionContext().setProperty(IPublicationServiceRole.OVERRIDE_PUB_CHECK,
        EPubUnpub.UNPUBLISHED);
    assertTrue(pubService.isUnpubOverride());
  }

  @Test
  public void testIsUnpubOverride_pub() {
    getExecutionContext().setProperty(IPublicationServiceRole.OVERRIDE_PUB_CHECK,
        EPubUnpub.PUBLISHED);
    assertFalse(pubService.isUnpubOverride());
  }

  @Test
  public void test_overridePubUnpub_PUBLISHED() {
    pubService.overridePubUnpub(EPubUnpub.PUBLISHED);
    assertEquals(EPubUnpub.PUBLISHED, getExecutionContext().getProperty(
        IPublicationServiceRole.OVERRIDE_PUB_CHECK));
  }

  @Test
  public void test_overridePubUnpub_UNPUBLISHED() {
    pubService.overridePubUnpub(EPubUnpub.UNPUBLISHED);
    assertEquals(EPubUnpub.UNPUBLISHED, getExecutionContext().getProperty(
        IPublicationServiceRole.OVERRIDE_PUB_CHECK));
  }

  private ExecutionContext getExecutionContext() {
    return Utils.getComponent(Execution.class).getContext();
  }

}
