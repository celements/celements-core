package com.celements.auth.user.listener;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.xpn.xwiki.doc.XWikiDocument;

public class CheckCorrectnessOfNewUserAndAddDefaultValuesListener
    extends AbstractLocalEventListener<XWikiDocument, Object> {

  private static final String NAME = "CheckCorrectnessOfNewUserAndAddDefaultValues";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return List.of(new DocumentCreatingEvent(), new DocumentUpdatingEvent());
  }

  @Override
  protected void onEventInternal(@NotNull Event event, @NotNull XWikiDocument source, Object data) {
    // TODO Auto-generated method stub

  }

}
