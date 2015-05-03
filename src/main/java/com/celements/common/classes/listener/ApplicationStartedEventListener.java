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
package com.celements.common.classes.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.common.classes.IClassesCompositorComponent;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component(ApplicationStartedEventListener.NAME)
public class ApplicationStartedEventListener implements EventListener {

  private static Logger LOGGER = LoggerFactory.getLogger(
      ApplicationStartedEventListener.class);

  public static final String NAME = "celements.classes.ApplicationStartedEventListener";

  @Requirement
  IClassesCompositorComponent classesCompositor;

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
    LOGGER.info("getEvents: registering for application started events");
    return Arrays.<Event>asList(new ApplicationStartedEvent());
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    LOGGER.debug("received ApplicationStartedEvent, remote state '{}', checkOnStart '{}'",
        remoteObservationManagerContext.isRemoteState(), checkOnStart());
    if (!remoteObservationManagerContext.isRemoteState() && checkOnStart()) {
      String dbBackup = getContext().getDatabase();
      try {
        for (String database : getAllDatabases()) {
          LOGGER.info("checking all class collections for db '{}'", database);
          getContext().setDatabase(database);
          classesCompositor.checkAllClassCollections();
        }
      } finally {
        getContext().setDatabase(dbBackup);
      }
    }
  }

  private boolean checkOnStart() {
    return getContext().getWiki().ParamAsLong("celements.classCollections.checkOnStart", 
        1L) == 1L;
  }

  private List<String> getAllDatabases() {
    List<String> ret = new ArrayList<String>();
    try {
      ret.add(getContext().getMainXWiki());
      if (getContext().getWiki().isVirtualMode()) {
        ret.addAll(getContext().getWiki().getVirtualWikisDatabaseNames(getContext()));
      }
    } catch (XWikiException xwe) {
      LOGGER.error("Error getting virtual wiki names", xwe);
    }
    return ret;
  }

}
