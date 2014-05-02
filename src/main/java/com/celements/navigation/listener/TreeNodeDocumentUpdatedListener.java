package com.celements.navigation.listener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.navigation.NavigationClasses;
import com.celements.navigation.event.TreeNodeCreatedEvent;
import com.celements.navigation.event.TreeNodeDeletedEvent;
import com.celements.navigation.event.TreeNodeUpdatedEvent;
import com.celements.navigation.service.ITreeNodeCache;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("TreeNodeDocumentUpdatedListener")
public class TreeNodeDocumentUpdatedListener extends AbstractTreeNodeDocumentListener
    implements EventListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      TreeNodeDocumentUpdatedListener.class);

  @Requirement
  private ComponentManager componentManager;

  @Requirement
  ITreeNodeCache treeNodeCache;

  @Requirement("celements.celNavigationClasses")
  IClassCollectionRole navClasses;

  @Requirement
  RemoteObservationManagerContext remoteObservationManagerContext;

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

  private boolean isLocalEvent() {
    return !remoteObservationManagerContext.isRemoteState();
  }

  public void onEvent(Event event, Object source, Object data) {
    LOGGER.trace("TreeNodeDocumentUpdatedListener onEvent: start.");
    XWikiDocument document = (XWikiDocument) source;
    XWikiDocument origDoc = getOrginialDocument(source);
    if ((document != null) && (origDoc != null) && isLocalEvent()) {
      LOGGER.debug("TreeNodeDocumentUpdatedListener onEvent: got event for ["
          + event.getClass() + "] on document [" + document.getDocumentReference()
          + "].");
      if (isMenuItemAdded(document, origDoc)) {
        LOGGER.debug("TreeNodeDocumentUpdatedListener checkMenuItemDiffs added to "
            + document.getDocumentReference() + "]");
        TreeNodeCreatedEvent newTreeNodeEvent = new TreeNodeCreatedEvent();
        getObservationManager().notify(newTreeNodeEvent, source, Collections.emptyMap());
      } else if (isMenuItemDeleted(document, origDoc)) {
        LOGGER.debug("TreeNodeDocumentUpdatedListener checkMenuItemDiffs deleted from "
            + document.getDocumentReference() + "]");
        TreeNodeDeletedEvent delTreeNodeEvent = new TreeNodeDeletedEvent();
        getObservationManager().notify(delTreeNodeEvent, source, Collections.emptyMap());
      }
      if (isMenuItemUpdated(document, origDoc)) {
        LOGGER.debug("TreeNodeDocumentUpdatedListener checkMenuItemDiffs updated on "
            + document.getDocumentReference() + "]");
        TreeNodeUpdatedEvent updTreeNodeEvent = new TreeNodeUpdatedEvent();
        getObservationManager().notify(updTreeNodeEvent, source, Collections.emptyMap());
      }
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent [" + isLocalEvent()
          + "] -> skip.");
    }
  }

  boolean isMenuItemAdded(XWikiDocument document, XWikiDocument origDoc) {
    BaseObject menuItemObj = document.getXObject(getNavClasses().getMenuItemClassRef(
        getContext().getDatabase()));
    BaseObject menuItemOrigObj = origDoc.getXObject(getNavClasses().getMenuItemClassRef(
        getContext().getDatabase()));
    LOGGER.trace("checkMenuItemAdded checkMenuItemDiffs menuItemObj [" + menuItemObj
        + "], menuItemOrigObj [" + menuItemOrigObj + "]");
    return ((menuItemObj != null) && (menuItemOrigObj == null));
  }

  boolean isMenuItemDeleted(XWikiDocument document, XWikiDocument origDoc) {
    BaseObject menuItemObj = document.getXObject(getNavClasses().getMenuItemClassRef(
        getContext().getDatabase()));
    BaseObject menuItemOrigObj = origDoc.getXObject(getNavClasses().getMenuItemClassRef(
        getContext().getDatabase()));
    LOGGER.trace("checkMenuItemAdded checkMenuItemDiffs menuItemObj [" + menuItemObj
        + "], menuItemOrigObj [" + menuItemOrigObj + "]");
    return ((menuItemObj == null) && (menuItemOrigObj != null));
  }

  boolean isMenuItemUpdated(XWikiDocument document, XWikiDocument origDoc) {
    BaseObject menuItemObj = document.getXObject(getNavClasses().getMenuItemClassRef(
        getContext().getDatabase()));
    BaseObject menuItemOrigObj = origDoc.getXObject(getNavClasses().getMenuItemClassRef(
        getContext().getDatabase()));
    LOGGER.trace("TreeNodeDocumentUpdatedListener checkMenuItemDiffs menuItemObj ["
        + menuItemObj + "], menuItemOrigObj [" + menuItemOrigObj + "]");
    if ((menuItemObj != null) && (menuItemOrigObj != null)) {
      int newPos = menuItemObj.getIntValue(NavigationClasses.MENU_POSITION_FIELD, -1);
      int oldPos = menuItemOrigObj.getIntValue(NavigationClasses.MENU_POSITION_FIELD, -1);
      LOGGER.debug("TreeNodeDocumentUpdatedListener checkMenuItemDiffs newPos ["
          + newPos + "], oldPos [" + oldPos + "]");
      if (newPos != oldPos) {
        return true;
      } else {
        DocumentReference parentRef = document.getParentReference();
        DocumentReference parentOrigRef = origDoc.getParentReference();
        LOGGER.debug("TreeNodeDocumentUpdatedListener checkMenuItemDiffs parentRef ["
            + parentRef + "], parentOrigRef [" + parentOrigRef + "]");
        if ((parentRef != null) && (parentOrigRef != null)) {
          return !parentRef.equals(parentOrigRef);
        } else {
          return ((parentRef != null) || (parentOrigRef != null));
        }
      }
    }
    return false;
  }

  private XWikiDocument getOrginialDocument(Object source) {
    if (source != null) {
      return ((XWikiDocument) source).getOriginalDocument();
    }
    return null;
  }

  @Override
  protected ComponentManager getComponentManager() {
    return componentManager;
  }

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

}
