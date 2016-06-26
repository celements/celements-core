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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.service.ITreeNodeService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class FileBaseTagsCmd {

  private final static Logger LOGGER = LoggerFactory.getLogger(FileBaseTagsCmd.class);

  public final static String FILEBASE_TAG_CLASS = "Classes.FilebaseTag";
  private final ITreeNodeService treeNodeSrv = Utils.getComponent(ITreeNodeService.class);
  private final IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
  private final INavigationClassConfig navClassConfig = Utils.getComponent(
      INavigationClassConfig.class);
  private final Execution execution = Utils.getComponent(Execution.class);

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  /**
   * @deprecated since 1.141 instead use getAllFileBaseTags()
   */
  @Deprecated
  public List<TreeNode> getAllFileBaseTags(XWikiContext context) {
    return getAllFileBaseTags();
  }

  public List<TreeNode> getAllFileBaseTags() {
    return treeNodeSrv.getSubNodesForParent(getTagSpaceRef(), new InternalRightsFilter());
  }

  /**
   * @deprecated since 1.141 instead use getTagSpaceRef()
   */
  @Deprecated
  public String getTagSpaceName(XWikiContext context) {
    return getTagSpaceRef().getName();
  }

  public SpaceReference getTagSpaceRef() {
    String centralFileBaseName = getCentralFileBaseFullName() + ".";
    String spaceName = centralFileBaseName.substring(0, centralFileBaseName.indexOf('.'));
    return new SpaceReference(spaceName, new WikiReference(getContext().getDatabase()));
  }

  /**
   * @deprecated since 1.141 instead use getCentralFileBaseFullName()
   */
  @Deprecated
  public String getCentralFileBaseFullName(XWikiContext context) {
    return getCentralFileBaseFullName();
  }

  public String getCentralFileBaseFullName() {
    return getContext().getWiki().getSpacePreference("cel_centralfilebase", "", getContext());
  }

  /**
   * @deprecated since 1.141 instead use existsTagWithName(String)
   */
  @Deprecated
  public boolean existsTagWithName(String tagName, XWikiContext context) {
    return existsTagWithName(tagName);
  }

  public boolean existsTagWithName(String tagName) {
    if (modelAccess.exists(getTagDocRef(tagName))) {
      DocumentReference tagDocRef = getTagDocRef(tagName);
      for (TreeNode node : getAllFileBaseTags()) {
        if (tagDocRef.equals(node.getDocumentReference())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @deprecated since 1.140 instead use getTagDocRef(String)
   */
  @Deprecated
  public String getTagFullName(String tagName, XWikiContext context) {
    return getTagSpaceName(context) + "." + tagName;
  }

  /**
   * @deprecated since 1.141 instead use getTagDocRef(String)
   */
  @Deprecated
  public DocumentReference getTagDocRef(String tagName, XWikiContext context) {
    return getTagDocRef(tagName);
  }

  public DocumentReference getTagDocRef(String tagName) {
    return new DocumentReference(getContext().getDatabase(), getTagSpaceRef().getName(), tagName);
  }

  /**
   * @deprecated since 1.141 instead use getOrCreateTagDocument(String, boolean)
   */
  @Deprecated
  public XWikiDocument getTagDocument(String tagName, boolean createIfNotExists,
      XWikiContext context) {
    try {
      return getOrCreateTagDocument(tagName, createIfNotExists);
    } catch (FailedToCreateTagException exp) {
      LOGGER.warn("deprecated getTagDocument usage.", exp);
    }
    return null;
  }

  public XWikiDocument getOrCreateTagDocument(String tagName, boolean createIfNotExists)
      throws FailedToCreateTagException {
    XWikiDocument tagDoc = null;
    try {
      if (!existsTagWithName(tagName) && createIfNotExists) {
        tagDoc = modelAccess.getOrCreateDocument(getTagDocRef(tagName));
        BaseObject menuItemObj = modelAccess.getOrCreateXObject(tagDoc,
            navClassConfig.getMenuItemClassRef());
        menuItemObj.setIntValue(INavigationClassConfig.MENU_POSITION_FIELD,
            getAllFileBaseTags().size());
        menuItemObj.setStringValue("menu_parent", "");
        menuItemObj.setStringValue(INavigationClassConfig.PART_NAME_FIELD, "");
        modelAccess.saveDocument(tagDoc, "Added by Navigation");
      }
    } catch (DocumentSaveException exp) {
      LOGGER.error("Failed to save document [{}].", getTagDocRef(tagName), exp);
    }
    try {
      return modelAccess.getDocument(getTagDocRef(tagName));
    } catch (DocumentNotExistsException exp) {
      LOGGER.info("Failed to get tag document [{}].", getTagDocRef(tagName), exp);
      throw new FailedToCreateTagException("Failed to get tag document [" + getTagDocRef(tagName)
          + "].", exp);
    }
  }

}
