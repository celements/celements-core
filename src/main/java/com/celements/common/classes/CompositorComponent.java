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
package com.celements.common.classes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("CompositerComponent")
public class CompositorComponent implements EventListener {

  @Requirement
  private Map<String, ICelementsClassCollection> classCollectionMap;
  
  @Requirement
  private Execution execution;

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CompositorComponent.class);
  
  public List<Event> getEvents() {
    mLogger.info("getEvents: registering for update, save and delete events.");
    return Arrays.<Event> asList(new DocumentUpdatedEvent());
  }

  public String getName() {
    return "CompositerComponent";
  }

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument document = (XWikiDocument) source;
    DocumentReference xwikiPrefDoc = new DocumentReference(document.getDocumentReference(
        ).getWikiReference().getName(), "XWiki", "XWikiPreferences");
    if(document.getDocumentReference().equals(xwikiPrefDoc)) {
      checkClassCollections();
    }
  }

  public void checkClassCollections() {
    ExecutionContext exContext = execution.getContext();
    XWikiContext context = (XWikiContext)exContext.getProperty("xwikicontext");
    for (ICelementsClassCollection classCollection : classCollectionMap.values()) {
      try {
        classCollection.runUpdate(context);
      } catch (XWikiException e) {
        mLogger.error("Exception checking class collection " 
            + classCollection.getConfigName(), e);
      }
    }
    
  }

}
