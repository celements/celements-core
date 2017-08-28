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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.ClassDefinition;
import com.celements.web.classes.oldcore.XWikiGlobalRightsClass;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("celements.mandatory.wikirights")
public class XWikiXWikiRights extends AbstractMandatoryDocument {

  private static final Logger LOGGER = LoggerFactory.getLogger(XWikiXWikiRights.class);

  @Requirement(XWikiGlobalRightsClass.CLASS_DEF_HINT)
  private ClassDefinition globalRightsClass;

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return Arrays.asList("celements.MandatoryGroups");
  }

  @Override
  public String getName() {
    return "CelementsXWikiRights";
  }

  @Override
  protected DocumentReference getDocRef() {
    return new DocumentReference(getWiki(), "XWiki", "XWikiPreferences");
  }

  @Override
  protected boolean skip() {
    return getContext().getWiki().ParamAsLong("celements.mandatory.skipWikiRights", 0) == 1L;
  }

  @Override
  protected boolean checkDocuments(XWikiDocument doc) throws XWikiException {
    return checkAccessRights(doc);
  }

  @Override
  protected boolean checkDocumentsMain(XWikiDocument doc) throws XWikiException {
    return checkAccessRights(doc);
  }

  boolean checkAccessRights(XWikiDocument wikiPrefDoc) throws XWikiException {
    BaseObject editRightsObj = wikiPrefDoc.getXObject(getGlobalRightsRef(), false, getContext());
    if (editRightsObj == null) {
      LOGGER.trace("checkAccessRights [" + getWiki() + "], global rights class exists: "
          + getContext().getWiki().exists(getGlobalRightsRef(), getContext()));
      LOGGER.trace("checkAccessRights [" + getWiki() + "], XWiki.ContentEditorsGroup" + " exists: "
          + getContext().getWiki().exists(new DocumentReference(getWiki(), "XWiki",
              "ContentEditorsGroup"), getContext()));
      editRightsObj = wikiPrefDoc.newXObject(getGlobalRightsRef(), getContext());
      editRightsObj.set("groups", "XWiki.ContentEditorsGroup", getContext());
      editRightsObj.set("levels", "edit,delete,undelete", getContext());
      editRightsObj.set("users", "", getContext());
      editRightsObj.set("allow", 1, getContext());
      BaseObject adminRightsObj = wikiPrefDoc.newXObject(getGlobalRightsRef(), getContext());
      LOGGER.trace("checkAccessRights [" + getWiki() + "], XWiki.ContentEditorsGroup" + " exists: "
          + getContext().getWiki().exists(new DocumentReference(getWiki(), "XWiki",
              "XWikiAdminGroup"), getContext()));
      adminRightsObj.set("groups", "XWiki.XWikiAdminGroup", getContext());
      adminRightsObj.set("levels", "admin,edit,comment,delete,undelete,register", getContext());
      adminRightsObj.set("users", "", getContext());
      adminRightsObj.set("allow", 1, getContext());
      LOGGER.debug("XWikiPreferences missing access rights fixed for database [" + getWiki()
          + "].");
      return true;
    }
    return false;
  }

  private DocumentReference getGlobalRightsRef() {
    return globalRightsClass.getClassReference().getDocRef();
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

}
