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
package com.celements.mandatory.listener;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.WikiCreatedEvent;
import org.xwiki.bridge.event.WikiEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.mandatory.IMandatoryDocumentCompositorRole;
import com.xpn.xwiki.XWikiContext;

@Component(WikiCreatedEventListener.NAME)
public class WikiCreatedEventListener implements EventListener {

  private static Logger LOGGER = LoggerFactory.getLogger(WikiCreatedEventListener.class);

  public static final String NAME = "celements.mandatory.WikiCreatedEventListener";

  @Requirement
  IMandatoryDocumentCompositorRole mandatoryDocCmp;

  @Requirement
  RemoteObservationManagerContext remoteObservationManagerContext;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    LOGGER.info("getEvents: registering for wiki created events.");
    return Arrays.<Event>asList(new WikiCreatedEvent());
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    WikiEvent wikiEvent = (WikiEvent) event;
    String database = wikiEvent.getWikiId();
    LOGGER.debug("received WikiCreatedEvent for database '{}', remote state '{}'", 
        database, remoteObservationManagerContext.isRemoteState());
    if (!remoteObservationManagerContext.isRemoteState()) {
      String dbBackup = getContext().getDatabase();
      try {
        LOGGER.info("checking all mandatory documents for db '{}'", database);
        getContext().setDatabase(database);
        mandatoryDocCmp.checkAllMandatoryDocuments();
      } finally {
        getContext().setDatabase(dbBackup);
      }
    }
  }

}
