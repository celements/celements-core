package com.celements.common.observation.converter;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.LocalEventData;
import org.xwiki.observation.remote.RemoteEventData;
import org.xwiki.observation.remote.converter.RemoteEventConverter;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class AnnotationEventConverterTest extends AbstractComponentTest {

  private AnnotationEventConverter converter;

  @Before
  public void setUp_AnnotationEventConverterTest() throws Exception {
    converter = (AnnotationEventConverter) Utils.getComponent(RemoteEventConverter.class,
        "Annotation");
  }

  @Test
  public void test_getPriority() {
    assertEquals(1500, converter.getPriority());
  }

  @Test
  public void test_toRemote_local() {
    LocalEventData localEvent = new LocalEventData();
    localEvent.setEvent(new LocalTestEvent());
    assertFalse(converter.toRemote(localEvent, null));
  }

  @Test
  public void testToRemote_remote() {
    LocalEventData localEvent = new LocalEventData();
    localEvent.setEvent(new RemoteTestEvent());
    localEvent.setSource(getSerializable());
    localEvent.setData(getSerializable());
    RemoteEventData remoteEvent = new RemoteEventData();

    replayDefault();
    assertTrue(converter.toRemote(localEvent, remoteEvent));
    verifyDefault();

    assertSame(localEvent.getEvent(), remoteEvent.getEvent());
    assertSame(localEvent.getSource(), remoteEvent.getSource());
    assertSame(localEvent.getData(), remoteEvent.getData());
  }

  @Test
  public void test_serializeSource() {
    LocalEventData localEvent = new LocalEventData();
    localEvent.setSource(getSerializable());

    replayDefault();
    Serializable ret = converter.serializeSource(localEvent);
    verifyDefault();

    assertSame(localEvent.getSource(), ret);
  }

  @Test
  public void test_serializeSource_XWikiDocument() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    LocalEventData localEvent = new LocalEventData();
    localEvent.setSource(new XWikiDocument(docRef));

    replayDefault();
    Serializable ret = converter.serializeSource(localEvent);
    verifyDefault();

    assertEquals(docRef, ((Map<?, ?>) ret).get(AnnotationEventConverter.DOC_NAME));
  }

  @Test
  public void test_serializeSource_invalid() {
    LocalEventData localEvent = new LocalEventData();
    localEvent.setSource(new Object());

    replayDefault();
    try {
      converter.serializeSource(localEvent);
      fail("expecting ClassCastException");
    } catch (ClassCastException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void test_serializeData() {
    LocalEventData localEvent = new LocalEventData();
    localEvent.setData(getSerializable());

    replayDefault();
    Serializable ret = converter.serializeData(localEvent);
    verifyDefault();

    assertSame(localEvent.getData(), ret);
  }

  @Test
  public void test_serializeData_XWikiContext() {
    XWikiContext context = getContext();
    String database = "myDB";
    context.setDatabase(database);
    LocalEventData localEvent = new LocalEventData();
    localEvent.setData(context);

    replayDefault();
    Serializable ret = converter.serializeData(localEvent);
    verifyDefault();

    assertEquals(database, ((Map<?, ?>) ret).get(AnnotationEventConverter.CONTEXT_WIKI));
  }

  @Test
  public void test_serializeData_invalid() {
    LocalEventData localEvent = new LocalEventData();
    localEvent.setData(new Object());

    replayDefault();
    try {
      converter.serializeData(localEvent);
      fail("expecting ClassCastException");
    } catch (ClassCastException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void test_fromRemote_local() {
    RemoteEventData remoteEvent = new RemoteEventData();
    remoteEvent.setEvent(new LocalTestEvent());

    replayDefault();
    assertFalse(converter.fromRemote(remoteEvent, null));
  }

  @Test
  public void test_fromRemote_remote() {
    RemoteEventData remoteEvent = new RemoteEventData();
    remoteEvent.setEvent(new RemoteTestEvent());
    remoteEvent.setSource(getSerializable());
    remoteEvent.setData(getSerializable());
    LocalEventData localEvent = new LocalEventData();

    replayDefault();
    assertTrue(converter.fromRemote(remoteEvent, localEvent));
    verifyDefault();

    assertSame(remoteEvent.getEvent(), localEvent.getEvent());
    assertSame(remoteEvent.getSource(), localEvent.getSource());
    assertSame(remoteEvent.getData(), localEvent.getData());
  }

  @Test
  public void test_unserializeSource() {
    RemoteEventData remoteEvent = new RemoteEventData();
    remoteEvent.setSource(getSerializable());

    replayDefault();
    Object ret = converter.unserializeSource(remoteEvent);
    verifyDefault();

    assertSame(remoteEvent.getSource(), ret);
  }

  @Test
  public void test_unserializeSource_XWikiDocument() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    Map<String, Serializable> docMap = new HashMap<>();
    docMap.put(AnnotationEventConverter.DOC_NAME, docRef);
    RemoteEventData remoteEvent = new RemoteEventData();
    remoteEvent.setSource((Serializable) docMap);

    replayDefault();
    Object ret = converter.unserializeSource(remoteEvent);
    verifyDefault();

    assertEquals(docRef, ((XWikiDocument) ret).getDocumentReference());
  }

  @Test
  public void test_unserializeData() {
    RemoteEventData remoteEvent = new RemoteEventData();
    remoteEvent.setData(getSerializable());

    replayDefault();
    Object ret = converter.unserializeData(remoteEvent);
    verifyDefault();

    assertSame(remoteEvent.getData(), ret);
  }

  @Test
  public void test_unserializeData_XWikiContext() {
    RemoteEventData remoteEvent = new RemoteEventData();
    remoteEvent.setData(getContext());

    replayDefault();
    Object ret = converter.unserializeData(remoteEvent);
    verifyDefault();
    assertEquals(getContext().getDatabase(), ((XWikiContext) ret).getDatabase());
  }

  @Test
  public void test_serializeXWikiDocument_parent() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    EntityReference parentRef = new DocumentReference("wiki", "space", "parent");
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setParentReference(parentRef);
    Map<?, ?> map = (Map<?, ?>) converter.serializeXWikiDocument(doc);
    assertEquals("wiki:space.parent", map.get(AnnotationEventConverter.DOC_PARENT));

    replayDefault();
    XWikiDocument ret = converter.unserializeDocument((Serializable) map);
    verifyDefault();

    assertEquals(parentRef, ret.getParentReference());
  }

  @Test
  public void test_serializeXWikiDocument_origParent() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    EntityReference parentRef = new DocumentReference("wiki", "space", "parent");
    XWikiDocument doc = new XWikiDocument(docRef);
    XWikiDocument origDoc = new XWikiDocument(docRef);
    origDoc.setParentReference(parentRef);
    doc.setOriginalDocument(origDoc);
    Map<?, ?> map = (Map<?, ?>) converter.serializeXWikiDocument(doc);
    assertEquals("wiki:space.parent", map.get(AnnotationEventConverter.ORIGDOC_PARENT));

    replayDefault();
    XWikiDocument ret = converter.unserializeDocument((Serializable) map);
    verifyDefault();

    assertEquals(parentRef, ret.getOriginalDocument().getParentReference());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void test_serializeXWikiDocument_relative() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    EntityReference parentRef = new EntityReference("parent", EntityType.DOCUMENT);
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setParentReference(parentRef);
    Map<?, ?> map = (Map<?, ?>) converter.serializeXWikiDocument(doc);
    assertEquals("parent", map.get(AnnotationEventConverter.DOC_PARENT));

    replayDefault();
    XWikiDocument ret = converter.unserializeDocument((Serializable) map);
    verifyDefault();

    assertEquals("parent", ret.getParent());
  }

  @Test
  public void test_shouldConvert_local() {
    assertFalse(converter.shouldConvert(LocalTestEvent.class));
  }

  @Test
  public void test_shouldConvert_remote() {
    assertTrue(converter.shouldConvert(RemoteTestEvent.class));
  }

  @Test
  public void test_shouldConvert_none() {
    assertFalse(converter.shouldConvert(NoneTestEvent.class));
  }

  @Local
  private class LocalTestEvent implements Event, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean matches(Object otherEvent) {
      return false;
    }
  }

  @Remote
  private class RemoteTestEvent implements Event, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean matches(Object otherEvent) {
      return false;
    }
  }

  private class NoneTestEvent implements Event, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean matches(Object otherEvent) {
      return false;
    }
  }

  private Serializable getSerializable() {
    return new Serializable() {

      private static final long serialVersionUID = 1L;
    };
  }

}
