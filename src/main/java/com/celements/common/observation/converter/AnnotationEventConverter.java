package com.celements.common.observation.converter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.LocalEventData;
import org.xwiki.observation.remote.RemoteEventData;

import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.observation.remote.converter.AbstractXWikiEventConverter;

@Component("Annotation")
public class AnnotationEventConverter extends AbstractXWikiEventConverter {

  private static Logger LOGGER = LoggerFactory.getLogger(AnnotationEventConverter.class);

  public static final String CONTEXT_WIKI = "contextwiki";
  public static final String CONTEXT_USER = "contextuser";
  public static final String DOC_NAME = "docname";
  public static final String DOC_PARENT = "docparent";
  public static final String DOC_VERSION = "docversion";
  public static final String DOC_LANGUAGE = "doclanguage";
  public static final String ORIGDOC_PARENT = "origdocparent";
  public static final String ORIGDOC_VERSION = "origdocversion";
  public static final String ORIGDOC_LANGUAGE = "origdoclanguage";

  private static final String CFG_SRC_KEY = "celements.observation.noAnnotationMissingWarning";

  @Requirement("relative")
  private EntityReferenceResolver<String> relativeRefResolver;

  @Requirement
  private ConfigurationSource configSrc;

  @Override
  public int getPriority() {
    // default < this < SerializableEventConverter
    return 1500;
  }

  @Override
  public boolean toRemote(LocalEventData localEvent, RemoteEventData remoteEvent) {
    Event event = localEvent.getEvent();
    LOGGER.trace("toRemote: start for event '{}'", event.getClass());
    if (shouldConvert(event.getClass())) {
      LOGGER.trace("toRemote: serialize event '{}'", event.getClass());
      remoteEvent.setEvent((Serializable) event);
      remoteEvent.setSource(serializeSource(localEvent));
      remoteEvent.setData(serializeData(localEvent));
      return true;
    } else {
      LOGGER.debug("toRemote: skip event '{}'", event.getClass());
    }
    return false;
  }

  protected Serializable serializeSource(LocalEventData localEvent) {
    Serializable ret = null;
    Object source = localEvent.getSource();
    if (source instanceof XWikiDocument) {
      ret = serializeXWikiDocument((XWikiDocument) source);
    } else {
      ret = (Serializable) source;
    }
    return ret;
  }

  protected Serializable serializeData(LocalEventData localEvent) {
    Serializable ret = null;
    Object data = localEvent.getData();
    if (data instanceof XWikiContext) {
      ret = serializeXWikiContext((XWikiContext) localEvent.getData());
    } else {
      ret = (Serializable) data;
    }
    return ret;
  }

  @Override
  public boolean fromRemote(RemoteEventData remoteEvent, LocalEventData localEvent) {
    Event event = (Event) remoteEvent.getEvent();
    LOGGER.trace("fromRemote: start for event '{}'", event.getClass());
    if (shouldConvert(event.getClass())) {
      LOGGER.trace("fromRemote: unserialize event '{}'", event.getClass());
      localEvent.setEvent(event);
      localEvent.setSource(unserializeSource(remoteEvent));
      localEvent.setData(unserializeData(remoteEvent));
      return true;
    } else {
      LOGGER.debug("fromRemote: skip event '{}'", event.getClass());
    }
    return false;
  }

  protected Object unserializeSource(RemoteEventData remoteEvent) {
    Object source = remoteEvent.getSource();
    if (source instanceof Map<?, ?>) {
      source = unserializeDocument((Serializable) source);
    }
    return source;
  }

  protected Object unserializeData(RemoteEventData remoteEvent) {
    Object data = remoteEvent.getData();
    if (data instanceof Map<?, ?>) {
      data = unserializeXWikiContext((Serializable) data);
    }
    return data;
  }

  boolean shouldConvert(Class<? extends Event> eventClass) {
    if (eventClass.isAnnotationPresent(Remote.class)) {
      return true;
    } else {
      logWarning(eventClass.getName());
      return false;
    }
  }

  private void logWarning(String eventClassName) {
    for (Object field : configSrc.getProperty(CFG_SRC_KEY, List.class)) {
      if (eventClassName.startsWith(field.toString())) {
        return;
      }
    }
    LOGGER.warn("Local/Remote Annotation missing on event class '{}'", eventClassName);
  }

  @Override
  @SuppressWarnings({ "unchecked", "deprecation" })
  protected Serializable serializeXWikiDocument(XWikiDocument document) {
    HashMap<String, Serializable> remoteDataMap = (HashMap<String, Serializable>) super.serializeXWikiDocument(
        document);
    // having to use getParent() here instead of getParentReference()
    // because the latter always returns an absolute document reference but we want to
    // serialize the relative reference.
    // getRelativeParentReference() is not (yet) public in 2.7.2
    remoteDataMap.put(DOC_PARENT, document.getParent());
    XWikiDocument originalDocument = document.getOriginalDocument();
    if (originalDocument != null) {
      remoteDataMap.put(ORIGDOC_PARENT, originalDocument.getParent());
    }
    return remoteDataMap;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected XWikiDocument unserializeDocument(Serializable remoteData) {
    XWikiDocument doc = super.unserializeDocument(remoteData);
    Map<String, Serializable> remoteDataMap = (Map<String, Serializable>) remoteData;
    String parentFN = (String) remoteDataMap.get(DOC_PARENT);
    if (!Strings.isNullOrEmpty(parentFN)) {
      doc.setParentReference(relativeRefResolver.resolve(parentFN, EntityType.DOCUMENT));
    }
    XWikiDocument origDoc = doc.getOriginalDocument();
    if (origDoc != null) {
      String origParentFN = (String) remoteDataMap.get(ORIGDOC_PARENT);
      if (!Strings.isNullOrEmpty(origParentFN)) {
        origDoc.setParentReference(
            relativeRefResolver.resolve(origParentFN, EntityType.DOCUMENT));
      }
    }
    return doc;
  }

}
