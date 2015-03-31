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
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("celements.mandatory.wikirights")
public class XWikiXWikiRights implements IMandatoryDocumentRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(XWikiXWikiRights.class);

  @Requirement
  private Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public List<String> dependsOnMandatoryDocuments() {
    return Arrays.asList("celements.MandatoryGroups");
  }


  public void checkDocuments() throws XWikiException {
    LOGGER.debug("starting mandatory checkXWikiRights for database [" 
        + getContext().getDatabase() + "], skipCelementsParam [" 
        + isSkipCelementsWikiRights() + "].");
    if (!isSkipCelementsWikiRights()) {
      checkXWikiRights();
    } else {
      LOGGER.info("skip mandatory checkXWikiRights for database ["
          + getContext().getDatabase() + "], skipCelementsParam [" 
          + isSkipCelementsWikiRights() + "].");
    }
    LOGGER.trace("end checkDocuments in XWikiXWikiRights for database [" 
        + getContext().getDatabase() + "].");
  }

  boolean isSkipCelementsWikiRights() {
    boolean isSkip = getContext().getWiki().ParamAsLong(
        "celements.mandatory.skipWikiRights", 0) == 1L;
    LOGGER.trace("skipCelementsWikiRights for database [" + getContext().getDatabase() 
        + "] returning [" + isSkip + "].");
    return isSkip;
  }

  void checkXWikiRights() throws XWikiException {
    DocumentReference xWikiPreferencesRef = getXWikiPreferencesRef(
        getContext().getDatabase());
    XWikiDocument wikiPrefDoc = getXWikiPreferencesDocument(xWikiPreferencesRef);
    if (wikiPrefDoc != null) {
      boolean dirty = checkAccessRights(wikiPrefDoc);
      if (dirty) {
        LOGGER.info("XWikiPreferencesDocument updated for [" + getContext().getDatabase()
            + "].");
        getContext().getWiki().saveDocument(wikiPrefDoc, "autocreate XWikiRights", 
            getContext());
      } else {
        LOGGER.debug("XWikiPreferencesDocument not saved. Everything uptodate. ["
            + getContext().getDatabase() + "].");
      }
    } else {
      LOGGER.trace("skip checkXWikiRights because wikiPrefDoc is null! ["
          + getContext().getDatabase() + "]");
    }
  }

  private XWikiDocument getXWikiPreferencesDocument(DocumentReference xWikiPreferencesRef
      ) throws XWikiException {
    XWikiDocument wikiPrefDoc;
    if (!getContext().getWiki().exists(xWikiPreferencesRef, getContext())) {
      LOGGER.debug("XWikiPreferencesDocument is missing that we create it. ["
          + getContext().getDatabase() + "]");
      wikiPrefDoc = new CreateDocumentCommand().createDocument(xWikiPreferencesRef,
          "WikiPreference");
    } else {
      wikiPrefDoc = getContext().getWiki().getDocument(xWikiPreferencesRef, getContext());
      LOGGER.trace("XWikiPreferencesDocument already exists. ["
          + getContext().getDatabase() + "]");
    }
    return wikiPrefDoc;
  }

  boolean checkAccessRights(XWikiDocument wikiPrefDoc)
      throws XWikiException {
    String wikiName = getContext().getDatabase();
    BaseObject editRightsObj = wikiPrefDoc.getXObject(getGlobalRightsRef(wikiName),
        false, getContext());
    if (editRightsObj == null) {
      LOGGER.trace("checkAccessRights [" + wikiName + "], global rights class exists: "
          + getContext().getWiki().exists(getGlobalRightsRef(wikiName), getContext()));
      LOGGER.trace("checkAccessRights [" + wikiName + "], XWiki.ContentEditorsGroup"
          + " exists: " + getContext().getWiki().exists(new DocumentReference(wikiName,
              "XWiki", "ContentEditorsGroup"), getContext()));
      editRightsObj = wikiPrefDoc.newXObject(getGlobalRightsRef(wikiName), getContext());
      editRightsObj.set("groups", "XWiki.ContentEditorsGroup", getContext());
      editRightsObj.set("levels", "edit,delete,undelete", getContext());
      editRightsObj.set("users", "", getContext());
      editRightsObj.set("allow", 1, getContext());
      BaseObject adminRightsObj = wikiPrefDoc.newXObject(getGlobalRightsRef(
          wikiName), getContext());
      LOGGER.trace("checkAccessRights [" + wikiName + "], XWiki.ContentEditorsGroup"
          + " exists: " + getContext().getWiki().exists(new DocumentReference(wikiName,
              "XWiki", "XWikiAdminGroup"), getContext()));
      adminRightsObj.set("groups", "XWiki.XWikiAdminGroup", getContext());
      adminRightsObj.set("levels", "admin,edit,comment,delete,undelete,register",
          getContext());
      adminRightsObj.set("users", "", getContext());
      adminRightsObj.set("allow", 1, getContext());
      LOGGER.debug("XWikiPreferences missing access rights fixed for database ["
          + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  private DocumentReference getXWikiPreferencesRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiPreferences");
  }

  private DocumentReference getGlobalRightsRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiGlobalRights");
  }

}
