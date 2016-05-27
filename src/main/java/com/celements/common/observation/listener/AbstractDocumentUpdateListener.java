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
package com.celements.common.observation.listener;

import java.util.Arrays;
import java.util.List;

import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.observation.event.Event;

import com.celements.copydoc.ICopyDocumentRole;
import com.google.common.base.Objects;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public abstract class AbstractDocumentUpdateListener extends AbstractDocumentListener {

  @Requirement
  protected ICopyDocumentRole copyDocService;

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(new DocumentUpdatingEvent(), new DocumentUpdatedEvent());
  }

  @Override
  protected Event getNotifyEvent(Event event, XWikiDocument doc) {
    Event notifyEvent = null;
    BaseObject bObj = getRequiredObj(doc);
    BaseObject origBObj = getRequiredObj(doc.getOriginalDocument());
    if ((bObj != null) && (origBObj == null)) {
      notifyEvent = getCreateEvent(event, doc.getDocumentReference());
    } else if ((bObj == null) && (origBObj != null)) {
      notifyEvent = getDeleteEvent(event, doc.getDocumentReference());
    } else if ((bObj != null) && (origBObj != null) && (copyDocService.checkObject(bObj, origBObj)
        || checkDocFields(doc))) {
      notifyEvent = getUpdateEvent(event, doc.getDocumentReference());
    }
    return notifyEvent;
  }

  private boolean checkDocFields(XWikiDocument doc) {
    boolean changed = false;
    XWikiDocument origDoc = doc.getOriginalDocument();
    if ((origDoc != null) && includeDocFields()) {
      changed |= !Objects.equal(doc.getTitle(), origDoc.getTitle());
      changed |= !Objects.equal(doc.getContent(), origDoc.getContent());
      getLogger().debug("checkDocFields: changed '{}'", changed);
    }
    return changed;
  }

  /**
   * @return true if doc fields (title and content) should also be checked for changes
   */
  protected abstract boolean includeDocFields();

  void injectCopyDocService(ICopyDocumentRole copyDocService) {
    this.copyDocService = copyDocService;
  }

}
