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

  private static Log LOGGER = LogFactory.getFactory().getInstance(TreeNodeCacheListener.class);

  @Requirement
  ITreeNodeCache treeNodeCache;

  @Override
  public String getName() {
    return "TreeNodeCacheListener";
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.asList((Event) new TreeNodeCreatedEvent(), (Event) new TreeNodeUpdatedEvent(),
        (Event) new TreeNodeDeletedEvent());
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = (XWikiDocument) source;
    if (document != null) {
      LOGGER.debug("onEvent: got event for [" + event.getClass() + "] on document ["
          + document.getDocumentReference() + "].");
      treeNodeCache.flushMenuItemCache();
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source [" + source
          + "] and data [" + data + "] -> skip.");
    }
  }

}
