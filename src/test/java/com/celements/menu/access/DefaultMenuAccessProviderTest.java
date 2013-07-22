package com.celements.menu.access;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class DefaultMenuAccessProviderTest extends AbstractBridgedComponentTestCase {

  private DefaultMenuAccessProvider defMenuAccessProvider;
  private XWikiContext context;
  private XWiki xwiki;
  private XWikiRightService rightsMock;

  @Before
  public void setUp_DefaultMenuAccessProviderTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    rightsMock = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightsMock).anyTimes();
    defMenuAccessProvider = (DefaultMenuAccessProvider) Utils.getComponent(
        IMenuAccessProviderRole.class, "celements.defaultMenuAccess");
  }

  @Test
  public void testDenyView() {
    replayDefault();
    assertFalse(defMenuAccessProvider.denyView(null));
    assertFalse(defMenuAccessProvider.denyView(new DocumentReference(
        context.getDatabase(), "Celements2", "CelMenuBar")));
    verifyDefault();
  }

  @Test
  public void testHasview_notLocal_central_hasAccess_XWikiGuest() throws Exception {
    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(false).once();
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
            eq("celements2web:Celements.MenuBar"), same(context))).andReturn(true).once();
    expect(xwiki.getXWikiPreferenceAsInt(eq("CelMenuBar-Celements.MenuBar"),
        eq("celements.menubar.guestview.Celements.MenuBar"), eq(0), same(context))
        ).andReturn(0);
    replayDefault();
    assertFalse(defMenuAccessProvider.hasview(menuBarDocRef));
    verifyDefault();
  }

  @Test
  public void testHasview_notLocal_central_hasAccess_user() throws Exception {
    String myUserName = "XWiki.myUser";
    context.setUser(myUserName);
    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(false).once();
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq(myUserName),
            eq("celements2web:Celements.MenuBar"), same(context))).andReturn(true).once();
    replayDefault();
    assertTrue(defMenuAccessProvider.hasview(menuBarDocRef));
    verifyDefault();
  }

  @Test
  public void testHasview_Local_central_noAccess() throws Exception {
    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(false).once();

    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(true).once();
    expect(
        rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
            eq("celements2web:Celements.MenuBar"), same(context))).andReturn(true).once();

    replayDefault();
    assertFalse(defMenuAccessProvider.hasview(menuBarDocRef));
    verifyDefault();
  }

  @Test
  public void testHasview_local_central_hasAccess() throws Exception {
    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(true).once();

    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("celements2web:Celements.MenuBar"), same(context))).andReturn(true).once();

    replayDefault();
    assertTrue(defMenuAccessProvider.hasview(menuBarDocRef));
    verifyDefault();
  }
  
  @Test
  public void testHasview_local_notCentral_hasAccess() throws Exception {
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(false).once();

    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(true).once();

    replayDefault();
    assertTrue(defMenuAccessProvider.hasview(menuBarDocRef));
    verifyDefault();
  }
  
  @Test
  public void testHasview_notLocal_central_noAccess() throws Exception {
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("celements2web:Celements.MenuBar"), same(context))).andReturn(false).once();

    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(false).once();
    expect(xwiki.getXWikiPreferenceAsInt(eq("CelMenuBar-Celements.MenuBar"),
        eq("celements.menubar.guestview.Celements.MenuBar"), eq(0), same(context))
        ).andReturn(1);
    replayDefault();
    assertFalse(defMenuAccessProvider.hasview(menuBarDocRef));
    verifyDefault();
  }
  
  @Test
  public void testHasview_local_notCentral_noAccess() throws Exception {
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(false).once();

    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(false).once();

    replayDefault();
    assertFalse(defMenuAccessProvider.hasview(menuBarDocRef));
    verifyDefault();
  }
  
}
