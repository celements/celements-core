package com.celements.navigation.service;

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
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.NavigationConfig;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class XObjectNavigationFactoryTest extends AbstractComponentTest {

  private XObjectNavigationFactory xobjNavFactory;

  @Before
  public void setUp_XObjectNavigationFactoryTest() throws Exception {
    xobjNavFactory = (XObjectNavigationFactory) Utils.getComponent(NavigationFactory.class,
        "xobject");
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

  private INavigationClassConfig getNavClasses() {
    return Utils.getComponent(INavigationClassConfig.class);
  }

}
