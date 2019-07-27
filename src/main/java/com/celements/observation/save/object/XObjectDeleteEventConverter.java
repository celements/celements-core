package com.celements.observation.save.object;

import static com.celements.observation.save.SaveEventOperation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;

import com.celements.observation.save.SaveEventOperation;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(XObjectDeleteEventConverter.NAME)
public class XObjectDeleteEventConverter extends AbstractXObjectEventConverter {

  public static final String NAME = "XObjectDeleteEventConverter";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(
        new DocumentUpdatingEvent(),
        new DocumentUpdatedEvent(),
        new DocumentDeletingEvent(),
        new DocumentDeletedEvent());
  }

  @Override
  protected Map<Class<? extends Event>, SaveEventOperation> getEventOperationMapping() {
    return new ImmutableMap.Builder<Class<? extends Event>, SaveEventOperation>()
        .put(DocumentUpdatingEvent.class, DELETING)
        .put(DocumentUpdatedEvent.class, DELETED)
        .put(DocumentDeletingEvent.class, DELETING)
        .put(DocumentDeletedEvent.class, DELETED)
        .build();
  }

  @Override
  protected XWikiDocument getRelevantDoc(XWikiDocument doc) {
    return doc.getOriginalDocument();
  }

  @Override
  protected XWikiDocument getCompareToDoc(XWikiDocument doc) {
    return doc;
  }

  @Override
  protected boolean shouldNotifyObject(BaseObject xObj, Map<ObjectMeta, BaseObject> objMap) {
    return !objMap.containsKey(ObjectMeta.from(xObj));
  }

}
