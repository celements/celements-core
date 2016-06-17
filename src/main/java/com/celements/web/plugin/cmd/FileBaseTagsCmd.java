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
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.service.ITreeNodeService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class FileBaseTagsCmd {

  private final static Logger Logger = LoggerFactory.getLogger(FileBaseTagsCmd.class);

  public static final String FILEBASE_TAG_CLASS = "Classes.FilebaseTag";
  private ITreeNodeService treeNodeSrv = Utils.getComponent(ITreeNodeService.class);

  public List<TreeNode> getAllFileBaseTags(XWikiContext context) {
    return treeNodeSrv.getSubNodesForParent("", getTagSpaceName(context),
        new InternalRightsFilter());
  }

  public String getTagSpaceName(XWikiContext context) {
    String centralFileBaseName = getCentralFileBaseFullName(context) + ".";
    return centralFileBaseName.substring(0, centralFileBaseName.indexOf('.'));
  }

  public String getCentralFileBaseFullName(XWikiContext context) {
    return context.getWiki().getSpacePreference("cel_centralfilebase", "", context);
  }

  public boolean existsTagWithName(String tagName, XWikiContext context) {
    if (context.getWiki().exists(getTagFullName(tagName, context), context)) {
      DocumentReference tagDocRef = getTagDocRef(tagName, context);
      for (TreeNode node : getAllFileBaseTags(context)) {
        if (tagDocRef.equals(node.getDocumentReference())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @deprecated instead use getTagDocRef
   */
  @Deprecated
  public String getTagFullName(String tagName, XWikiContext context) {
    return getTagSpaceName(context) + "." + tagName;
  }

  public DocumentReference getTagDocRef(String tagName, XWikiContext context) {
    return new DocumentReference(context.getDatabase(), getTagSpaceName(context), tagName);
  }

  public XWikiDocument getTagDocument(String tagName, boolean createIfNotExists,
      XWikiContext context) {
    XWikiDocument tagDoc = null;
    try {
      // FIXME change to modelAccess
      tagDoc = context.getWiki().getDocument(getTagFullName(tagName, context), context);
      if (!existsTagWithName(tagName, context)) {
        if (createIfNotExists) {
          BaseObject menuItemObj = tagDoc.newObject("Celements2.MenuItem", context);
          menuItemObj.setIntValue("menu_position", getAllFileBaseTags(context).size());
          menuItemObj.setStringValue("menu_parent", "");
          menuItemObj.setStringValue("part_name", "");
          context.getWiki().saveDocument(tagDoc, "Added by Navigation", context);
        }
      }
    } catch (XWikiException exp) {
      Logger.error("Failed to get tag document [" + getTagFullName(tagName, context) + "].", exp);
    }
    return tagDoc;
  }

  void inject_treeNodeSrv(ITreeNodeService mockTreeNodeSrv) {
    this.treeNodeSrv = mockTreeNodeSrv;
  }

}
