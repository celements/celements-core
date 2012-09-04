package com.celements.web.service;


import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.User;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class PrepareVelocityContextServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private PrepareVelocityContextService prepVeloContextService;
  private XWikiDocument skinDoc;
  private XWikiRightService rightServiceMock;

  @Before
  public void setUp_PrepareVelocityContextServiceTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    rightServiceMock = createMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightServiceMock ).anyTimes();
    VelocityContext vContext = new VelocityContext();
    context.put("vcontext", vContext);
    prepVeloContextService = (PrepareVelocityContextService) Utils.getComponent(
        IPrepareVelocityContext.class);
    skinDoc = createMock(XWikiDocument.class);
    expect(skinDoc.getFullName()).andReturn("XWiki.Celements2Skin").anyTimes();
    expect(skinDoc.getDocumentReference()).andReturn(new DocumentReference(
        context.getDatabase(), "XWiki", "Celements2Skin")).anyTimes();
    expect(skinDoc.newDocument(same(context))).andReturn(new Document(skinDoc, context)
      ).anyTimes();
    expect(xwiki.getDocument(eq("celements2web:XWiki.Celements2Skin"), same(context))
      ).andReturn(skinDoc).anyTimes();
    expect(xwiki.getDocument(eq(new DocumentReference("celements2web", "XWiki",
        "Celements2Skin")), same(context))
      ).andReturn(skinDoc).anyTimes();
  }

  @Test
  public void testInitCelementsVelocity_checkNPEs_forNullContext() {
    context.remove("vcontext");
    prepVeloContextService.initCelementsVelocity(context);
  }
  
  @Test
  public void testInitCelementsVelocity_checkNPEs_forEmptyVContext(
      ) throws XWikiException {
    context.setUser("XWiki.myTestUser");
    VelocityContext vContext = new VelocityContext();
    context.put("vcontext", vContext);
    expect(xwiki.getPluginApi(eq(prepVeloContextService.getVelocityName()), same(context))
        ).andReturn(null
      ).anyTimes();
    expect(xwiki.getSpacePreference(eq("default_language"), same(context))).andReturn(""
      ).anyTimes();
    expect(xwiki.getSpacePreference(eq("language"), same(context))).andReturn(""
      ).anyTimes();
    expect(xwiki.getSpacePreference(eq("skin"), same(context))).andReturn(""
      ).anyTimes();
    expect(xwiki.getSpacePreference(eq("admin_language"), eq("de"), same(context))
      ).andReturn("").anyTimes();
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "myTestUser");
    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(
      new XWikiDocument(userDocRef)).anyTimes();
    expect(skinDoc.getURL(eq("view"), same(context))).andReturn("").anyTimes();
    expect(xwiki.getSpacePreference(eq("editbox_width"), same(context))).andReturn("123"
      ).anyTimes();
    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(true
      ).atLeastOnce();
    expect(xwiki.getDocument(eq("PageTypes.RichText"), same(context))).andReturn(
        new XWikiDocument()).atLeastOnce();
    expect(xwiki.getSkin(same(context))).andReturn("celements2web:Skins.CellSkin"
        ).anyTimes();
    DocumentReference cellSkinDoc = new DocumentReference("celements2web","Skins",
        "CellSkin");
    expect(xwiki.getDocument(eq(cellSkinDoc), same(context))).andReturn(new XWikiDocument(
        cellSkinDoc)).atLeastOnce();
    expect(xwiki.getUser(eq("XWiki.myTestUser"), same(context))
      ).andReturn(new User(context.getXWikiUser(), context)).atLeastOnce();
    XWikiGroupService groupServiceMock = createMock(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(context))).andReturn(groupServiceMock).anyTimes();
    List<DocumentReference> groupRefList = Collections.emptyList();
    expect(groupServiceMock.getAllGroupsReferencesForMember(eq(userDocRef), eq(0), eq(0),
        same(context))).andReturn(groupRefList).atLeastOnce();
    context.setWiki(xwiki);
    replayAll(groupServiceMock);
    prepVeloContextService.initCelementsVelocity(context);
    assertEquals("expecting tinyMCE_width be set.", "123", vContext.get("tinyMCE_width"));
    verifyAll(groupServiceMock);
  }
  
  @Test
  public void testInitPanelsVelocity_checkNPEs_forEmptyVContext() {
    context.put("vcontext", new VelocityContext());
    expect(xwiki.getSpacePreference(eq("showRightPanels"), same(context))).andReturn(null
      ).atLeastOnce();
    expect(xwiki.getSpacePreference(eq("showLeftPanels"), same(context))).andReturn(null
      ).atLeastOnce();
    replayAll();
    prepVeloContextService.initPanelsVelocity(context);
    verifyAll();
  }
  
  @Test
  public void testInitPanelsVelocity_checkNPEs_forNullContext() {
    context.remove("vcontext");
    prepVeloContextService.initPanelsVelocity(context);
  }
  
  @Test
  public void testGetRTEwidth_default() throws Exception {
    expect(xwiki.getSpacePreference(eq("editbox_width"), same(context))).andReturn("");
    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(true
      ).atLeastOnce();
    expect(xwiki.getDocument(eq("PageTypes.RichText"), same(context))).andReturn(
        new XWikiDocument()).atLeastOnce();
    replayAll();
    assertEquals("453", prepVeloContextService.getRTEwidth(context));
    verifyAll();
  }
  
  @Test
  public void testGetRTEwidth_preferences() throws Exception {
    expect(xwiki.getSpacePreference(eq("editbox_width"), same(context))).andReturn("500");
    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(true
      ).atLeastOnce();
    expect(xwiki.getDocument(eq("PageTypes.RichText"), same(context))).andReturn(
        new XWikiDocument()).atLeastOnce();
    replayAll();
    assertEquals("500", prepVeloContextService.getRTEwidth(context));
    verifyAll();
  }

  @Test
  public void testGetRTEwidth_pageType() throws Exception {
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    expect(xwiki.getSpacePreference(eq("editbox_width"), same(context))).andReturn("500"
        ).anyTimes();
    XWikiDocument theDoc = new XWikiDocument(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"));
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setStringValue("page_type", "SpecialRichText");
    DocumentReference pagTypeClassRef = new DocumentReference(context.getDatabase(), 
        "Celements2", "PageType");
    pageTypeObj.setXClassReference(pagTypeClassRef);
    theDoc.setXObjects(pagTypeClassRef, Arrays.asList(pageTypeObj));
    context.setDoc(theDoc);
    expect(request.get(eq("template"))).andReturn(null).anyTimes();
    DocumentReference specialPTRef = new DocumentReference(context.getDatabase(),
        "PageTypes", "SpecialRichText");
    expect(xwiki.exists(eq("PageTypes.SpecialRichText"), same(context))).andReturn(true
        ).atLeastOnce();
    XWikiDocument pageTypeDoc = new XWikiDocument(specialPTRef);
    expect(xwiki.getDocument(eq("PageTypes.SpecialRichText"), same(context))).andReturn(
        pageTypeDoc).once();
    BaseObject pageTypePropObj = new BaseObject();
    pageTypePropObj.setIntValue("rte_width", 700);
    DocumentReference pageTypePropClassRef = new DocumentReference(context.getDatabase(), 
        "Celements2", "PageTypeProperties");
    pageTypePropObj.setXClassReference(pageTypePropClassRef);
    pageTypeDoc.setXObjects(pageTypePropClassRef, Arrays.asList(pageTypePropObj));
    replayAll(request);
    assertEquals("700", prepVeloContextService.getRTEwidth(context));
    verifyAll(request);
  }
  

  private void replayAll(Object ... mocks) {
    replay(xwiki, skinDoc, rightServiceMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, skinDoc, rightServiceMock);
    verify(mocks);
  }

}
