package com.celements.observation.save.object;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.observation.save.SaveEventOperation.*;
import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ImmutableObjectReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.observation.save.SaveEventOperation;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class XObjectEventConverterTest extends AbstractComponentTest {

  private EventListener converter;
  private XWikiDocument doc;

  @Before
  public void prepare() throws Exception {
    registerComponentMock(ObservationManager.class);
    converter = Utils.getComponent(EventListener.class, XObjectEventConverter.NAME);
    doc = createDocWithOriginal();
  }

  @Test
  public void test_noChange() {
    replayDefault();
    converter.onEvent(new DocumentCreatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_translation() {
    doc.setTranslation(1);
    addObject(doc);
    replayDefault();
    converter.onEvent(new DocumentCreatingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_creating() {
    expectNotify(doc, addObject(doc), CREATING);
    replayDefault();
    converter.onEvent(new DocumentCreatingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_created() {
    expectNotify(doc, addObject(doc), CREATED);
    replayDefault();
    converter.onEvent(new DocumentCreatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_create_multiple() {
    expectNotify(doc, addObject(doc), CREATED);
    expectNotify(doc, addObject(doc), CREATED);
    expectNotify(doc, addObject(doc), CREATED);
    replayDefault();
    converter.onEvent(new DocumentUpdatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_deleting() {
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETING);
    replayDefault();
    converter.onEvent(new DocumentDeletingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_deleted() {
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETED);
    replayDefault();
    converter.onEvent(new DocumentDeletedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_delete_multiple() {
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETED);
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETED);
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETED);
    replayDefault();
    converter.onEvent(new DocumentUpdatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_updating() {
    addObject(doc.getOriginalDocument()).setStringValue("field", "val");
    expectNotify(doc, addObject(doc), UPDATING);
    replayDefault();
    converter.onEvent(new DocumentUpdatingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_updated() {
    addObject(doc.getOriginalDocument());
    BaseObject xObj = addObject(doc);
    xObj.setStringValue("field", "val");
    expectNotify(doc, xObj, UPDATED);
    replayDefault();
    converter.onEvent(new DocumentUpdatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_update_noObjChange() {
    addObject(doc);
    addObject(doc.getOriginalDocument());
    replayDefault();
    converter.onEvent(new DocumentUpdatingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_mix() {
    // no change
    addObject(doc);
    addObject(doc.getOriginalDocument());
    // update
    addObject(doc.getOriginalDocument()).setStringValue("field", "val");
    expectNotify(doc, addObject(doc), UPDATED);
    // create
    doc.getOriginalDocument().removeXObject(addObject(doc.getOriginalDocument()));
    expectNotify(doc, addObject(doc), CREATED);
    // update
    addObject(doc.getOriginalDocument()).setStringValue("field", "val");
    expectNotify(doc, addObject(doc), UPDATED);
    // delete
    doc.removeXObject(addObject(doc));
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETED);
    replayDefault();
    converter.onEvent(new DocumentUpdatedEvent(), doc, null);
    verifyDefault();
  }

  private static XWikiDocument createDocWithOriginal() {
    XWikiDocument currDoc = new XWikiDocument(new DocumentReference("wiki", "space", "doc"));
    XWikiDocument origDoc = new XWikiDocument(currDoc.getDocumentReference());
    currDoc.setOriginalDocument(origDoc);
    return currDoc;
  }

  private static BaseObject addObject(XWikiDocument doc) {
    BaseObject xObj = new BaseObject();
    xObj.setXClassReference(new ClassReference("space", "class"));
    doc.addXObject(xObj);
    return xObj;
  }

  private static void expectNotify(XWikiDocument currDoc, BaseObject xObj, SaveEventOperation ops) {
    ObjectEvent expEvent = new ObjectEvent(ops, new ClassReference(xObj.getXClassReference()));
    getMock(ObservationManager.class).notify(eq(expEvent), same(currDoc),
        eq(ImmutableObjectReference.from(xObj)));
  }

}
