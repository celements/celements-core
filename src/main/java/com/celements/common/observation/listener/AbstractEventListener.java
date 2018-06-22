package com.celements.common.observation.listener;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public abstract class AbstractEventListener implements EventListener {

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
    return disabled || isDisabledInConfigSrc();
  }

  private boolean isDisabledInConfigSrc() {
    boolean disabled = false;
    Object prop = configSrc.getProperty(CFG_SRC_KEY);
    if (prop instanceof Collection) {
      disabled = ((Collection<?>) prop).contains(getName());
    } else if (prop instanceof String) {
      disabled = prop.toString().trim().equals(getName());
    }
    return disabled;
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
  public void onEvent(Event event, Object source, Object data) {
    if (isDisabled()) {
      getLogger().info("listener disabled");
    } else if ((event == null) || (source == null)) {
      getLogger().warn("onEvent: got null event '{}' or source '{}'", event, source);
    } else {
      getLogger().trace("onEvent: '{}', source '{}', data '{}'", event.getClass(), source, data);
      if (isLocalEvent()) {
        onLocalEvent(event, source, data);
      } else {
        onRemoteEvent(event, source, data);
      }
    }
  }

  protected abstract void onLocalEvent(@NotNull Event event, @NotNull Object source,
      @Nullable Object data);

  protected abstract void onRemoteEvent(@NotNull Event event, @NotNull Object source,
      @Nullable Object data);

  protected XWikiDocument getDocument(Object source, Event event) {
    XWikiDocument doc = (XWikiDocument) source;
    if (event instanceof DocumentDeletedEvent) {
      doc = doc.getOriginalDocument();
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

}
