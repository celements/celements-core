package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class SkinConfigObjCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private SkinConfigObjCommand skinConfObjCmd;
  private DocumentReference currDocRef;
  private XWikiDocument currentDoc;

  @Before
  public void setUp_SkinConfigObjCommandTest() throws Exception {
    context = getContext();
    currDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    currentDoc = new XWikiDocument(currDocRef);
    context.setDoc(currentDoc);
    xwiki = getWikiMock();
    skinConfObjCmd = new SkinConfigObjCommand();
  }

  @Test
  public void testGetSkinConfigObj_localSkin() throws Exception {
    DocumentReference skinDocRef = new DocumentReference(context.getDatabase(), "Skins",
        "Nautica05BlankSkin");
    XWikiDocument skinDoc = new XWikiDocument(skinDocRef);
    BaseObject skinObj = new BaseObject();
    DocumentReference skinClassRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiSkins");
    skinObj.setXClassReference(skinClassRef);
    skinObj.setStringValue("skin_config_class_name", "Skins.Nautica05ConfigClass");
    skinDoc.addXObject(skinObj);
    expect(xwiki.getDocument(eq(skinDocRef), same(context))).andReturn(skinDoc
        ).atLeastOnce();
    expect(xwiki.getSpacePreference(eq("skin"), same(context))).andReturn(
        "Skins.Nautica05BlankSkin").anyTimes();
    DocumentReference webPrefDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(xwiki.getDocument(eq("mySpace.WebPreferences"), same(context))).andReturn(
        webPrefDoc).atLeastOnce();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    BaseObject expectedSkinConfObj = new BaseObject();
    DocumentReference skinConfigClassRef = new DocumentReference(context.getDatabase(),
        "Skins", "Nautica05ConfigClass");
    expectedSkinConfObj.setXClassReference(skinConfigClassRef);
    xwikiPrefDoc.addXObject(expectedSkinConfObj);
    expect(xwiki.getDocument(eq("XWiki.XWikiPreferences"), same(context))).andReturn(
        xwikiPrefDoc).atLeastOnce();
    replayDefault();
    BaseObject skinConfObj = skinConfObjCmd.getSkinConfigObj();
    assertNotNull(skinConfObj);
    assertSame(expectedSkinConfObj, skinConfObj);
    verifyDefault();
  }

  @Test
  public void testGetSkinConfigObj_centralSkin() throws Exception {
    DocumentReference skinDocRef = new DocumentReference("celements2web", "Skins",
        "Nautica05BlankSkin");
    XWikiDocument skinDoc = new XWikiDocument(skinDocRef);
    BaseObject skinObj = new BaseObject();
    DocumentReference skinClassRef = new DocumentReference("celements2web", "XWiki",
        "XWikiSkins");
    skinObj.setXClassReference(skinClassRef);
    skinObj.setStringValue("skin_config_class_name", "Skins.Nautica05ConfigClass");
    skinDoc.addXObject(skinObj);
    expect(xwiki.getDocument(eq(skinDocRef), same(context))).andReturn(skinDoc
        ).atLeastOnce();
    expect(xwiki.getSpacePreference(eq("skin"), same(context))).andReturn(
        "celements2web:Skins.Nautica05BlankSkin").anyTimes();
    DocumentReference webPrefDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(xwiki.getDocument(eq("mySpace.WebPreferences"), same(context))).andReturn(
        webPrefDoc).atLeastOnce();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    BaseObject expectedSkinConfObj = new BaseObject();
    DocumentReference skinConfigClassRef = new DocumentReference(context.getDatabase(),
        "Skins", "Nautica05ConfigClass");
    expectedSkinConfObj.setXClassReference(skinConfigClassRef);
    xwikiPrefDoc.addXObject(expectedSkinConfObj);
    expect(xwiki.getDocument(eq("XWiki.XWikiPreferences"), same(context))).andReturn(
        xwikiPrefDoc).atLeastOnce();
    replayDefault();
    BaseObject skinConfObj = skinConfObjCmd.getSkinConfigObj();
    assertNotNull(skinConfObj);
    assertSame(expectedSkinConfObj, skinConfObj);
    verifyDefault();
  }

}
