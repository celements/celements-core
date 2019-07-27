package com.celements.observation.save.object;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.observation.save.SaveEventOperation.*;
import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.celements.observation.save.SaveEventOperation;
import com.celements.observation.save.object.ObjectEvent;
import com.celements.observation.save.object.XObjectCreateEventConverter;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class XObjectCreateEventConverterTest extends AbstractComponentTest {

  XObjectCreateEventConverter converter;

  @Before
  public void prepare() throws Exception {
    registerComponentMock(ObservationManager.class);
    converter = (XObjectCreateEventConverter) Utils.getComponent(EventListener.class,
        XObjectCreateEventConverter.NAME);
  }

  @Test
  public void test_noChange() {
    XWikiDocument currDoc = createDocWithOriginal();
    replayDefault();
    converter.onEvent(new DocumentCreatedEvent(), currDoc, null);
    verifyDefault();
  }

  @Test
  public void test_DocumentCreatingEvent() {
    XWikiDocument doc = createDocWithOriginal();
    expectNotify(doc, addObject(doc), CREATING);
    replayDefault();
    converter.onEvent(new DocumentCreatingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_DocumentCreatedEvent() {
    XWikiDocument doc = createDocWithOriginal();
    expectNotify(doc, addObject(doc), CREATED);
    replayDefault();
    converter.onEvent(new DocumentCreatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_DocumentUpdatingEvent() {
    XWikiDocument doc = createDocWithOriginal();
    expectNotify(doc, addObject(doc), CREATING);
    replayDefault();
    converter.onEvent(new DocumentUpdatingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_DocumentUpdatedEvent_multiple() {
    XWikiDocument doc = createDocWithOriginal();
    expectNotify(doc, addObject(doc), CREATED);
    expectNotify(doc, addObject(doc), CREATED);
    expectNotify(doc, addObject(doc), CREATED);
    replayDefault();
    converter.onEvent(new DocumentUpdatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_illegal_event() {
    XWikiDocument doc = createDocWithOriginal();
    addObject(doc);
    replayDefault();
    new ExceptionAsserter<IllegalArgumentException>(IllegalArgumentException.class) {

      @Override
      protected void execute() throws Exception {
        converter.onEvent(new DocumentDeletedEvent(), doc, null);
      }
    }.evaluate();
    verifyDefault();
  }

  static XWikiDocument createDocWithOriginal() {
    XWikiDocument currDoc = new XWikiDocument(new DocumentReference("wiki", "space", "curr"));
    XWikiDocument origDoc = new XWikiDocument(new DocumentReference("wiki", "space", "orig"));
    currDoc.setOriginalDocument(origDoc);
    return currDoc;
  }

  static BaseObject addObject(XWikiDocument doc) {
    BaseObject xObj = new BaseObject();
    xObj.setXClassReference(new DocumentReference("wiki", "space", "class"));
    doc.addXObject(xObj);
    return xObj;
  }

  static void expectNotify(XWikiDocument currDoc, BaseObject xObj, SaveEventOperation ops) {
    ObjectEvent expEvent = new ObjectEvent(ops, new ClassReference(xObj.getXClassReference()));
    getMock(ObservationManager.class).notify(eq(expEvent), same(currDoc), same(xObj));
  }

}
