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
package com.celements.navigation.event.converter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.LocalEventData;
import org.xwiki.observation.remote.RemoteEventData;

import com.celements.navigation.event.TreeNodeCreatedEvent;
import com.celements.navigation.event.TreeNodeDeletedEvent;
import com.celements.navigation.event.TreeNodeUpdatedEvent;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.observation.remote.converter.AbstractXWikiEventConverter;

@Component("TreeNode")
public class TreeNodeEventConverter extends AbstractXWikiEventConverter {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      TreeNodeEventConverter.class);

  /**
   * The events supported by this converter.
   */
  private Set<Class<? extends Event>> events = new HashSet<Class<? extends Event>>() {
    private static final long serialVersionUID = 1L;
    {
      add(TreeNodeDeletedEvent.class);
      add(TreeNodeCreatedEvent.class);
      add(TreeNodeUpdatedEvent.class);
    }
  };

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.observation.remote.converter.LocalEventConverter#toRemote(
   *      org.xwiki.observation.remote.LocalEventData,
   *      org.xwiki.observation.remote.RemoteEventData)
   */
  public boolean toRemote(LocalEventData localEvent, RemoteEventData remoteEvent) {
    if (this.events.contains(localEvent.getEvent().getClass())) {
      LOGGER.trace("toRemote: serialize event [" + localEvent.getEvent().getClass()
          + "].");
      // fill the remote event
      remoteEvent.setEvent((Serializable) localEvent.getEvent());
      remoteEvent.setSource(serializeXWikiDocument(
          (XWikiDocument) localEvent.getSource()));
      remoteEvent.setData(serializeXWikiContext((XWikiContext) localEvent.getData()));
      return true;
    } else {
      LOGGER.debug("toRemote: skip event [" + localEvent.getEvent().getClass() + "].");
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.observation.remote.converter.RemoteEventConverter#fromRemote(
   *      org.xwiki.observation.remote.RemoteEventData,
   *      org.xwiki.observation.remote.LocalEventData)
   */
  public boolean fromRemote(RemoteEventData remoteEvent, LocalEventData localEvent) {
    LOGGER.trace("fromRemote: start for event [" + remoteEvent.getEvent().getClass()
        + "].");
    if (this.events.contains(remoteEvent.getEvent().getClass())) {
      LOGGER.trace("fromRemote: unserialize event [" + remoteEvent.getEvent().getClass()
          + "].");
      // fill the local event
      XWikiContext context = unserializeXWikiContext(remoteEvent.getData());
      if (context != null) {
        localEvent.setEvent((Event) remoteEvent.getEvent());
        localEvent.setSource(unserializeDocument(remoteEvent.getSource()));
        localEvent.setData(unserializeXWikiContext(remoteEvent.getData()));
      }
      return true;
    } else {
      LOGGER.debug("fromRemote: skip event [" + remoteEvent.getEvent().getClass() + "].");
    }
    return false;
  }

}
