package com.celements.navigation.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.navigation.event.TreeNodeCreatedEvent;
import com.celements.navigation.event.TreeNodeDeletedEvent;
import com.celements.navigation.event.TreeNodeUpdatedEvent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("TreeNodeCacheListener")
public class TreeNodeCacheListener implements EventListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      TreeNodeCacheListener.class);

  @Requirement
  ITreeNodeCache treeNodeCache;

  public String getName() {
    return "TreeNodeDocumentDeletedListener";
  }

  public List<Event> getEvents() {
    return Arrays.asList((Event)new TreeNodeCreatedEvent(),
        (Event)new TreeNodeUpdatedEvent(), (Event)new TreeNodeDeletedEvent());
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = (XWikiDocument) source;
    if (document != null) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      treeNodeCache.flushMenuItemCache();
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "] -> skip.");
    }
  }

}
