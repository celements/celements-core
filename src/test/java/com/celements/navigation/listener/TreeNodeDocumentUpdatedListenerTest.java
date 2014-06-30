package com.celements.navigation.listener;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.NavigationClasses;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class TreeNodeDocumentUpdatedListenerTest
    extends AbstractBridgedComponentTestCase {
  
  private TreeNodeDocumentUpdatedListener eventListener;
  private XWikiContext context;

  @Before
  public void setUp_TreeNodeDocumentUpdatedListenerTest() throws Exception {
    context = getContext();
    eventListener = getTreeNodeDocumentUpdatedListener();
  }

  @Test
  public void testComponentSingleton() {
    assertSame(eventListener, getTreeNodeDocumentUpdatedListener());
  }

  @Test
  public void testGetEvents() {
    List<String> expectedEventClassList = Arrays.asList(new DocumentUpdatedEvent(
        ).getClass().getName());
    replayDefault();
    List<Event> actualEventList = eventListener.getEvents();
    assertEquals(expectedEventClassList.size(), actualEventList.size());
    for (Event actualEvent : actualEventList) {
      assertTrue("Unexpected Event [" + actualEvent.getClass().getName() + "] found.",
          expectedEventClassList.contains(actualEvent.getClass().getName()));
    }
    verifyDefault();
  }

  @Test
  public void testIsMenuItemAdded_added() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    document.addXObject(menuItemObj);
    replayDefault();
    assertTrue(eventListener.isMenuItemAdded(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemAdded_deleted() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject menuItemOrigObj = new BaseObject();
    menuItemOrigObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    origDoc.addXObject(menuItemOrigObj);
    replayDefault();
    assertFalse(eventListener.isMenuItemAdded(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemAdded_changed() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    document.addXObject(menuItemObj);
    BaseObject menuItemOrigObj = new BaseObject();
    menuItemOrigObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    origDoc.addXObject(menuItemOrigObj);
    replayDefault();
    assertFalse(eventListener.isMenuItemAdded(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemAdded_noMenuItems() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    replayDefault();
    assertFalse(eventListener.isMenuItemAdded(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemDeleted_added() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    document.addXObject(menuItemObj);
    replayDefault();
    assertFalse(eventListener.isMenuItemDeleted(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemDeleted_deleted() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject menuItemOrigObj = new BaseObject();
    menuItemOrigObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    origDoc.addXObject(menuItemOrigObj);
    replayDefault();
    assertTrue(eventListener.isMenuItemDeleted(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemDeleted_changed() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    document.addXObject(menuItemObj);
    BaseObject menuItemOrigObj = new BaseObject();
    menuItemOrigObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    origDoc.addXObject(menuItemOrigObj);
    replayDefault();
    assertFalse(eventListener.isMenuItemDeleted(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemDeleted_noMenuItems() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    replayDefault();
    assertFalse(eventListener.isMenuItemDeleted(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemUpdated_no_menuItem() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    replayDefault();
    assertFalse(eventListener.isMenuItemUpdated(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemUpdated_remove_menuItem() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject menuItemOrigObj = new BaseObject();
    menuItemOrigObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    origDoc.addXObject(menuItemOrigObj);
    replayDefault();
    assertFalse(eventListener.isMenuItemUpdated(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemUpdated_add_menuItem() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    document.addXObject(menuItemObj);
    replayDefault();
    assertFalse(eventListener.isMenuItemUpdated(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemUpdated_changePos_menuItem() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject menuItemOrigObj = new BaseObject();
    menuItemOrigObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    menuItemOrigObj.setIntValue(NavigationClasses.MENU_POSITION_FIELD, 2);
    origDoc.addXObject(menuItemOrigObj);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    menuItemObj.setIntValue(NavigationClasses.MENU_POSITION_FIELD, 1);
    document.addXObject(menuItemObj);
    replayDefault();
    assertTrue(eventListener.isMenuItemUpdated(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemUpdated_changeNavPart_menuItem() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject menuItemOrigObj = new BaseObject();
    menuItemOrigObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    menuItemOrigObj.setStringValue(NavigationClasses.MENU_PART_FIELD, "");
    origDoc.addXObject(menuItemOrigObj);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    menuItemObj.setStringValue(NavigationClasses.MENU_PART_FIELD, "mainNav");
    document.addXObject(menuItemObj);
    replayDefault();
    assertTrue(eventListener.isMenuItemUpdated(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemUpdated_parentChange() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    DocumentReference parentReference = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestParentPage");
    document.setParentReference((EntityReference)parentReference);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    DocumentReference origParentReference = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestOrigParentPage");
    origDoc.setParentReference((EntityReference)origParentReference);
    BaseObject menuItemOrigObj = new BaseObject();
    menuItemOrigObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    menuItemOrigObj.setIntValue(NavigationClasses.MENU_POSITION_FIELD, 2);
    origDoc.addXObject(menuItemOrigObj);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    menuItemObj.setIntValue(NavigationClasses.MENU_POSITION_FIELD, 2);
    document.addXObject(menuItemObj);
    replayDefault();
    assertTrue(eventListener.isMenuItemUpdated(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemUpdated_parentChange_noMenuItem() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    DocumentReference parentReference = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestParentPage");
    document.setParentReference((EntityReference)parentReference);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    DocumentReference origParentReference = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestOrigParentPage");
    origDoc.setParentReference((EntityReference)origParentReference);
    replayDefault();
    assertFalse(eventListener.isMenuItemUpdated(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsMenuItemUpdated_noChanges() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    DocumentReference parentReference = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestParentPage");
    document.setParentReference((EntityReference)parentReference);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    DocumentReference origParentReference = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestParentPage");
    origDoc.setParentReference((EntityReference)origParentReference);
    BaseObject menuItemOrigObj = new BaseObject();
    menuItemOrigObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    menuItemOrigObj.setIntValue(NavigationClasses.MENU_POSITION_FIELD, 2);
    origDoc.addXObject(menuItemOrigObj);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getNavigationClasses().getMenuItemClassRef(
        context.getDatabase()));
    menuItemObj.setIntValue(NavigationClasses.MENU_POSITION_FIELD, 2);
    document.addXObject(menuItemObj);
    replayDefault();
    assertFalse(eventListener.isMenuItemUpdated(document, origDoc));
    verifyDefault();
  }


  private NavigationClasses getNavigationClasses() {
    return (NavigationClasses) Utils.getComponent(IClassCollectionRole.class,
        "celements.celNavigationClasses");
  }

  private TreeNodeDocumentUpdatedListener getTreeNodeDocumentUpdatedListener() {
    return (TreeNodeDocumentUpdatedListener) Utils.getComponent(
        EventListener.class, "TreeNodeDocumentUpdatedListener");
  }

}
