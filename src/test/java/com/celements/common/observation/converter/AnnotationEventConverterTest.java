package com.celements.common.observation.converter;

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
import org.xwiki.observation.remote.converter.LocalEventConverter;
import org.xwiki.observation.remote.converter.RemoteEventConverter;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class AnnotationEventConverterTest extends AbstractBridgedComponentTestCase {
  
  private AnnotationEventConverter localEventConverter;
  private AnnotationEventConverter remoteEventConverter;

  @Before
  public void setUp_AnnotationEventConverterTest() throws Exception {
    localEventConverter = getAnnotationLocalEventConverter();
    remoteEventConverter = getAnnotationRemoteEventConverter();
  }

  @Test
  public void testComponentSingleton() throws Exception {
    assertSame(localEventConverter, getAnnotationLocalEventConverter());
  }

  @Test
  public void testComponentDoubleRole() {
    assertNotNull(localEventConverter);
    assertNotNull(remoteEventConverter);
    assertNotSame(localEventConverter, remoteEventConverter);
  }

  @Test
  public void testToRemote_local() {
    LocalEventData localEvent = new LocalEventData();
    localEvent.setEvent(new LocalTestEvent());
    assertFalse(localEventConverter.toRemote(localEvent, null));
  }

  @Test
  public void testToRemote_remote() {
    XWikiContext context = getContext();
    String database = "myDB";
    context.setDatabase(database);
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    LocalEventData localEvent = new LocalEventData();
    localEvent.setEvent(new RemoteTestEvent());
    localEvent.setSource(new XWikiDocument(docRef));
    localEvent.setData(context);
    RemoteEventData remoteEvent = new RemoteEventData();
    
    assertTrue(localEventConverter.toRemote(localEvent, remoteEvent));
    
    assertSame(localEvent.getEvent(), remoteEvent.getEvent());
    assertEquals(docRef, ((Map<?, ?>) remoteEvent.getSource()).get(
        AnnotationEventConverter.DOC_NAME));
    assertEquals(database, ((Map<?, ?>) remoteEvent.getData()).get(
        AnnotationEventConverter.CONTEXT_WIKI));
  }
  
  @Test
  public void testToRemote_invalidSource() {
    XWikiContext context = getContext();
    String database = "myDB";
    context.setDatabase(database);
    LocalEventData localEvent = new LocalEventData();
    localEvent.setEvent(new RemoteTestEvent());
    localEvent.setSource(new Object());
    localEvent.setData(context);
    RemoteEventData remoteEvent = new RemoteEventData();
    
    assertFalse(localEventConverter.toRemote(localEvent, remoteEvent));
  }

  @Test
  public void testToRemote_noContextInData() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    LocalEventData localEvent = new LocalEventData();
    localEvent.setEvent(new RemoteTestEvent());
    localEvent.setSource(new XWikiDocument(docRef));
    localEvent.setData(new Object());
    RemoteEventData remoteEvent = new RemoteEventData();
    
    assertTrue(localEventConverter.toRemote(localEvent, remoteEvent));
    
    assertSame(localEvent.getEvent(), remoteEvent.getEvent());
    assertEquals(docRef, ((Map<?, ?>) remoteEvent.getSource()).get(
        AnnotationEventConverter.DOC_NAME));
    assertEquals("xwikidb", ((Map<?, ?>) remoteEvent.getData()).get(
        AnnotationEventConverter.CONTEXT_WIKI));
  }

  @Test
  public void testFromRemote_local() {
    RemoteEventData remoteEvent = new RemoteEventData();
    remoteEvent.setEvent(new LocalTestEvent());
    assertFalse(localEventConverter.fromRemote(remoteEvent, null));
  }

  @Test
  public void testFromRemote_remote() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    Map<String, Serializable> docMap = new HashMap<String, Serializable>();
    docMap.put(AnnotationEventConverter.DOC_NAME, docRef);
    RemoteEventData remoteEvent = new RemoteEventData();
    remoteEvent.setEvent(new RemoteTestEvent());
    remoteEvent.setSource((Serializable) docMap);
    remoteEvent.setData(getContext());
    LocalEventData localEvent = new LocalEventData();
    
    assertTrue(localEventConverter.fromRemote(remoteEvent, localEvent));
    
    assertSame(localEvent.getEvent(), localEvent.getEvent());
    assertEquals(docRef, ((XWikiDocument) localEvent.getSource()).getDocumentReference());
    assertEquals(getContext().getDatabase(),
        ((XWikiContext) localEvent.getData()).getDatabase());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSerializeXWikiDocument_parent() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    EntityReference parentRef = new DocumentReference("wiki", "space", "parent");
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setParentReference(parentRef);
    HashMap<String, Serializable> map = (HashMap<String, Serializable>
        ) localEventConverter.serializeXWikiDocument(doc);
    assertEquals("wiki:space.parent", map.get(AnnotationEventConverter.DOC_PARENT));
    XWikiDocument ret = localEventConverter.unserializeDocument(map);
    assertEquals(parentRef, ret.getParentReference());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSerializeXWikiDocument_origParent() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    EntityReference parentRef = new DocumentReference("wiki", "space", "parent");
    XWikiDocument doc = new XWikiDocument(docRef);
    XWikiDocument origDoc = new XWikiDocument(docRef);
    origDoc.setParentReference(parentRef);
    doc.setOriginalDocument(origDoc);
    HashMap<String, Serializable> map = (HashMap<String, Serializable>
        ) localEventConverter.serializeXWikiDocument(doc);
    assertEquals("wiki:space.parent", map.get(AnnotationEventConverter.ORIGDOC_PARENT));
    XWikiDocument ret = localEventConverter.unserializeDocument(map);
    assertEquals(parentRef, ret.getOriginalDocument().getParentReference());
  }

  @Test
  @SuppressWarnings({ "unchecked", "deprecation" })
  public void testSerializeXWikiDocument_relative() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    EntityReference parentRef = new EntityReference("parent", EntityType.DOCUMENT);
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setParentReference(parentRef);
    HashMap<String, Serializable> map = (HashMap<String, Serializable>
        ) localEventConverter.serializeXWikiDocument(doc);
    assertEquals("parent", map.get(AnnotationEventConverter.DOC_PARENT));
    XWikiDocument ret = localEventConverter.unserializeDocument(map);
    assertEquals("parent", ret.getParent());
  }

  private AnnotationEventConverter getAnnotationLocalEventConverter() throws Exception {
    return (AnnotationEventConverter) Utils.getComponent(LocalEventConverter.class,
        "Annotation");
  }

  private AnnotationEventConverter getAnnotationRemoteEventConverter() throws Exception {
    return (AnnotationEventConverter) Utils.getComponent(RemoteEventConverter.class,
        "Annotation");
  }

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

}
