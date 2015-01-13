package com.celements.observation.listener;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;

public abstract class AbstractListener implements EventListener {

  @Requirement
  private RemoteObservationManagerContext remoteObsManagerContext;

  @Requirement
  private ComponentManager componentManager;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private Execution execution;

  /**
   * The observation manager that will be use to fire user creation events.
   * Note: We can't have the OM as a requirement, since it would create an
   * infinite initialization loop, causing a stack overflow error (this event
   * listener would require an initialized OM and the OM requires a list of
   * initialized event listeners)
   */
  private ObservationManager observationManager;

  protected ObservationManager getObservationManager() {
    if (this.observationManager == null) {
      try {
        this.observationManager = componentManager.lookup(ObservationManager.class);  
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

  protected IWebUtilsService getWebUtilsService() {
    return webUtilsService;
  }

  void injectRemoteObservationManagerContext(RemoteObservationManagerContext context) {
    this.remoteObsManagerContext = context;
  }

  void injectObservationManager(ObservationManager observationManager) {
    this.observationManager = observationManager;
  }

  void injectWebUtilsService(IWebUtilsService webUtilsService) {
    this.webUtilsService = webUtilsService;
  }

  void injecExecution(Execution execution) {
    this.execution = execution;
  }

}
