package com.celements.observation.save.object;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.observation.save.SaveEventOperation.*;
import static com.celements.observation.save.object.XObjectCreateEventConverterTest.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class XObjectUpdateEventConverterTest extends AbstractComponentTest {

  EventListener converter;

  @Before
  public void prepare() throws Exception {
    registerComponentMock(ObservationManager.class);
    converter = Utils.getComponent(EventListener.class, XObjectEventConverter.NAME);
  }

  @Test
  public void test_noChange() {
    XWikiDocument doc = createDocWithOriginal();
    replayDefault();
    converter.onEvent(new DocumentUpdatingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_noObjChange() {
    XWikiDocument doc = createDocWithOriginal();
    addObject(doc);
    addObject(doc.getOriginalDocument());
    replayDefault();
    converter.onEvent(new DocumentUpdatingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_create() {
    XWikiDocument doc = createDocWithOriginal();
    addObject(doc); // create, should not notify
    replayDefault();
    converter.onEvent(new DocumentUpdatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_delete() {
    XWikiDocument doc = createDocWithOriginal();
    addObject(doc.getOriginalDocument()); // delete, should not notify
    replayDefault();
    converter.onEvent(new DocumentUpdatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_DocumentUpdatingEvent() {
    XWikiDocument doc = createDocWithOriginal();
    addObject(doc.getOriginalDocument()).setStringValue("field", "val");
    expectNotify(doc, addObject(doc), UPDATING);
    replayDefault();
    converter.onEvent(new DocumentUpdatingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_DocumentUpdatedEvent_multiple() {
    XWikiDocument doc = createDocWithOriginal();
    addObject(doc.getOriginalDocument()).setStringValue("field", "val");
    expectNotify(doc, addObject(doc), UPDATED);
    addObject(doc.getOriginalDocument()).setStringValue("field", "val");
    expectNotify(doc, addObject(doc), UPDATED);
    addObject(doc); // new, should not notify
    addObject(doc.getOriginalDocument()); // deleted, should not notify
    replayDefault();
    converter.onEvent(new DocumentUpdatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_illegal_event() {
    XWikiDocument doc = createDocWithOriginal();
    addObject(doc.getOriginalDocument()).setStringValue("field", "val");
    addObject(doc);
    replayDefault();
    new ExceptionAsserter<IllegalArgumentException>(IllegalArgumentException.class) {

      @Override
      protected void execute() throws Exception {
        converter.onEvent(new DocumentCreatedEvent(), doc, null);
      }
    }.evaluate();
    verifyDefault();
  }

}
