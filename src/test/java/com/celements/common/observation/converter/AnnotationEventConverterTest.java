package com.celements.common.observation.converter;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.LocalEventData;
import org.xwiki.observation.remote.RemoteEventData;
import org.xwiki.observation.remote.converter.LocalEventConverter;
import org.xwiki.observation.remote.converter.RemoteEventConverter;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.doc.XWikiDocument;

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
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    LocalEventData localEvent = new LocalEventData();
    localEvent.setEvent(new RemoteTestEvent());
    localEvent.setSource(new XWikiDocument(docRef));
    localEvent.setData(getContext());
    RemoteEventData remoteEvent = new RemoteEventData();
    
    replayDefault();
    assertTrue(localEventConverter.toRemote(localEvent, remoteEvent));
    verifyDefault();
    
    assertSame(localEvent.getEvent(), remoteEvent.getEvent());
    assertEquals(docRef, ((Map<?, ?>) remoteEvent.getSource()).get("docname"));
    assertNotNull(remoteEvent.getData());
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
    docMap.put("docname", docRef);
    RemoteEventData remoteEvent = new RemoteEventData();
    remoteEvent.setEvent(new RemoteTestEvent());
    remoteEvent.setSource((Serializable) docMap);
    remoteEvent.setData(getContext());
    LocalEventData localEvent = new LocalEventData();
    
    replayDefault();
    assertTrue(localEventConverter.fromRemote(remoteEvent, localEvent));
    verifyDefault();
    
    assertSame(localEvent.getEvent(), localEvent.getEvent());
    assertEquals(docRef, ((XWikiDocument) localEvent.getSource()).getDocumentReference());
    assertNotNull(localEvent.getData());
  }

  private AnnotationEventConverter getAnnotationLocalEventConverter() throws Exception {
    return (AnnotationEventConverter) getComponentManager().lookup(
        LocalEventConverter.class, "Annotation");
  }

  private AnnotationEventConverter getAnnotationRemoteEventConverter() throws Exception {
    return (AnnotationEventConverter) getComponentManager().lookup(
        RemoteEventConverter.class, "Annotation");
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
