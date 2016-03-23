package com.celements.common.observation.converter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
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

  @Requirement("relative")
  private EntityReferenceResolver<String> relativeRefResolver;

  @Requirement
  private Execution exec;
  
  private XWikiContext getContext() {
    return (XWikiContext) exec.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public boolean toRemote(LocalEventData localEvent, RemoteEventData remoteEvent) {
    Event event = localEvent.getEvent();
    LOGGER.trace("toRemote: start for event '{}'", event.getClass());
    if (shouldConvert(event.getClass())) {
      LOGGER.trace("toRemote: serialize event '{}'", event.getClass());
      remoteEvent.setEvent((Serializable) event);
      remoteEvent.setSource(getSerializedSource(localEvent));
      remoteEvent.setData(getSerializedData(localEvent));
      return (remoteEvent.getSource() != null) && (remoteEvent.getData() != null);
    } else {
      LOGGER.debug("toRemote: skip event '{}'", event.getClass());
    }
    return false;
  }

  private Serializable getSerializedSource(LocalEventData localEvent) {
    Serializable source = null;
    if (localEvent.getSource() instanceof XWikiDocument) {
      source = serializeXWikiDocument((XWikiDocument) localEvent.getSource());
    } else {
      LOGGER.warn("getSerializedSource: source '{}' isn't XWikiDocument",
          localEvent.getSource());
    }
    return source;
  }

  private Serializable getSerializedData(LocalEventData localEvent) {
    XWikiContext context = null;
    if (localEvent.getData() instanceof XWikiDocument) {
      context = (XWikiContext) localEvent.getData();
    } else {
      context = getContext();
      LOGGER.info("no XWikiContext in data, getting from execution instead");
    }
    return serializeXWikiContext(context);
  }

  @Override
  public boolean fromRemote(RemoteEventData remoteEvent, LocalEventData localEvent) {
    Event event = (Event) remoteEvent.getEvent();
    LOGGER.trace("fromRemote: start for event '{}'", event.getClass());
    if (shouldConvert(event.getClass())) {
      LOGGER.trace("fromRemote: unserialize event '{}'", event.getClass());
      // fill the local event
      XWikiContext context = unserializeXWikiContext(remoteEvent.getData());
      if (context != null) {
        localEvent.setEvent(event);
        localEvent.setSource(unserializeDocument(remoteEvent.getSource()));
        localEvent.setData(unserializeXWikiContext(remoteEvent.getData()));
      }
      return true;
    } else {
      LOGGER.debug("fromRemote: skip event '{}'", event.getClass());
    }
    return false;
  }

  boolean shouldConvert(Class<? extends Event> eventClass) {
    boolean isRemote = eventClass.getAnnotation(Remote.class) != null;
    boolean isLocal = eventClass.getAnnotation(Local.class) != null;
    if (isRemote && isLocal) {
      throw new IllegalStateException("Event class is defined as local and remote at the "
          + "same time '" + eventClass + "'");
    } else if (isRemote) {
      return true;
    } else if (isLocal) {
      return false;
    } else {
      LOGGER.warn("Local/Remote Annotation missing from event class '{}'", eventClass);
      return false;
    }
  }

  @SuppressWarnings({ "unchecked", "deprecation" })
  @Override
  protected Serializable serializeXWikiDocument(XWikiDocument document) {
    HashMap<String, Serializable> remoteDataMap = (HashMap<String, Serializable>
        ) super.serializeXWikiDocument(document);
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

  @SuppressWarnings("unchecked")
  @Override
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
        origDoc.setParentReference(relativeRefResolver.resolve(origParentFN, 
            EntityType.DOCUMENT));
      }
    }
    return doc;
  }

}
