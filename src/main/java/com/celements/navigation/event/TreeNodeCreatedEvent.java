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
package com.celements.navigation.event;

import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.filter.EventFilter;

import com.celements.common.observation.converter.Remote;

@Remote
public class TreeNodeCreatedEvent extends AbstractDocumentEvent {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor initializing the event filter with an
   * {@link org.xwiki.observation.event.filter.AlwaysMatchingEventFilter}, meaning that
   * this event will match any other document delete event.
   */
  public TreeNodeCreatedEvent() {
    super();
  }

  /**
   * Constructor initializing the event filter with a
   * {@link org.xwiki.observation.event.filter.FixedNameEventFilter}, meaning that this
   * event will match only delete events affecting the same document.
   * 
   * @param documentReference
   *          the reference of the document to match
   */
  public TreeNodeCreatedEvent(DocumentReference documentReference) {
    super(documentReference);
  }

  /**
   * Constructor using a custom {@link EventFilter}.
   * 
   * @param eventFilter
   *          the filter to use for matching events
   */
  public TreeNodeCreatedEvent(EventFilter eventFilter) {
    super(eventFilter);
  }
}
