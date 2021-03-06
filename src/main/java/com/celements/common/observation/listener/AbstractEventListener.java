package com.celements.common.observation.listener;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.configuration.ConfigSourceUtils;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public abstract class AbstractEventListener<S, D> implements EventListener {

  protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  static final String CFG_SRC_KEY = "celements.observation.disabledListener";

  private volatile boolean disabled = false;

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected IWebUtilsService webUtilsService;

  @Requirement
  protected ConfigurationSource configSrc;

  @Requirement
  protected ModelContext context;

  @Requirement
  private RemoteObservationManagerContext remoteObsManagerContext;

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

  /**
   * @deprecated instead use {@link #context}
   */
  @Deprecated
  protected XWikiContext getContext() {
    return context.getXWikiContext();
  }

  public synchronized boolean isDisabled() {
    return disabled || ConfigSourceUtils.getStringListProperty(configSrc, CFG_SRC_KEY).contains(
        getName());
  }

  public synchronized void enable() {
    disabled = false;
  }

  public synchronized void disable() {
    disabled = true;
  }

  protected boolean isLocalEvent() {
    return !remoteObsManagerContext.isRemoteState();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onEvent(Event event, Object source, Object data) {
    if (isDisabled()) {
      LOGGER.info("onEvent - listener disabled");
    } else if ((event == null) || (source == null)) {
      LOGGER.warn("onEvent - got null event '{}' or source '{}'", event, source);
    } else {
      LOGGER.trace("onEvent - '{}', source '{}', data '{}'", event.getClass(), source, data);
      if (isLocalEvent()) {
        onLocalEvent(event, (S) source, (D) data);
      } else {
        onRemoteEvent(event, (S) source, (D) data);
      }
    }
  }

  protected abstract void onLocalEvent(@NotNull Event event, @NotNull S source, @Nullable D data);

  protected abstract void onRemoteEvent(@NotNull Event event, @NotNull S source, @Nullable D data);

  protected XWikiDocument getDocument(Object source, Event event) {
    XWikiDocument doc = (XWikiDocument) source;
    if (event instanceof DocumentDeletedEvent) {
      doc = doc.getOriginalDocument();
    }
    return doc;
  }

  /**
   * @deprecated since 4.0 instead use {@link #LOGGER}
   */
  @Deprecated
  protected Logger getLogger() {
    return LOGGER;
  }

  @Deprecated
  public void injectWebUtilsService(IWebUtilsService webUtilsService) {
    this.webUtilsService = webUtilsService;
  }

  @Deprecated
  public void injectObservationManager(ObservationManager observationManager) {
    this.observationManager = observationManager;
  }

  @Deprecated
  void injectRemoteObservationManagerContext(RemoteObservationManagerContext context) {
    this.remoteObsManagerContext = context;
  }

}
