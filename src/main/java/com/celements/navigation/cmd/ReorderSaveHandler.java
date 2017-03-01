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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.navigation.INavigationClassConfig;
import com.celements.sajson.AbstractEventHandler;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class ReorderSaveHandler extends AbstractEventHandler<EReorderLiteral> {

  private static Log LOGGER = LogFactory.getFactory().getInstance(ReorderSaveHandler.class);
  private XWikiContext context;
  private DocumentReference parentRef;
  private EReorderLiteral currentCommand;
  private Integer currentPos;
  private Set<EntityReference> dirtyParents;

  public ReorderSaveHandler(XWikiContext context) {
    this.context = context;
  }

  @Override
  public void closeEvent(EReorderLiteral literal) {
    LOGGER.debug("close event: " + literal.name());
  }

  @Override
  public void openEvent(EReorderLiteral literal) {
    LOGGER.debug("open event: " + literal.name());
    currentCommand = literal;
  }

  @Override
  public void readPropertyKey(String key) {
    LOGGER.debug("read property key: " + key);
    if (currentCommand == EReorderLiteral.PARENT_CHILDREN_PROPERTY) {
      String newParentFN = extractDocFN(key);
      DocumentReference newParentRef = convertToDocRef(newParentFN);
      if ((newParentRef != null) && context.getWiki().exists(newParentRef, context)) {
        parentRef = newParentRef;
      } else {
        parentRef = null;
        LOGGER.error("readPropertyKey: cannot load parentDocument [" + newParentFN + "].");
      }
      currentPos = 0;
    } else {
      throw new IllegalStateException("readPropertyKey: expecting ParentChildren but" + " found "
          + currentCommand);
    }
  }

  DocumentReference convertToDocRef(String docFN) {
    if (!StringUtils.isEmpty(docFN)) {
      return getWebUtils().resolveDocumentReference(docFN);
    }
    return null;
  }

  String extractDocFN(String param) {
    if (param.split(":").length > 2) {
      return param.split(":")[2];
    } else {
      return "";
    }
  }

  DocumentReference getParentReference() {
    return parentRef;
  }

  String getParentFN() {
    if (parentRef != null) {
      return getWebUtils().getRefLocalSerializer().serialize(parentRef);
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
   *
   * @param object
   */
  void inject_ParentRef(DocumentReference newParent) {
    parentRef = newParent;
  }

  /**
   * FOR TESTS ONLY!!!
   *
   * @param object
   */
  void inject_current(EReorderLiteral newCurrentCommand) {
    currentCommand = newCurrentCommand;
  }

  @Override
  public void stringEvent(String value) {
    LOGGER.debug("string event: " + value + " with parent " + getParentFN());
    if (currentCommand == EReorderLiteral.ELEMENT_ID) {
      String docFN = extractDocFN(value);
      DocumentReference docRef = convertToDocRef(docFN);
      if ((docRef != null) && context.getWiki().exists(docRef, context)) {
        try {
          boolean updateNeeded = false;
          XWikiDocument xdoc = context.getWiki().getDocument(docRef, context);
          if (hasDiffParentReferences(xdoc.getParentReference())) {
            markParentDirty(xdoc.getParentReference());
            xdoc.setParentReference(getRelativeParentReference());
            markParentDirty(getParentReference());
            updateNeeded = true;
          }
          BaseObject menuItemObj = xdoc.getXObject(getNavClassConfig().getMenuItemClassRef(
              context.getDatabase()));
          if ((menuItemObj != null) && (menuItemObj.getIntValue(
              "menu_position") != getCurrentPos())) {
            menuItemObj.setIntValue("menu_position", getCurrentPos());
            markParentDirty(xdoc.getParentReference());
            updateNeeded = true;
          }
          if (updateNeeded) {
            context.getWiki().saveDocument(xdoc, "Restructuring", context);
          }
        } catch (XWikiException e) {
          LOGGER.error("readPropertyKey: cannot load document [" + docFN + "].");
        }
        currentPos = getCurrentPos() + 1;
      } else {
        LOGGER.error("readPropertyKey: cannot load parentDocument [" + docFN + "].");
      }
    } else {
      throw new IllegalStateException("stringEvent: expecting element_id but" + " found ["
          + currentCommand + "] with parent [" + getParentFN() + "].");
    }
  }

  EntityReference getRelativeParentReference() {
    return getWebUtils().resolveRelativeEntityReference(getParentFN(), EntityType.DOCUMENT);
  }

  boolean hasDiffParentReferences(EntityReference parentReference) {
    if (getParentReference() != null) {
      return !getParentReference().equals(parentReference);
    } else if (parentReference != null) {
      return true;
    } else {
      return false;
    }
  }

  void markParentDirty(EntityReference parentRef) {
    getDirtyParents().add(parentRef);
  }

  public Set<EntityReference> getDirtyParents() {
    if (dirtyParents == null) {
      dirtyParents = new HashSet<>();
    }
    return dirtyParents;
  }

  private IWebUtilsService getWebUtils() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private INavigationClassConfig getNavClassConfig() {
    return Utils.getComponent(INavigationClassConfig.class);
  }

}
