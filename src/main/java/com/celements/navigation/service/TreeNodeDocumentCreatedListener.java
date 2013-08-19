package com.celements.navigation.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.navigation.NavigationClasses;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("TreeNodeDocumentCreatedListener")
public class TreeNodeDocumentCreatedListener implements EventListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      TreeNodeDocumentCreatedListener.class);

  @Requirement
  ITreeNodeCache treeNodeCache;

  @Requirement("celements.celNavigationClasses")
  IClassCollectionRole navClasses;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  private NavigationClasses getNavClasses() {
    return (NavigationClasses) navClasses;
  }

  public String getName() {
    return "TreeNodeDocumentCreatedListener";
  }

  public List<Event> getEvents() {
    return Arrays.asList((Event)new DocumentCreatedEvent());
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = (XWikiDocument) source;
    LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
        + document.getDocumentReference() + "].");
    BaseObject menuItemObj = document.getXObject(getNavClasses().getMenuItemClassRef(
        getContext().getDatabase()));
    if (menuItemObj != null) {
      treeNodeCache.flushMenuItemCache();
    }
  }

}
