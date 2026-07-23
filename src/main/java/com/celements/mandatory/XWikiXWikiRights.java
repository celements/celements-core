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

import static com.celements.rights.access.EAccessLevel.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.reference.RefBuilder;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.classes.oldcore.XWikiGlobalRightsClass;
import com.xpn.xwiki.XWikiConstant;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("celements.mandatory.wikirights")
public class XWikiXWikiRights extends AbstractMandatoryDocument {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

  boolean checkAccessRightObjs(XWikiDocument wikiPrefDoc) {
    boolean dirty = false;
    dirty |= checkGlobalRightObj(wikiPrefDoc, "XWiki.ContentEditorsGroup",
        List.of(EDIT, DELETE, UNDELETE));
    dirty |= checkGlobalRightObj(wikiPrefDoc, "XWiki.XWikiAdminGroup",
        List.of(ADMIN, EDIT, COMMENT, DELETE, UNDELETE, REGISTER));
    return dirty;
  }

  protected boolean checkGlobalRightObj(XWikiDocument doc, String group,
      List<EAccessLevel> levels) {
    var editor = XWikiObjectEditor.on(doc)
        .filter(XWikiGlobalRightsClass.CLASS_REF)
        .filter(XWikiGlobalRightsClass.FIELD_GROUPS, List.of(group))
        .filter(XWikiGlobalRightsClass.FIELD_ALLOW, true);
    if (!editor.fetch().exists()) {
      editor.filter(XWikiGlobalRightsClass.FIELD_LEVELS, levels)
          .createFirstIfNotExists();
      return true;
    }
    return false;
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

}
