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
import com.celements.model.access.IModelAccessFacade;
import com.celements.navigation.INavigation;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.NavigationConfig;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class XObjectNavigationFactoryTest extends AbstractComponentTest {

  private XObjectNavigationFactory xobjNavFactory;

  @Before
  public void setUp_XObjectNavigationFactoryTest() throws Exception {
    registerComponentMock(IModelAccessFacade.class);
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
        new WikiReference(getContext().getDatabase())));
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    SpaceReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getContext().getDatabase()));
    replayDefault();
    NavigationConfig navConfig = xobjNavFactory.loadConfigFromObject(navConfigObj);
    assertEquals(mySpaceRef, navConfig.getNodeSpaceRef().get());
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
        new WikiReference(getContext().getDatabase())));
    String nodeSpaceName = "theMenuSpace";
    navConfigObj.setStringValue("menu_space", nodeSpaceName);
    SpaceReference parentSpaceRef = new SpaceReference(nodeSpaceName, new WikiReference(
        getContext().getDatabase()));
    replayDefault();
    NavigationConfig navConfig = xobjNavFactory.loadConfigFromObject(navConfigObj);
    assertEquals(parentSpaceRef, navConfig.getNodeSpaceRef().get());
    verifyDefault();
  }

  @Test
  public void testLoadConfigFromObject_menuSpace_empty() {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        new WikiReference(getContext().getDatabase())));
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getContext().getDatabase()));
    replayDefault();
    NavigationConfig navConfig = xobjNavFactory.loadConfigFromObject(navConfigObj);
    assertEquals(mySpaceRef, navConfig.getNodeSpaceRef().get());
    verifyDefault();
  }

  @Test
  public void testLoadConfigFromObject_presentationType_notEmpty() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        new WikiReference(getContext().getDatabase())));
    navConfigObj.setStringValue(INavigationClassConfig.PRESENTATION_TYPE_FIELD,
        "testPresentationType");
    IPresentationTypeRole componentInstance = registerComponentMock(IPresentationTypeRole.class,
        "testPresentationType");
    replayDefault();
    NavigationConfig navConfig = xobjNavFactory.loadConfigFromObject(navConfigObj);
    assertSame(componentInstance, navConfig.getPresentationType().get());
    verifyDefault();
  }

  @Test
  public void testCreateNavigation() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    XWikiDocument collConfigDoc = new XWikiDocument(cellConfigDocRef);
    collConfigDoc.setNew(false);
    getContext().setDoc(collConfigDoc);
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        new WikiReference(getContext().getDatabase())));
    collConfigDoc.addXObject(navConfigObj);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(cellConfigDocRef))
        .andReturn(collConfigDoc);
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
    collConfigDoc.setNew(false);
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        new WikiReference(getContext().getDatabase())));
    collConfigDoc.addXObject(navConfigObj);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(cellConfigDocRef))
        .andReturn(collConfigDoc);
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
    collConfigDoc.setNew(false);
    getContext().setDoc(collConfigDoc);
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        new WikiReference(getContext().getDatabase())));
    collConfigDoc.addXObject(navConfigObj);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(cellConfigDocRef))
        .andReturn(collConfigDoc);
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
    collConfigDoc.setNew(false);
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        new WikiReference(getContext().getDatabase())));
    collConfigDoc.addXObject(navConfigObj);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(cellConfigDocRef))
        .andReturn(collConfigDoc);
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
    collConfigDoc.setNew(false);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(cellConfigDocRef))
        .andReturn(collConfigDoc);
    replayDefault();
    assertFalse(xobjNavFactory.hasNavigationConfig(cellConfigDocRef));
    verifyDefault();
  }

  @Test
  public void testHasNavigationConfig_docRef_NotExists_false() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    XWikiDocument collConfigDoc = new XWikiDocument(cellConfigDocRef);
    collConfigDoc.setNew(true);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(cellConfigDocRef))
        .andReturn(collConfigDoc);
    replayDefault();
    assertFalse(xobjNavFactory.hasNavigationConfig(cellConfigDocRef));
    verifyDefault();
  }

  @Test
  public void testGetNavigationConfig_docRef() throws Exception {
    DocumentReference cellConfigDocRef = new DocumentReference(getContext().getDatabase(),
        "MySpace", "MyDoc");
    XWikiDocument collConfigDoc = new XWikiDocument(cellConfigDocRef);
    collConfigDoc.setNew(false);
    BaseObject navConfigObj = new BaseObject();
    navConfigObj.setDocumentReference(cellConfigDocRef);
    navConfigObj.setXClassReference(getNavClasses().getNavigationConfigClassRef(
        new WikiReference(getContext().getDatabase())));
    collConfigDoc.addXObject(navConfigObj);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(cellConfigDocRef))
        .andReturn(collConfigDoc);
    String spaceName = "MySpace";
    navConfigObj.setStringValue("menu_space", spaceName);
    EntityReference mySpaceRef = new SpaceReference(spaceName, new WikiReference(
        getContext().getDatabase()));
    replayDefault();
    NavigationConfig navConfig = xobjNavFactory.getNavigationConfig(cellConfigDocRef);
    assertEquals(mySpaceRef, navConfig.getNodeSpaceRef().get());
    verifyDefault();
  }

  private INavigationClassConfig getNavClasses() {
    return Utils.getComponent(INavigationClassConfig.class);
  }

}
