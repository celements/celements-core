package com.celements.navigation.factories;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.navigation.INavigation;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.NavigationConfig;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.xobject.XObjectPageTypeProvider;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class PageTypeNavigationFactoryTest extends AbstractComponentTest {

  private PageTypeNavigationFactory xobjNavFactory;
  private XWiki xwiki;
  private IPageTypeResolverRole mockPageTypeResolver;
  private DocumentReference currDocRef;
  private XWikiDocument currDoc;
  private DocumentReference testDocRef;
  private DocumentReference pageTypeDocRef;
  private XWikiDocument pageTypeDoc;
  private DocumentReference defaultPageTypeDocRef;
  private XWikiDocument defaultPageTypeDoc;

  @Before
  public void setUp_PageTypeNavigationFactoryTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, LayoutServiceRole.class);
    xwiki = getMock(XWiki.class);
    mockPageTypeResolver = registerComponentMock(IPageTypeResolverRole.class);
    xobjNavFactory = (PageTypeNavigationFactory) Utils.getComponent(NavigationFactory.class,
        PageTypeNavigationFactory.PAGETYPE_NAV_FACTORY_HINT);
    defaultPageTypeDocRef = new DocumentReference(getXContext().getDatabase(), "PageTypes",
        "RichText");
    defaultPageTypeDoc = new XWikiDocument(defaultPageTypeDocRef);
    defaultPageTypeDoc.setNew(false);
    PageTypeReference defaultPageTypeRef = new PageTypeReference(defaultPageTypeDocRef.getName(),
        XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER, Collections.<String>emptyList());
    expect(mockPageTypeResolver.getPageTypeRefForCurrentDoc()).andReturn(
        defaultPageTypeRef).anyTimes();
    pageTypeDocRef = new DocumentReference(getXContext().getDatabase(), "PageTypes", "MyPageType");
    pageTypeDoc = new XWikiDocument(pageTypeDocRef);
    pageTypeDoc.setNew(false);
    PageTypeReference pageTypeRef = new PageTypeReference(pageTypeDocRef.getName(),
        XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER, Collections.<String>emptyList());
    testDocRef = new DocumentReference(getXContext().getDatabase(), "MySpace", "MyTestDoc");
    expect(mockPageTypeResolver.getPageTypeRefForDocWithDefault(eq(testDocRef))).andReturn(
        pageTypeRef).anyTimes();
    currDocRef = new DocumentReference(getXContext().getDatabase(), "MySpace", "MyDoc");
    currDoc = new XWikiDocument(currDocRef);
    currDoc.setNew(false);
    getXContext().setDoc(currDoc);
  }

  @Test
  public void testCreateNavigation() throws Exception {
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(defaultPageTypeDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getXContext().getDatabase()));
    defaultPageTypeDoc.addXObject(navConfigObj);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(defaultPageTypeDocRef))
        .andReturn(defaultPageTypeDoc);
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    replayDefault();
    INavigation nav = xobjNavFactory.createNavigation();
    assertEquals(mySpaceRef, nav.getNodeSpaceRef());
    verifyDefault();
  }

  @Test
  public void testCreateNavigation_docRef() throws Exception {
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(pageTypeDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getXContext().getDatabase()));
    pageTypeDoc.addXObject(navConfigObj);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(pageTypeDocRef))
        .andReturn(pageTypeDoc);
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    replayDefault();
    INavigation nav = xobjNavFactory.createNavigation(testDocRef);
    assertEquals(mySpaceRef, nav.getNodeSpaceRef());
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig() throws Exception {
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(defaultPageTypeDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getXContext().getDatabase()));
    defaultPageTypeDoc.addXObject(navConfigObj);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(defaultPageTypeDocRef))
        .andReturn(defaultPageTypeDoc);
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    replayDefault();
    assertTrue(xobjNavFactory.hasNavigationConfig());
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_docRef() throws Exception {
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(pageTypeDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getXContext().getDatabase()));
    pageTypeDoc.addXObject(navConfigObj);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(pageTypeDocRef))
        .andReturn(pageTypeDoc);
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    replayDefault();
    assertTrue(xobjNavFactory.hasNavigationConfig(testDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_docRef_false() throws Exception {
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(pageTypeDocRef))
        .andReturn(pageTypeDoc);
    replayDefault();
    assertFalse(xobjNavFactory.hasNavigationConfig(testDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_docRef_NotExists_false() throws Exception {
    pageTypeDoc.setNew(true);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(pageTypeDocRef))
        .andReturn(pageTypeDoc);
    replayDefault();
    assertFalse(xobjNavFactory.hasNavigationConfig(testDocRef));
    verifyDefault();
  }

  @Test
  public void testGetNavigationConfig_docRef() throws Exception {
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(pageTypeDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getXContext().getDatabase()));
    pageTypeDoc.addXObject(navConfigObj);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(pageTypeDocRef))
        .andReturn(pageTypeDoc);
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getXContext().getDatabase()));
    replayDefault();
    NavigationConfig navConfig = xobjNavFactory.getNavigationConfig(testDocRef);
    assertEquals(mySpaceRef, navConfig.getNodeSpaceRef().get());
    verifyDefault();
  }

  private INavigationClassConfig getNavClasses() {
    return Utils.getComponent(INavigationClassConfig.class);
  }

}
