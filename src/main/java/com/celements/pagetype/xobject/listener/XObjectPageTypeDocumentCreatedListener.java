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
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.pagetype.xobject.event.XObjectPageTypeCreatedEvent;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(XObjectPageTypeDocumentCreatedListener.NAME)
public class XObjectPageTypeDocumentCreatedListener
    extends AbstractXObjectPageTypeDocumentListener implements EventListener {

  public static final String NAME = "XObjectPageTypeDocumentCreatedListener";

  private static Logger LOGGER = LoggerFactory.getLogger(
      XObjectPageTypeDocumentCreatedListener.class);

  @Requirement
  RemoteObservationManagerContext remoteObservationManagerContext;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public String getName() {
    return NAME;
  }

  public List<Event> getEvents() {
    return Arrays.asList((Event)new DocumentCreatedEvent());
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = (XWikiDocument) source;
    if ((document != null) && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      BaseObject pageTypePropObj = document.getXObject(getPageTypePropertiesClassRef(
          document));
      if (pageTypePropObj != null) {
        LOGGER.debug("XObjectPageTypeDocumentCreatedListener onEvent added to "
            + document.getDocumentReference() + "]");
        XObjectPageTypeCreatedEvent newXObjectPageTypeEvent =
            new XObjectPageTypeCreatedEvent(document.getDocumentReference());
        getObservationManager().notify(newXObjectPageTypeEvent, source, getContext());
      }
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source ["
          + source + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
