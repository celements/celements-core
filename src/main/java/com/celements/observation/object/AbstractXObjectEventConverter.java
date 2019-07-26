package com.celements.observation.object;

import static java.util.Optional.*;

import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.celements.model.object.ObjectBridge;
import com.celements.model.object.xwiki.XWikiObjectBridge;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.observation.event.EventOperation;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

abstract class AbstractXObjectEventConverter
    extends AbstractLocalEventListener<XWikiDocument, XWikiContext> {

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Requirement(XWikiObjectBridge.NAME)
  protected ObjectBridge<XWikiDocument, BaseObject> xObjBridge;

  @Override
  protected void onEventInternal(Event sourceEvent, XWikiDocument doc, XWikiContext context) {
    if (doc.getTranslation() == 0) {
      final Map<ObjectMeta, BaseObject> objMap = XWikiObjectFetcher.on(getCompareToDoc(doc))
          .iter().stream()
          .collect(ImmutableMap.toImmutableMap(ObjectMeta::from, Function.identity()));
      XWikiObjectEditor.on(getRelevantDoc(doc)).fetch().iter().stream()
          .filter(xObj -> shouldNotifyObject(xObj, objMap))
          .forEach(xObj -> notifyObjectEvent(sourceEvent, doc, xObj));
    } else {
      LOGGER.trace("skip translated document [{}], [{}]", doc.getDocumentReference(),
          doc.getTranslation());
    }
  }

  private void notifyObjectEvent(Event sourceEvent, XWikiDocument doc, BaseObject xObj) {
    EventOperation operation = ofNullable(getEventOperationMapping().get(sourceEvent.getClass()))
        .orElseThrow(() -> new IllegalArgumentException("illegal event: " + sourceEvent));
    ObjectEvent objEvent = new ObjectEvent(operation, ObjectMeta.from(xObj).classRef);
    getObservationManager().notify(objEvent, doc, xObj);

  }

  protected abstract Map<Class<? extends Event>, EventOperation> getEventOperationMapping();

  protected abstract XWikiDocument getRelevantDoc(XWikiDocument doc);

  protected abstract XWikiDocument getCompareToDoc(XWikiDocument doc);

  protected abstract boolean shouldNotifyObject(BaseObject xObj, Map<ObjectMeta, BaseObject> objMap);

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
