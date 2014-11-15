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
package com.celements.pagetype.xobject.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.celements.pagetype.xobject.event.XObjectPageTypeCreatedEvent;
import com.celements.pagetype.xobject.event.XObjectPageTypeDeletedEvent;
import com.celements.pagetype.xobject.event.XObjectPageTypeUpdatedEvent;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("XObjectPageTypeDocumentUpdatedListener")
public class XObjectPageTypeDocumentUpdatedListener
    extends AbstractXObjectPageTypeDocumentListener implements EventListener {

  private static Logger LOGGER = LoggerFactory.getLogger(
      XObjectPageTypeDocumentUpdatedListener.class);

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
    LOGGER.trace("XObjectPageTypeDocumentUpdatedListener getName");
    return "XObjectPageTypeDocumentUpdatedListener";
  }

  public List<Event> getEvents() {
    LOGGER.trace("XObjectPageTypeDocumentUpdatedListener getEvents");
    return Arrays.asList((Event)new DocumentUpdatedEvent());
  }

  public void onEvent(Event event, Object source, Object data) {
    LOGGER.trace("XObjectPageTypeDocumentUpdatedListener onEvent: start.");
    XWikiDocument document = (XWikiDocument) source;
    XWikiDocument origDoc = getOrginialDocument(source);
    if ((document != null) && (origDoc != null)
        && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("XObjectPageTypeDocumentUpdatedListener onEvent: got event for ["
          + event.getClass() + "] on document [" + document.getDocumentReference()
          + "].");
      if (isPageTypePropertiesAdded(document, origDoc)) {
        LOGGER.debug("XObjectPageTypeDocumentUpdatedListener checkMenuItemDiffs added to "
            + document.getDocumentReference() + "]");
        XObjectPageTypeCreatedEvent newXObjectPageTypeEvent =
            new XObjectPageTypeCreatedEvent(document.getDocumentReference());
        getObservationManager().notify(newXObjectPageTypeEvent, source, getContext());
      } else if (isMenuItemDeleted(document, origDoc)) {
        LOGGER.debug("XObjectPageTypeDocumentUpdatedListener checkMenuItemDiffs"
            + " deleted from " + document.getDocumentReference() + "]");
        XObjectPageTypeDeletedEvent delXObjectPageTypeEvent = new
            XObjectPageTypeDeletedEvent(document.getDocumentReference());
        getObservationManager().notify(delXObjectPageTypeEvent, source, getContext());
      }
      if (isMenuItemUpdated(document, origDoc)) {
        LOGGER.debug("XObjectPageTypeDocumentUpdatedListener checkMenuItemDiffs updated on "
            + document.getDocumentReference() + "]");
        XObjectPageTypeUpdatedEvent updXObjectPageTypeEvent =
            new XObjectPageTypeUpdatedEvent(document.getDocumentReference());
        getObservationManager().notify(updXObjectPageTypeEvent, source, getContext());
      }
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }

  boolean isPageTypePropertiesAdded(XWikiDocument document, XWikiDocument origDoc) {
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
    LOGGER.trace("XObjectPageTypeDocumentUpdatedListener checkMenuItemDiffs menuItemObj ["
        + menuItemObj + "], menuItemOrigObj [" + menuItemOrigObj + "]");
    if ((menuItemObj != null) && (menuItemOrigObj != null)) {
      int newPos = menuItemObj.getIntValue(NavigationClasses.MENU_POSITION_FIELD, -1);
      int oldPos = menuItemOrigObj.getIntValue(NavigationClasses.MENU_POSITION_FIELD, -1);
      String newPart = menuItemObj.getStringValue(NavigationClasses.MENU_PART_FIELD);
      String oldPart = menuItemOrigObj.getStringValue(NavigationClasses.MENU_PART_FIELD);
      LOGGER.debug("XObjectPageTypeDocumentUpdatedListener checkMenuItemDiffs newPos ["
          + newPos + "], oldPos [" + oldPos + "]");
      if (newPos != oldPos) {
        return true;
      } else if (!StringUtils.equals(newPart, oldPart)) {
          return true;
      } else {
        DocumentReference parentRef = document.getParentReference();
        DocumentReference parentOrigRef = origDoc.getParentReference();
        LOGGER.debug("XObjectPageTypeDocumentUpdatedListener checkMenuItemDiffs parentRef ["
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
  protected Logger getLogger() {
    return LOGGER;
  }

}
