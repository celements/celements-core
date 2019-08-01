package com.celements.observation.save.object;

import static com.google.common.base.MoreObjects.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.celements.copydoc.ICopyDocumentRole;
import com.celements.model.object.ObjectBridge;
import com.celements.model.object.xwiki.XWikiObjectBridge;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.observation.save.SaveEventOperation;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(XObjectEventConverter.NAME)
public class XObjectEventConverter extends AbstractLocalEventListener<XWikiDocument, XWikiContext> {

  public static final String NAME = "XObjectEventConverter";

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
        new DocumentCreatingEvent(),
        new DocumentUpdatingEvent(),
        new DocumentDeletingEvent(),
        new DocumentCreatedEvent(),
        new DocumentUpdatedEvent(),
        new DocumentDeletedEvent());
  }

  @Override
  protected void onEventInternal(Event sourceEvent, XWikiDocument doc, XWikiContext context) {
    if (doc.getTranslation() == 0) {
      Map<ObjectMeta, BaseObject> newObjMap = getObjectMap(doc);
      Map<ObjectMeta, BaseObject> origObjMap = getObjectMap(doc.getOriginalDocument());
      for (ObjectMeta objMeta : Sets.union(newObjMap.keySet(), origObjMap.keySet())) {
        BaseObject newObj = newObjMap.get(objMeta);
        BaseObject origObj = origObjMap.get(objMeta);
        SaveEventOperation operation = calculateOperation(sourceEvent, newObj, origObj);
        if (!operation.isUpdate() || copyDocService.checkObject(origObj, newObj)) {
          ObjectEvent objEvent = new ObjectEvent(operation, objMeta.classRef);
          getObservationManager().notify(objEvent, doc, firstNonNull(newObj, origObj));
          LOGGER.trace("notified [{}] for changed object [{}]", objEvent, objMeta);
        } else {
          LOGGER.trace("skip unchanged object [{}]", objMeta);
        }
      }
    } else {
      LOGGER.debug("skip translation {} on document [{}]", doc.getTranslation(),
          doc.getDocumentReference());
    }
  }

  private Map<ObjectMeta, BaseObject> getObjectMap(XWikiDocument doc) {
    return XWikiObjectEditor.on(doc).fetch().iter().stream().collect(
        ImmutableMap.toImmutableMap(ObjectMeta::from, Function.identity()));
  }

  private SaveEventOperation calculateOperation(Event sourceEvent, BaseObject newObj,
      BaseObject origObj) {
    return SaveEventOperation.from((origObj != null), (newObj != null),
        SaveEventOperation.from(sourceEvent).isBeforeSave()).get();
  }

}
