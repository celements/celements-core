package com.celements.common.observation.listener;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.model.access.IModelAccessFacade;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public abstract class AbstractEventListener implements EventListener {

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected IWebUtilsService webUtilsService;

  @Requirement
  private RemoteObservationManagerContext remoteObsManagerContext;

  @Requirement
  private Execution execution;

  /**
   * The observation manager that will be use to fire user creation events. Note: We can't
   * have the OM as a requirement, since it would create an infinite initialization loop,
   * causing a stack overflow error (this event listener would require an initialized OM
   * and the OM requires a list of initialized event listeners)
   */
  private ObservationManager observationManager;

  protected ObservationManager getObservationManager() {
    if (this.observationManager == null) {
      try {
        this.observationManager = webUtilsService.lookup(ObservationManager.class);
      } catch (ComponentLookupException exc) {
        throw new RuntimeException("Cound not retrieve an Observation Manager against "
            + "the component manager", exc);
      }
    }
    return this.observationManager;
  }

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  protected boolean isLocalEvent() {
    return !remoteObsManagerContext.isRemoteState();
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    if ((event != null) && (source != null)) {
      if (isLocalEvent()) {
        onLocalEvent(event, source, data);
      } else {
        onRemoteEvent(event, source, data);
      }
    } else {
      getLogger().warn("onEvent: got null values, event '{}', source '{}'", event,source);
    }
  }

  protected abstract void onLocalEvent(Event event, Object source, Object data);

  protected abstract void onRemoteEvent(Event event, Object source, Object data);

  protected XWikiDocument getDocument(Object source, Event event) {
    XWikiDocument doc = null;
    if (source != null) {
      doc = (XWikiDocument) source;
      if (event instanceof DocumentDeletedEvent) {
        doc = doc.getOriginalDocument();
      }
    }
    return doc;
  }

  protected abstract Logger getLogger();

  /**
   * FOR TEST PURPOSES ONLY
   */
  public void injectWebUtilsService(IWebUtilsService webUtilsService) {
    this.webUtilsService = webUtilsService;
  }

  /**
   * FOR TEST PURPOSES ONLY
   */
  public void injectObservationManager(ObservationManager observationManager) {
    this.observationManager = observationManager;
  }

  void injectRemoteObservationManagerContext(RemoteObservationManagerContext context) {
    this.remoteObsManagerContext = context;
  }

  void injecExecution(Execution execution) {
    this.execution = execution;
  }

}
