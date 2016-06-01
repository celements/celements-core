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
package com.celements.web.utils;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.util.AbstractXWikiRunnable;

public class DocumentCreationWorker extends AbstractXWikiRunnable{
  private static Log mLogger = LogFactory.getFactory().getInstance(
      DocumentCreationWorker.class);
  
  private String space;
  private int docsCreated;
  private long startTime;
  private int workerId;
  private boolean isRunning;
  private XWikiContext context;
  
  public DocumentCreationWorker(String space, int workerId, XWikiContext context) {
    this((XWikiContext)context.clone());
    this.space = space;
    this.workerId = workerId;
  }

  private DocumentCreationWorker(XWikiContext context) {
    super("xwikicontext", context);
    this.context = context;
  }

  @Override
  protected void runInternal() {
    docsCreated = 0;
    startTime = (new Date()).getTime();
    isRunning = true;
    while(isRunning) {
      String docFullName = space + "." + startTime + "_" + workerId + "_" + docsCreated;
      boolean even = false;
      if((docsCreated + 1) % 2 == 0) {
        even = true;
      }
      boolean third = false;
      if((docsCreated + 1) % 3 == 0) {
        third = true;
      }
      boolean fifth = false;
      if((docsCreated + 1) % 5 == 0) {
        fifth = true;
      }
      if((docsCreated +1) % 20000 == 0) {
        context.getWiki().flushCache(context);
      }
      try {
        XWikiDocument doc = context.getWiki().getDocument(docFullName, context);
        doc.setContent("{pre}\nThe amazing content of " + docFullName + ". As number "
            + docsCreated + " this is a [" + (even?"even ":"") + (third?"third ":"") +
            (fifth?"fifeht ":"") + "] document.{/pre}");
        BaseObject mappedObj = doc.newObject("Classes.RTEConfigTypePropertiesClass", 
            context);
        long rnd1 = Math.round(Math.random()*99);
        long rnd2 = Math.round(Math.random()*999);
        String styles = "dividableBy1.css";
        if(even) {
          styles += " 'dividableBy2.css'";
        }
        if(third) {
          styles += " 'dividableBy3.css'";
        }
        if(fifth) {
          styles += " 'dividableBy5.css'";
        }
        mappedObj.setStringValue("styles", styles);
        mappedObj.setStringValue("plugins", "" + new Date());
        mappedObj.setStringValue("row_1", "none");
        mappedObj.setStringValue("row_2", rnd2 + " Lorem " + rnd1 + " ipsum");
        mappedObj.setStringValue("row_3", rnd2 + " dolor " + rnd1 + " sim");
        mappedObj.setStringValue("blockformats", rnd1 + " - " + rnd2);
        mappedObj.setStringValue("valid_elements", "Lorem " + rnd2 + " ipsum " + rnd1);
        mappedObj.setStringValue("invalid_elements", "dolor " + rnd2 + " sim " + rnd1);
        BaseObject mappedInnoDB = doc.newObject("Celements.NewsletterReceiverClass", 
            context);
        mappedInnoDB.setStringValue("email", "is even = " + even);
        int thirdNr = 0;
        if(third) {
          thirdNr = 1;
        }
        mappedInnoDB.setIntValue("isactive", thirdNr);
        mappedInnoDB.setStringValue("subscribed", "blabla " + rnd1 + " " + rnd2 + " is " +
            "third = " + third);
        BaseObject unmappedObj = doc.newObject("Classes.FilebaseTag", context);
        unmappedObj.setStringValue("attachment", styles + " - dolor " + rnd2 + " sim " + 
            rnd1);
        context.getWiki().saveDocument(doc, context);
        docsCreated++;
      } catch (XWikiException e) {
        mLogger.error(context.getWiki() + " - " + docFullName, e);
      } catch (NullPointerException npe) {
        mLogger.error(context.getWiki() + " - " + docFullName, npe);
      }
    }
    mLogger.error("thread " + getCelId() + " stopped! isRunning=" + isRunning);
  }
  
  public synchronized boolean isRunning() {
    return isRunning;
  }
  
  public synchronized void stopThread() {
    isRunning = false;;
  }
  
  public int getCelId() {
    return workerId;
  }

  public Long[] getStats() {
    long time = (new Date()).getTime() - startTime;
    return new Long[] {(long)docsCreated, time};
  }
}
