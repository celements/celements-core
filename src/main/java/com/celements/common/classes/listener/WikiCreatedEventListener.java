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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.WikiCreatedEvent;
import org.xwiki.bridge.event.WikiEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.classes.IClassesCompositorComponent;
import com.xpn.xwiki.XWikiContext;

@Component("celements.classes.WikiCreatedEventListener")
public class WikiCreatedEventListener implements EventListener {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      WikiCreatedEventListener.class);

  @Requirement
  IClassesCompositorComponent classesCompositor;

  @Requirement
  private Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public List<Event> getEvents() {
    LOGGER.info("getEvents: registering for wiki created events.");
    return Arrays.<Event>asList(new WikiCreatedEvent());
  }

  public String getName() {
    return "celements.classes.WikiCreatedEventListener";
  }

  public void onEvent(Event event, Object source, Object data) {
    if (event instanceof WikiCreatedEvent) {
      String saveDbName = getContext().getDatabase();
      try {
        WikiEvent wikiEvent = (WikiEvent) event;
        String newDbName = wikiEvent.getWikiId();
        LOGGER.info("new wiki created [" + newDbName + "]. Checking all Class"
            + " Collections.");
        getContext().setDatabase(newDbName);
        classesCompositor.checkAllClassCollections();
      } finally {
        getContext().setDatabase(saveDbName);
      }
    } else {
      LOGGER.warn("unrecognised event [" + event.getClass()
          + "] in classes.CompositorComonent.");
    }
  }

}
