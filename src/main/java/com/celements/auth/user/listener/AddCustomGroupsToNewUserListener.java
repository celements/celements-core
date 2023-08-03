package com.celements.auth.user.listener;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.xpn.xwiki.doc.XWikiDocument;

public class AddCustomGroupsToNewUserListener
    extends AbstractLocalEventListener<XWikiDocument, Object> {

  private static final String NAME = "addCustomGroupsToNewUser";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return List.of(new DocumentCreatedEvent(), new DocumentUpdatedEvent());
  }

  @Override
  protected void onEventInternal(@NotNull Event event, @NotNull XWikiDocument source, Object data) {
    // TODO Auto-generated method stub

  }

}
