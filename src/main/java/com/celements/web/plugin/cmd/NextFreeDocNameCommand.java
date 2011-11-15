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
package com.celements.web.plugin.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

public class NextFreeDocNameCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      NextFreeDocNameCommand.class);

  public String getNextUntitledPageFullName(String space,
      XWikiContext context) {
    return createUntitledPageFullName(
        getNextTitledPageNum(space, "untitled", context), space, "untitled");
  }
  
  public String getNextTitledPageFullName(String space, String title,
      XWikiContext context) {
    return createUntitledPageFullName(getNextTitledPageNum(space, title, context), space,
        title);
  }

  public DocumentReference getNextTitledPageDocRef(String space, String title,
      XWikiContext context) {
    return createUntitledPageDocRef(getNextTitledPageNum(space, title, context), space,
        title, context);
  }

  private long getNextTitledPageNum(String space, String title, XWikiContext context) {
    long num = 1;
    while (!isAvailableDocRef(context, createUntitledPageDocRef(num, space, title,
        context))) {
      num += 1;
    }
    return num;
  }

  private boolean isAvailableDocRef(XWikiContext context, DocumentReference newDocRef) {
    try {
      return (!context.getWiki().exists(newDocRef, context)
          && (context.getWiki().getDocument(newDocRef, context).getLock(context) == null)
          );
    } catch (XWikiException exp) {
      mLogger.info("Failed to check new document reference [" + newDocRef + "].", exp);
    }
    return false;
  }

  private String createUntitledPageFullName(long num, String space, String title) {
    return space + "." + createUntitledPageName(title, num);
  }

  private DocumentReference createUntitledPageDocRef(long num, String space, String title,
      XWikiContext context) {
    return new DocumentReference(context.getDatabase(), space, createUntitledPageName(
        title, num));
  }

  private String createUntitledPageName(String title, long num) {
    return title + num;
  }

  public String getNextUntitledPageName(String space, XWikiContext context) {
    return createUntitledPageName("untitled", getNextTitledPageNum(space, "untitled",
        context));
  }
}
