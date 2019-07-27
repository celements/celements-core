package com.celements.observation.object;

import static com.celements.observation.event.EventOperation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.observation.event.Event;

import com.celements.copydoc.ICopyDocumentRole;
import com.celements.model.object.ObjectBridge;
import com.celements.model.object.xwiki.XWikiObjectBridge;
import com.celements.observation.event.EventOperation;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(XObjectUpdateEventConverter.NAME)
public class XObjectUpdateEventConverter extends AbstractXObjectEventConverter {

  public static final String NAME = "XObjectUpdateEventConverter";

  @Requirement(XWikiObjectBridge.NAME)
  private ObjectBridge<XWikiDocument, BaseObject> xObjBridge;

  @Requirement
  private ICopyDocumentRole copyDocService;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(
        new DocumentUpdatingEvent(),
        new DocumentUpdatedEvent());
  }

  @Override
  protected Map<Class<? extends Event>, EventOperation> getEventOperationMapping() {
    return new ImmutableMap.Builder<Class<? extends Event>, EventOperation>()
        .put(DocumentUpdatingEvent.class, UPDATING)
        .put(DocumentUpdatedEvent.class, UPDATED)
        .build();
  }

  @Override
  protected XWikiDocument getRelevantDoc(XWikiDocument doc) {
    return doc;
  }

  @Override
  protected XWikiDocument getCompareToDoc(XWikiDocument doc) {
    return doc.getOriginalDocument();
  }

  @Override
  protected boolean shouldNotifyObject(BaseObject xObj, Map<ObjectMeta, BaseObject> objMap) {
    return copyDocService.checkObject(xObj, objMap.getOrDefault(ObjectMeta.from(xObj), xObj));
  }

}
