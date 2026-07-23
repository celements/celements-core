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

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.classes.IClassesCompositorComponent;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class XWikiPreferencesClassActivationListener implements EventListener {

  public static final String NAME = "celements.classes.XWikiPreferencesClassActivationListener";

  private static final Logger LOGGER = LoggerFactory.getLogger(
      XWikiPreferencesClassActivationListener.class);

  private final IClassesCompositorComponent classesCompositor;
  private final Execution execution;

  @Inject
  public XWikiPreferencesClassActivationListener(
      IClassesCompositorComponent classesCompositor,
      Execution execution) {
    this.classesCompositor = classesCompositor;
    this.execution = execution;
  }

  protected ExecutionContext getEContext() {
    return execution.getContext();
  }

  @Override
  public List<Event> getEvents() {
    LOGGER.info("getEvents: registering for document update events.");
    return List.of(new DocumentUpdatedEvent());
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    if (source instanceof XWikiDocument) {
      XWikiDocument document = (XWikiDocument) source;
      DocumentReference xwikiPrefDoc = new DocumentReference(
          document.getDocumentReference().getLastSpaceReference().getParent().getName(), "XWiki",
          "XWikiPreferences");
      if (!document.getDocumentReference().equals(xwikiPrefDoc)) {
        LOGGER.trace("changes on [{}] saved. NOT checking all Class Collections", xwikiPrefDoc);
      } else if (Boolean.TRUE.equals(getEContext().getProperty(NAME))) {
        LOGGER.debug("cycle detected in [{}], skipping", NAME, new Throwable());
      } else {
        getEContext().setProperty(NAME, true);
        try {
          LOGGER.info("changes on [{}] saved. Checking all Class Collections", xwikiPrefDoc);
          classesCompositor.checkClasses();
        } finally {
          getEContext().removeProperty(NAME);
        }
      }
    } else {
      LOGGER.warn("unrecognised event [{}] in classes.CompositorComponent", event.getClass());
    }
  }

}
