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
package com.celements.navigation.cmd;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.sajson.AbstractEventHandler;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class ReorderSaveHandler extends AbstractEventHandler<EReorderLiteral>{

  private static Log mLogger = LogFactory.getFactory().getInstance(
      ReorderSaveHandler.class);
  private XWikiContext context;
  private String parentFN;
  private EReorderLiteral currentCommand;
  private Integer currentPos;
  private boolean flushCacheNeeded;
  private Set<String> dirtyParents;

  public ReorderSaveHandler(XWikiContext context) {
    this.context = context;
  }

  public void closeEvent(EReorderLiteral literal) {
    mLogger.debug("close event: " + literal.name());
  }

  public void openEvent(EReorderLiteral literal) {
    mLogger.debug("open event: " + literal.name());
      currentCommand = literal;
  }

  public void readPropertyKey(String key) {
    mLogger.debug("read property key: " + key);
    if (currentCommand == EReorderLiteral.PARENT_CHILDREN_PROPERTY) {
      String newParentFN = extractDocFN(key);
      if (context.getWiki().exists(newParentFN, context)) {
        parentFN = newParentFN;
      } else {
        parentFN = null;
        mLogger.error("readPropertyKey: cannot load parentDocument [" + newParentFN 
            + "].");
      }
      currentPos = 0;
    } else {
      throw new IllegalStateException("readPropertyKey: expecting ParentChildren but"
          + " found " + currentCommand);
    }
  }

  String extractDocFN(String param) {
    if (param.split(":").length > 1) {
      return param.split(":")[1];
    } else {
      return "";
    }
  }

  String getParentFN() {
    if (parentFN != null) {
      return parentFN;
    }
    return "";
  }

  Integer getCurrentPos() {
    if (currentPos != null) {
      return currentPos;
    }
    return 0;
  }

  /**
   * FOR TESTS ONLY!!!
   * @param object 
   */
  void inject_ParentFN(String newParent) {
    parentFN = newParent;
  }

  /**
   * FOR TESTS ONLY!!!
   * @param object 
   */
  void inject_current(EReorderLiteral newCurrentCommand) {
    currentCommand = newCurrentCommand;
  }

  public void stringEvent(String value) {
    mLogger.debug("string event: " + value + " with parent " + getParentFN());
    if (currentCommand == EReorderLiteral.ELEMENT_ID) {
      String docFN = extractDocFN(value);
      if (context.getWiki().exists(docFN, context)) {
        try {
          boolean updateNeeded = false;
          XWikiDocument xdoc = context.getWiki().getDocument(docFN, context);
          if (!getParentFN().equals(xdoc.getParent())) {
            markParentDirty(xdoc.getParent());
            xdoc.setParent(getParentFN());
            markParentDirty(getParentFN());
            updateNeeded = true;
          }
          BaseObject menuItemObj = xdoc.getObject("Celements2.MenuItem");
          if ((menuItemObj != null)
              && (menuItemObj.getIntValue("menu_position") != getCurrentPos())) {
            menuItemObj.setIntValue("menu_position", getCurrentPos());
            markParentDirty(xdoc.getParent());
            updateNeeded = true;
          }
          if (updateNeeded) {
            context.getWiki().saveDocument(xdoc, "Restructuring", context);
            setFlushCacheNeeded();
          }
        } catch (XWikiException e) {
          mLogger.error("readPropertyKey: cannot load document [" + docFN 
              + "].");
        }
        currentPos = getCurrentPos() + 1;
      } else {
        mLogger.error("readPropertyKey: cannot load parentDocument [" + docFN 
            + "].");
      }
    } else {
      throw new IllegalStateException("stringEvent: expecting element_id but"
          + " found [" + currentCommand + "] with parent [" + getParentFN() + "].");
    }
  }

  void markParentDirty(String parent) {
    getDirtyParents().add(parent);
  }

  void setFlushCacheNeeded() {
    this.flushCacheNeeded = true;
  }

  boolean isFlushCacheNeeded() {
    return flushCacheNeeded;
  }

  public Set<String> getDirtyParents() {
    if (dirtyParents == null) {
      dirtyParents = new HashSet<String>();
    }
    return dirtyParents;
  }

}
