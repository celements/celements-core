package com.celements.navigation.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.navigation.NavigationClasses;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("TreeNodeDocumentUpdatedListener")
public class TreeNodeDocumentUpdatedListener implements EventListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      TreeNodeDocumentUpdatedListener.class);

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
    LOGGER.trace("TreeNodeDocumentUpdatedListener getName");
    return "TreeNodeDocumentUpdatedListener";
  }

  public List<Event> getEvents() {
    LOGGER.trace("TreeNodeDocumentUpdatedListener getEvents");
    return Arrays.asList((Event)new DocumentUpdatedEvent());
  }

  public void onEvent(Event event, Object source, Object data) {
    LOGGER.trace("TreeNodeDocumentUpdatedListener onEvent: start.");
    XWikiDocument document = (XWikiDocument) source;
    XWikiDocument origDoc = getOrginialDocument(source);
    if ((document != null) && (origDoc != null)) {
      LOGGER.debug("TreeNodeDocumentUpdatedListener onEvent: got event for ["
          + event.getClass() + "] on document [" + document.getDocumentReference()
          + "].");
      if (checkMenuItemDiffs(document, origDoc)) {
        LOGGER.trace("TreeNodeDocumentUpdatedListener flush menu item cache.");
        treeNodeCache.flushMenuItemCache();
      }
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "] -> skip.");
    }
  }

  boolean checkMenuItemDiffs(XWikiDocument document, XWikiDocument origDoc) {
    BaseObject menuItemObj = document.getXObject(getNavClasses().getMenuItemClassRef(
        getContext().getDatabase()));
    BaseObject menuItemOrigObj = origDoc.getXObject(getNavClasses().getMenuItemClassRef(
        getContext().getDatabase()));
    LOGGER.trace("TreeNodeDocumentUpdatedListener checkMenuItemDiffs menuItemObj ["
        + menuItemObj + "], menuItemOrigObj [" + menuItemOrigObj + "]");
    if ((menuItemObj != null) && (menuItemOrigObj != null)) {
      int newPos = menuItemObj.getIntValue(NavigationClasses.MENU_POSITION_FIELD, -1);
      int oldPos = menuItemOrigObj.getIntValue(NavigationClasses.MENU_POSITION_FIELD, -1);
      if (newPos != oldPos) {
        return true;
      } else {
        DocumentReference parentRef = document.getParentReference();
        DocumentReference parentOrigRef = origDoc.getParentReference();
        if ((parentRef != null) && (parentOrigRef != null)) {
          return !parentRef.equals(parentOrigRef);
        } else {
          return ((parentRef != null) || (parentOrigRef != null));
        }
      }
    } else if ((menuItemObj != null) || (menuItemOrigObj != null)) {
      return true;
    }
    return false;
  }

  private XWikiDocument getOrginialDocument(Object source) {
    if (source != null) {
      return ((XWikiDocument) source).getOriginalDocument();
    }
    return null;
  }

}
