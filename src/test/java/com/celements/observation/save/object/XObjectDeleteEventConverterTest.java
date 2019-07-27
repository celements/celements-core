package com.celements.observation.save.object;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.observation.save.SaveEventOperation.*;
import static com.celements.observation.save.object.XObjectCreateEventConverterTest.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.celements.observation.save.object.XObjectDeleteEventConverter;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class XObjectDeleteEventConverterTest extends AbstractComponentTest {

  XObjectDeleteEventConverter converter;

  @Before
  public void prepare() throws Exception {
    registerComponentMock(ObservationManager.class);
    converter = (XObjectDeleteEventConverter) Utils.getComponent(EventListener.class,
        XObjectDeleteEventConverter.NAME);
  }

  @Test
  public void test_noChange() {
    XWikiDocument currDoc = createDocWithOriginal();
    replayDefault();
    converter.onEvent(new DocumentDeletedEvent(), currDoc, null);
    verifyDefault();
  }

  @Test
  public void test_DocumentDeletingEvent() {
    XWikiDocument doc = createDocWithOriginal();
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETING);
    replayDefault();
    converter.onEvent(new DocumentDeletingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_DocumentDeletedEvent() {
    XWikiDocument doc = createDocWithOriginal();
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETED);
    replayDefault();
    converter.onEvent(new DocumentDeletedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_DocumentUpdatingEvent() {
    XWikiDocument doc = createDocWithOriginal();
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETING);
    replayDefault();
    converter.onEvent(new DocumentUpdatingEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_DocumentUpdatedEvent_multiple() {
    XWikiDocument doc = createDocWithOriginal();
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETED);
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETED);
    expectNotify(doc, addObject(doc.getOriginalDocument()), DELETED);
    replayDefault();
    converter.onEvent(new DocumentUpdatedEvent(), doc, null);
    verifyDefault();
  }

  @Test
  public void test_illegal_event() {
    XWikiDocument doc = createDocWithOriginal();
    addObject(doc.getOriginalDocument());
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
