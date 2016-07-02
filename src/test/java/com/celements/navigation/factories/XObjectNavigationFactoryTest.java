package com.celements.navigation.factories;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.INavigation;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.NavigationConfig;
import com.celements.navigation.factories.NavigationFactory;
import com.celements.navigation.factories.XObjectNavigationFactory;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class XObjectNavigationFactoryTest extends AbstractComponentTest {

  private XObjectNavigationFactory xobjNavFactory;
  private XWiki xwiki;

  @Before
  public void setUp_XObjectNavigationFactoryTest() throws Exception {
    xwiki = getWikiMock();
    xobjNavFactory = (XObjectNavigationFactory) Utils.getComponent(NavigationFactory.class,
        XObjectNavigationFactory.XOBJECT_NAV_FACTORY_HINT);
  }

  @Test
  public void testLoadConfigFromObject_defaults() {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getContext().getDatabase()));
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getContext().getDatabase()));
    replayDefault();
    NavigationConfig navConfig = xobjNavFactory.loadConfigFromObject(navConfigObj);
    assertEquals(mySpaceRef, navConfig.getNodeSpaceRef());
    assertEquals("default for fromHierarchyLevel must be greater than zero.", 1,
        navConfig.getFromHierarchyLevel());
    verifyDefault();
  }

  @Test
  public void testLoadConfigFromObject_menuSpace() {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getContext().getDatabase()));
    String nodeSpaceName = "theMenuSpace";
    navConfigObj.setStringValue("menu_space", nodeSpaceName);
    SpaceReference parentSpaceRef = new SpaceReference(nodeSpaceName, new WikiReference(
        getContext().getDatabase()));
    replayDefault();
    NavigationConfig navConfig = xobjNavFactory.loadConfigFromObject(navConfigObj);
    assertEquals(parentSpaceRef, navConfig.getNodeSpaceRef());
    verifyDefault();
  }

  @Test
  public void testLoadConfigFromObject_menuSpace_empty() {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getContext().getDatabase()));
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getContext().getDatabase()));
    replayDefault();
    NavigationConfig navConfig = xobjNavFactory.loadConfigFromObject(navConfigObj);
    assertEquals(mySpaceRef, navConfig.getNodeSpaceRef());
    verifyDefault();
  }

  @Test
  public void testLoadConfigFromObject_presentationType_notEmpty() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getContext().getDatabase()));
    navConfigObj.setStringValue(INavigationClassConfig.PRESENTATION_TYPE_FIELD,
        "testPresentationType");
    IPresentationTypeRole componentInstance = createMockAndAddToDefault(
        IPresentationTypeRole.class);
    IWebUtilsService wUServiceMock = registerComponentMock(IWebUtilsService.class);
    expect(wUServiceMock.lookup(eq(IPresentationTypeRole.class), eq(
        "testPresentationType"))).andReturn(componentInstance);
    replayDefault();
    NavigationConfig navConfig = xobjNavFactory.loadConfigFromObject(navConfigObj);
    assertSame(componentInstance, navConfig.getPresentationType());
    verifyDefault();
  }

  @Test
  public void testCreateNavigation() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    XWikiDocument collConfigDoc = new XWikiDocument(cellConfigDocRef);
    getContext().setDoc(collConfigDoc);
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getContext().getDatabase()));
    collConfigDoc.addXObject(navConfigObj);
    expect(xwiki.exists(cellConfigDocRef, getContext())).andReturn(true);
    expect(xwiki.getDocument(cellConfigDocRef, getContext())).andReturn(collConfigDoc).once();
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getContext().getDatabase()));
    replayDefault();
    INavigation nav = xobjNavFactory.createNavigation();
    assertEquals(mySpaceRef, nav.getNodeSpaceRef());
    verifyDefault();
  }

  @Test
  public void testCreateNavigation_docRef() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    XWikiDocument collConfigDoc = new XWikiDocument(cellConfigDocRef);
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getContext().getDatabase()));
    collConfigDoc.addXObject(navConfigObj);
    expect(xwiki.exists(cellConfigDocRef, getContext())).andReturn(true);
    expect(xwiki.getDocument(cellConfigDocRef, getContext())).andReturn(collConfigDoc).once();
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getContext().getDatabase()));
    replayDefault();
    INavigation nav = xobjNavFactory.createNavigation(cellConfigDocRef);
    assertEquals(mySpaceRef, nav.getNodeSpaceRef());
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    XWikiDocument collConfigDoc = new XWikiDocument(cellConfigDocRef);
    getContext().setDoc(collConfigDoc);
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getContext().getDatabase()));
    collConfigDoc.addXObject(navConfigObj);
    expect(xwiki.exists(cellConfigDocRef, getContext())).andReturn(true);
    expect(xwiki.getDocument(cellConfigDocRef, getContext())).andReturn(collConfigDoc).once();
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    replayDefault();
    assertTrue(xobjNavFactory.hasNavigationConfig());
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_docRef() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    XWikiDocument collConfigDoc = new XWikiDocument(cellConfigDocRef);
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getContext().getDatabase()));
    collConfigDoc.addXObject(navConfigObj);
    expect(xwiki.exists(cellConfigDocRef, getContext())).andReturn(true);
    expect(xwiki.getDocument(cellConfigDocRef, getContext())).andReturn(collConfigDoc).once();
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    replayDefault();
    assertTrue(xobjNavFactory.hasNavigationConfig(cellConfigDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_docRef_false() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    XWikiDocument collConfigDoc = new XWikiDocument(cellConfigDocRef);
    expect(xwiki.exists(cellConfigDocRef, getContext())).andReturn(true);
    expect(xwiki.getDocument(cellConfigDocRef, getContext())).andReturn(collConfigDoc).once();
    replayDefault();
    assertFalse(xobjNavFactory.hasNavigationConfig(cellConfigDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_docRef_NotExists_false() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    expect(xwiki.exists(cellConfigDocRef, getContext())).andReturn(false);
    replayDefault();
    assertFalse(xobjNavFactory.hasNavigationConfig(cellConfigDocRef));
    verifyDefault();
  }

  @Test
  public void testGetNavigationConfig_docRef() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    XWikiDocument collConfigDoc = new XWikiDocument(cellConfigDocRef);
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        getContext().getDatabase()));
    collConfigDoc.addXObject(navConfigObj);
    expect(xwiki.exists(cellConfigDocRef, getContext())).andReturn(true);
    expect(xwiki.getDocument(cellConfigDocRef, getContext())).andReturn(collConfigDoc).once();
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getContext().getDatabase()));
    replayDefault();
    NavigationConfig navConfig = xobjNavFactory.getNavigationConfig(cellConfigDocRef);
    assertEquals(mySpaceRef, navConfig.getNodeSpaceRef());
    verifyDefault();
  }

  private INavigationClassConfig getNavClasses() {
    return Utils.getComponent(INavigationClassConfig.class);
  }

}
