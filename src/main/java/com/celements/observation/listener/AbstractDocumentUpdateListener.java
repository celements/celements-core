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
package com.celements.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.observation.event.Event;

import com.celements.copydoc.ICopyDocumentRole;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public abstract class AbstractDocumentUpdateListener extends AbstractDocumentListener {

  @Requirement
  private ICopyDocumentRole copyDocService;

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(new DocumentUpdatingEvent(), new DocumentUpdatedEvent());
  }

  @Override
  protected Event getNotifyEvent(Event event, XWikiDocument doc) {
    Event notifyEvent = null;
    BaseObject bObj = getRequiredObj(doc);
    BaseObject origBObj = getRequiredObj(doc.getOriginalDocument());
    if (isEventCreate(bObj, origBObj)) {
      if (isEventING(event)) {
        notifyEvent = getCreatingEvent(bObj.getDocumentReference());
      } else if (isEventED(event)) {
        notifyEvent = getCreatedEvent(bObj.getDocumentReference());
      }
    } else if (isEventDelete(bObj, origBObj)) {
      if (isEventING(event)) {
        notifyEvent = getDeletingEvent(origBObj.getDocumentReference());
      } else if (isEventED(event)) {
        notifyEvent = getDeletedEvent(origBObj.getDocumentReference());
      }
    } else if (isEventUpdate(bObj, origBObj)) {
      if (isEventING(event)) {
        notifyEvent = getUpdatingEvent(bObj.getDocumentReference());
      } else if (isEventED(event)) {
        notifyEvent = getUpdatedEvent(bObj.getDocumentReference());
      }
    }
    return notifyEvent;
  }

  private boolean isEventING(Event event) {
    return (event instanceof DocumentUpdatingEvent);
  }

  private boolean isEventED(Event event) {
    return (event instanceof DocumentUpdatedEvent);
  }

  protected boolean isEventCreate(BaseObject bObj, BaseObject origBObj) {
    return (bObj != null) && (origBObj == null);
  }

  protected boolean isEventDelete(BaseObject bObj, BaseObject origBObj) {
    return (bObj == null) && (origBObj != null);
  }

  protected boolean isEventUpdate(BaseObject bObj, BaseObject origBObj) {
    return (bObj != null) && (origBObj != null) && checkUpdateFields(bObj, origBObj);
  }

  private boolean checkUpdateFields(BaseObject bObj, BaseObject origBObj) {
    boolean changed = false;
    for (String name : getRequiredUpdateFields()) {
      Object val = copyDocService.getValue(bObj, name);
      Object origVal = copyDocService.getValue(origBObj, name);
      if (!ObjectUtils.equals(val, origVal)) {
        changed = true;
        break;
      }
    }
    return changed;
  }

  /**
   * @return the names of the fields required to be changed on the required object on the
   * triggered doc to qualify for notifying update events
   */
  protected abstract List<String> getRequiredUpdateFields();

  void injectCopyDocService(ICopyDocumentRole copyDocService) {
    this.copyDocService = copyDocService;
  }

}
