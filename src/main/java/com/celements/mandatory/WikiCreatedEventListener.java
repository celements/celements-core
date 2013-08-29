package com.celements.mandatory;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.WikiCreatedEvent;
import org.xwiki.bridge.event.WikiEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.XWikiContext;

@Component("celements.mandatory.WikiCreatedEventListener")
public class WikiCreatedEventListener implements EventListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      WikiCreatedEventListener.class);

  @Requirement
  IMandatoryDocumentCompositorRole mandatoryDocCmp;

  @Requirement
  private Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public String getName() {
    return "celements.mandatory.WikiCreatedEventListener";
  }

  public List<Event> getEvents() {
    return Arrays.<Event>asList(new WikiCreatedEvent());
  }

  public void onEvent(Event event, Object source, Object data) {
    String saveDbName = getContext().getDatabase();
    try {
      WikiEvent wikiEvent = (WikiEvent) event;
      String newDbName = wikiEvent.getWikiId();
      getContext().setDatabase(newDbName);
      LOGGER.info("received wikiEvent [" + wikiEvent.getClass() + "] for wikiId ["
          + newDbName + "] now executing checkAllMandatoryDocuments.");
      mandatoryDocCmp.checkAllMandatoryDocuments();
    } finally {
      LOGGER.debug("finishing onEvent in WikiCreatedEventListener for wikiId ["
          + getContext().getDatabase() + "].");
      getContext().setDatabase(saveDbName);
    }
  }

}
