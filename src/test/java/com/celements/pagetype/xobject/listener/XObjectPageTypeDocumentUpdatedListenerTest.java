/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.pagetype.xobject.listener;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.IPageTypeClassConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeDocumentUpdatedListenerTest extends AbstractComponentTest {

  private static final String _COMPONENT_NAME = "XObjectPageTypeDocumentUpdatedListener";
  private XObjectPageTypeDocumentUpdatedListener eventListener;
  private XWikiContext context;

  @Before
  public void setUp_XObjectPageTypeDocumentUpdatedListenerTest() throws Exception {
    context = getContext();
    eventListener = getXObjPageTypeDocUpdatedListener();
  }

  @Test
  public void testGetWebUtilsService() {
    assertNotNull(eventListener.getWebUtilsService());
  }

  @Test
  public void testComponentSingleton() {
    assertSame(eventListener, getXObjPageTypeDocUpdatedListener());
  }

  @Test
  public void testGetName() {
    assertEquals(_COMPONENT_NAME, eventListener.getName());
  }

  @Test
  public void testGetEvents() {
    List<String> expectedEventClassList = Arrays.asList(
        new DocumentUpdatedEvent().getClass().getName());
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
  public void testIsPageTypePropertiesAdded_added() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject pageTypePropObj = new BaseObject();
    pageTypePropObj.setXClassReference(getPageTypePropertiesClassRef());
    document.addXObject(pageTypePropObj);
    replayDefault();
    assertTrue(eventListener.isPageTypePropertiesAdded(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesAdded_deleted() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject pageTypePropOrigObj = new BaseObject();
    pageTypePropOrigObj.setXClassReference(getPageTypePropertiesClassRef());
    origDoc.addXObject(pageTypePropOrigObj);
    replayDefault();
    assertFalse(eventListener.isPageTypePropertiesAdded(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesAdded_changed() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject pageTypePropObj = new BaseObject();
    pageTypePropObj.setXClassReference(getPageTypePropertiesClassRef());
    document.addXObject(pageTypePropObj);
    BaseObject pageTypePropOrigObj = new BaseObject();
    pageTypePropOrigObj.setXClassReference(getPageTypePropertiesClassRef());
    origDoc.addXObject(pageTypePropOrigObj);
    replayDefault();
    assertFalse(eventListener.isPageTypePropertiesAdded(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesAdded_noPageTypePropObj() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    replayDefault();
    assertFalse(eventListener.isPageTypePropertiesAdded(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesDeleted_added() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject pageTypePropObj = new BaseObject();
    pageTypePropObj.setXClassReference(getPageTypePropertiesClassRef());
    document.addXObject(pageTypePropObj);
    replayDefault();
    assertFalse(eventListener.isPageTypePropertiesDeleted(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesDeleted_deleted() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject pageTypePropOrigObj = new BaseObject();
    pageTypePropOrigObj.setXClassReference(getPageTypePropertiesClassRef());
    origDoc.addXObject(pageTypePropOrigObj);
    replayDefault();
    assertTrue(eventListener.isPageTypePropertiesDeleted(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesDeleted_changed() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject pageTypePropObj = new BaseObject();
    pageTypePropObj.setXClassReference(getPageTypePropertiesClassRef());
    document.addXObject(pageTypePropObj);
    BaseObject pageTypePropOrigObj = new BaseObject();
    pageTypePropOrigObj.setXClassReference(getPageTypePropertiesClassRef());
    origDoc.addXObject(pageTypePropOrigObj);
    replayDefault();
    assertFalse(eventListener.isPageTypePropertiesDeleted(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesDeleted_noPageTypePropObj() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    replayDefault();
    assertFalse(eventListener.isPageTypePropertiesDeleted(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesUpdated_no_PageTypePropObj() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    replayDefault();
    assertFalse(eventListener.isPageTypePropertiesUpdated(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesUpdated_remove_PageTypePropObj() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject pageTypePropOrigObj = new BaseObject();
    pageTypePropOrigObj.setXClassReference(getPageTypePropertiesClassRef());
    origDoc.addXObject(pageTypePropOrigObj);
    replayDefault();
    assertFalse(eventListener.isPageTypePropertiesUpdated(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesUpdated_add_PageTypePropObj() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject pageTypePropObj = new BaseObject();
    pageTypePropObj.setXClassReference(getPageTypePropertiesClassRef());
    document.addXObject(pageTypePropObj);
    replayDefault();
    assertFalse(eventListener.isPageTypePropertiesUpdated(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesUpdated_typeName_PageTypePropObj() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    BaseObject pageTypePropOrigObj = new BaseObject();
    pageTypePropOrigObj.setXClassReference(getPageTypePropertiesClassRef());
    pageTypePropOrigObj.setStringValue(IPageTypeClassConfig.PAGETYPE_PROP_TYPE_NAME, "oldTypeName");
    origDoc.addXObject(pageTypePropOrigObj);
    BaseObject pageTypePropObj = new BaseObject();
    pageTypePropObj.setXClassReference(getPageTypePropertiesClassRef());
    pageTypePropObj.setStringValue(IPageTypeClassConfig.PAGETYPE_PROP_TYPE_NAME, "newTypeName");
    document.addXObject(pageTypePropObj);
    replayDefault();
    assertTrue(eventListener.isPageTypePropertiesUpdated(document, origDoc));
    verifyDefault();
  }

  @Test
  public void testIsPageTypePropertiesUpdated_noChanges() {
    DocumentReference testDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument document = new XWikiDocument(testDocRef);
    DocumentReference parentReference = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestParentPage");
    document.setParentReference((EntityReference) parentReference);
    XWikiDocument origDoc = new XWikiDocument(testDocRef);
    DocumentReference origParentReference = new DocumentReference(context.getDatabase(),
        "TestSpace", "TestParentPage");
    origDoc.setParentReference((EntityReference) origParentReference);
    BaseObject pageTypePropOrigObj = new BaseObject();
    pageTypePropOrigObj.setXClassReference(getPageTypePropertiesClassRef());
    pageTypePropOrigObj.setStringValue(IPageTypeClassConfig.PAGETYPE_PROP_TYPE_NAME, "typeName");
    origDoc.addXObject(pageTypePropOrigObj);
    BaseObject pageTypePropObj = new BaseObject();
    pageTypePropObj.setXClassReference(getPageTypePropertiesClassRef());
    pageTypePropObj.setStringValue(IPageTypeClassConfig.PAGETYPE_PROP_TYPE_NAME, "typeName");
    document.addXObject(pageTypePropObj);
    replayDefault();
    assertFalse(eventListener.isPageTypePropertiesUpdated(document, origDoc));
    verifyDefault();
  }

  private DocumentReference getPageTypePropertiesClassRef() {
    return Utils.getComponent(IPageTypeClassConfig.class).getPageTypePropertiesClassRef(
        new WikiReference(context.getDatabase()));
  }

  private XObjectPageTypeDocumentUpdatedListener getXObjPageTypeDocUpdatedListener() {
    return (XObjectPageTypeDocumentUpdatedListener) Utils.getComponent(EventListener.class,
        _COMPONENT_NAME);
  }

}
