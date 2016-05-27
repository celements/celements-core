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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.navigation.NavigationClasses;
import com.celements.navigation.event.TreeNodeDeletedEvent;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("TreeNodeDocumentDeletedListener")
public class TreeNodeDocumentDeletedListener extends AbstractTreeNodeDocumentListener implements
    EventListener {

  private static Logger LOGGER = LoggerFactory.getLogger(TreeNodeDocumentDeletedListener.class);

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement("celements.celNavigationClasses")
  IClassCollectionRole navClasses;

  @Requirement
  RemoteObservationManagerContext remoteObservationManagerContext;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  private NavigationClasses getNavClasses() {
    return (NavigationClasses) navClasses;
  }

  public String getName() {
    return "TreeNodeDocumentDeletedListener";
  }

  public List<Event> getEvents() {
    return Arrays.asList((Event) new DocumentDeletedEvent());
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = getOrginialDocument(source);
    if ((document != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      BaseObject menuItemObj = document.getXObject(getNavClasses().getMenuItemClassRef(
          getContext().getDatabase()));
      if (menuItemObj != null) {
        LOGGER.debug("TreeNodeDocumentDeletedListener checkMenuItemDiffs deleted from "
            + document.getDocumentReference() + "]");
        TreeNodeDeletedEvent delTreeNodeEvent = new TreeNodeDeletedEvent(
            document.getDocumentReference());
        getObservationManager().notify(delTreeNodeEvent, source, getContext());
      }
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source [" + source
          + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }

  XWikiDocument getOrginialDocument(Object source) {
    if (source != null) {
      return ((XWikiDocument) source).getOriginalDocument();
    }
    return null;
  }

  @Override
  protected IWebUtilsService getWebUtilsService() {
    return webUtilsService;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
