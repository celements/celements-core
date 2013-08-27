package com.celements.mandatory;

import java.util.Arrays;
import java.util.List;

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
      mandatoryDocCmp.checkAllMandatoryDocuments();
    } finally {
      getContext().setDatabase(saveDbName);
    }
  }

}
