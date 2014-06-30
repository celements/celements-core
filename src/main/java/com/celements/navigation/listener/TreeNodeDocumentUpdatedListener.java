/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.navigation.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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

  public void onEvent(Event event, Object source, Object data) {
    LOGGER.trace("TreeNodeDocumentUpdatedListener onEvent: start.");
    XWikiDocument document = (XWikiDocument) source;
    XWikiDocument origDoc = getOrginialDocument(source);
    if ((document != null) && (origDoc != null)
        && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("TreeNodeDocumentUpdatedListener onEvent: got event for ["
          + event.getClass() + "] on document [" + document.getDocumentReference()
          + "].");
      if (isMenuItemAdded(document, origDoc)) {
        LOGGER.debug("TreeNodeDocumentUpdatedListener checkMenuItemDiffs added to "
            + document.getDocumentReference() + "]");
        TreeNodeCreatedEvent newTreeNodeEvent = new TreeNodeCreatedEvent(
            document.getDocumentReference());
        getObservationManager().notify(newTreeNodeEvent, source, getContext());
      } else if (isMenuItemDeleted(document, origDoc)) {
        LOGGER.debug("TreeNodeDocumentUpdatedListener checkMenuItemDiffs deleted from "
            + document.getDocumentReference() + "]");
        TreeNodeDeletedEvent delTreeNodeEvent = new TreeNodeDeletedEvent(
            document.getDocumentReference());
        getObservationManager().notify(delTreeNodeEvent, source, getContext());
      }
      if (isMenuItemUpdated(document, origDoc)) {
        LOGGER.debug("TreeNodeDocumentUpdatedListener checkMenuItemDiffs updated on "
            + document.getDocumentReference() + "]");
        TreeNodeUpdatedEvent updTreeNodeEvent = new TreeNodeUpdatedEvent(
            document.getDocumentReference());
        getObservationManager().notify(updTreeNodeEvent, source, getContext());
      }
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
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
      String newPart = menuItemObj.getStringValue(NavigationClasses.MENU_PART_FIELD);
      String oldPart = menuItemOrigObj.getStringValue(NavigationClasses.MENU_PART_FIELD);
      LOGGER.debug("TreeNodeDocumentUpdatedListener checkMenuItemDiffs newPos ["
          + newPos + "], oldPos [" + oldPos + "]");
      if (newPos != oldPos) {
        return true;
      } else if (!StringUtils.equals(newPart, oldPart)) {
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
