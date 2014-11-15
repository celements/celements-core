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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.pagetype.xobject.event.XObjectPageTypeDeletedEvent;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("XObjectPageTypeDocumentDeletedListener")
public class XObjectPageTypeDocumentDeletedListener
    extends AbstractXObjectPageTypeDocumentListener implements EventListener {

  private static Logger LOGGER = LoggerFactory.getLogger(
      XObjectPageTypeDocumentDeletedListener.class);

  @Requirement
  private ComponentManager componentManager;

  @Requirement
  IPageTypeClassConfig pageTypeClassConfig;

  @Requirement
  RemoteObservationManagerContext remoteObservationManagerContext;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public String getName() {
    return "XObjectPageTypeDocumentDeletedListener";
  }

  public List<Event> getEvents() {
    return Arrays.asList((Event)new DocumentDeletedEvent());
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = getOrginialDocument(source);
    if ((document != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      BaseObject pageTypePropObj = document.getXObject(
          pageTypeClassConfig.getPageTypePropertiesClassRef(getContext().getDatabase()));
      if (pageTypePropObj != null) {
        LOGGER.debug("XObjectPageTypeDocumentDeletedListener onEvent deleted from "
            + document.getDocumentReference() + "]");
        XObjectPageTypeDeletedEvent delXObjectPageTypeEvent =
            new XObjectPageTypeDeletedEvent(document.getDocumentReference());
        getObservationManager().notify(delXObjectPageTypeEvent, source, getContext());
      }
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
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
  protected ComponentManager getComponentManager() {
    return componentManager;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
