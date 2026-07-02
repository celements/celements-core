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
package com.celements.mandatory;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.ClassDefinition;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.reference.RefBuilder;
import com.celements.web.classes.oldcore.XWikiGlobalRightsClass;
import com.xpn.xwiki.XWikiConstant;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("celements.mandatory.wikirights")
public class XWikiXWikiRights extends AbstractMandatoryDocument {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Requirement(XWikiGlobalRightsClass.CLASS_DEF_HINT)
  private ClassDefinition globalRightsClass;

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return List.of("celements.MandatoryGroups");
  }

  @Override
  public String getName() {
    return "CelementsXWikiRights";
  }

  @Override
  protected DocumentReference getDocRef() {
    return new RefBuilder().with(modelContext.getWikiRef())
        .space(XWikiConstant.XWIKI_SPACE)
        .doc(XWikiConstant.XWIKI_PREF_DOC_NAME)
        .build(DocumentReference.class);
  }

  @Override
  protected boolean skip() {
    return false;
  }

  @Override
  protected boolean checkDocuments(XWikiDocument doc) throws XWikiException {
    return checkAccessRightObjs(doc);
  }

  @Override
  protected boolean checkDocumentsMain(XWikiDocument doc) throws XWikiException {
    return checkAccessRightObjs(doc);
  }

  boolean checkAccessRightObjs(XWikiDocument wikiPrefDoc) throws XWikiException {
    boolean dirty = false;
    dirty |= checkGlobalRightObj(wikiPrefDoc, "XWiki.ContentEditorsGroup", "edit,delete,undelete");
    dirty |= checkGlobalRightObj(wikiPrefDoc, "XWiki.XWikiAdminGroup",
        "admin,edit,comment,delete,undelete,register");
    return dirty;
  }

  protected boolean checkGlobalRightObj(XWikiDocument wikiPrefDoc, String groupFN, String levels) {
    var editor = XWikiObjectEditor.on(wikiPrefDoc).filter(globalRightsClass);
    if (editor.fetch().filter(obj -> hasGlobalRights(obj, groupFN, levels)).exists()) {
      return false;
    }
    BaseObject rightsObj = editor.createFirst();
    rightsObj.setStringValue("groups", groupFN);
    rightsObj.setStringValue("levels", levels);
    rightsObj.setStringValue("users", "");
    rightsObj.setIntValue("allow", 1);
    logger.debug("XWikiGlobalRights added missing [{}] for database [{}].", groupFN, getWiki());
    return true;
  }

  private boolean hasGlobalRights(BaseObject obj, String groupFN, String levels) {
    return (obj.getIntValue("allow", 0) == 1)
        && groupFN.equals(obj.getStringValue("groups"))
        && levels.equals(obj.getStringValue("levels"))
        && "".equals(obj.getStringValue("users"));
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

}
