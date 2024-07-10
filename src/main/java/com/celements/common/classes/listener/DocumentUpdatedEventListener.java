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

import static com.celements.execution.XWikiExecutionProp.*;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.classes.IClassesCompositorComponent;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("celements.classes.DocumentUpdatedEventListener")
public class DocumentUpdatedEventListener implements EventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      DocumentUpdatedEventListener.class);

  @Requirement
  IClassesCompositorComponent classesCompositor;

  @Requirement
  private Execution execution;

  protected ExecutionContext getEContext() {
    return execution.getContext();
  }

  protected XWikiContext getContext() {
    return getEContext().get(XWIKI_CONTEXT).orElseThrow();
  }

  @Override
  public List<Event> getEvents() {
    LOGGER.info("getEvents: registering for document update events.");
    return Arrays.<Event>asList(new DocumentUpdatedEvent());
  }

  @Override
  public String getName() {
    return "celements.classes.DocumentUpdatedEventListener";
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    if (source instanceof XWikiDocument) {
      XWikiDocument document = (XWikiDocument) source;
      DocumentReference xwikiPrefDoc = new DocumentReference(
          document.getDocumentReference().getLastSpaceReference().getParent().getName(), "XWiki",
          "XWikiPreferences");
      if (document.getDocumentReference().equals(xwikiPrefDoc) && checkCycle()) {
        LOGGER.info("changes on [" + xwikiPrefDoc + "] saved. Checking all Class Collections.");
        classesCompositor.checkClasses();
      } else {
        LOGGER.trace("changes on [" + xwikiPrefDoc
            + "] saved. NOT checking all Class Collections.");
      }
    } else {
      LOGGER.warn("unrecognised event [" + event.getClass() + "] in classes.CompositorComonent.");
    }
  }

  private boolean checkCycle() {
    if (Boolean.TRUE.equals(getEContext().getProperty(getName()))) {
      LOGGER.warn("cycle detected in [" + getName() + "].", new Throwable());
      return true;
    }
    getEContext().setProperty(getName(), true);
    return false;
  }

}
