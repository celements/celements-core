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
    LOGGER.debug("starting celements mandatory XWikiRights for db '{}'", getWiki());
    if (!skipCelementsWikiRights()) {
      checkXWikiRights();
    }
    LOGGER.debug("end celements mandatory XWikiRights for db '{}'", getWiki());
  }

  boolean skipCelementsWikiRights() {
    boolean skip = getContext().getWiki().ParamAsLong(
        "celements.mandatory.skipWikiRights", 0) == 1L;
    LOGGER.trace("skipping XWikiRights for database '{}': {}", getWiki(), skip);
    return skip;
  }

  void checkXWikiRights() throws XWikiException {
    XWikiDocument wikiPrefDoc = getXWikiPrefDoc();
    if (wikiPrefDoc != null) {
      if (checkAccessRights(wikiPrefDoc)) {
        LOGGER.info("XWikiRights updated for db '{}'", getWiki());
        getContext().getWiki().saveDocument(wikiPrefDoc, "autocreate XWikiRights",
            getContext());
      } else {
        LOGGER.debug("XWikiRights uptodate for db '{}'", getWiki());
      }
    } else {
      LOGGER.trace("skip checkXWikiRights because wikiPrefDoc is null! ["
          + getWiki() + "]");
    }
  }

  private XWikiDocument getXWikiPrefDoc() throws XWikiException {
    XWikiDocument wikiPrefDoc;
    if (!getContext().getWiki().exists(getXWikiPreferencesRef(), getContext())) {
      LOGGER.debug("XWikiPreferencesDocument is missing that we create it. ["
          + getWiki() + "]");
      wikiPrefDoc = new CreateDocumentCommand().createDocument(getXWikiPreferencesRef(),
          "WikiPreference");
    } else {
      wikiPrefDoc = getContext().getWiki().getDocument(getXWikiPreferencesRef(), 
          getContext());
      LOGGER.trace("XWikiPreferencesDocument already exists. [" + getWiki() + "]");
    }
    return wikiPrefDoc;
  }

  boolean checkAccessRights(XWikiDocument wikiPrefDoc) throws XWikiException {
    BaseObject editRightsObj = wikiPrefDoc.getXObject(getGlobalRightsRef(),
        false, getContext());
    if (editRightsObj == null) {
      LOGGER.trace("checkAccessRights [" + getWiki() + "], global rights class exists: "
          + getContext().getWiki().exists(getGlobalRightsRef(), getContext()));
      LOGGER.trace("checkAccessRights [" + getWiki() + "], XWiki.ContentEditorsGroup"
          + " exists: " + getContext().getWiki().exists(new DocumentReference(getWiki(),
              "XWiki", "ContentEditorsGroup"), getContext()));
      editRightsObj = wikiPrefDoc.newXObject(getGlobalRightsRef(), getContext());
      editRightsObj.set("groups", "XWiki.ContentEditorsGroup", getContext());
      editRightsObj.set("levels", "edit,delete,undelete", getContext());
      editRightsObj.set("users", "", getContext());
      editRightsObj.set("allow", 1, getContext());
      BaseObject adminRightsObj = wikiPrefDoc.newXObject(getGlobalRightsRef(), 
          getContext());
      LOGGER.trace("checkAccessRights [" + getWiki() + "], XWiki.ContentEditorsGroup"
          + " exists: " + getContext().getWiki().exists(new DocumentReference(getWiki(),
              "XWiki", "XWikiAdminGroup"), getContext()));
      adminRightsObj.set("groups", "XWiki.XWikiAdminGroup", getContext());
      adminRightsObj.set("levels", "admin,edit,comment,delete,undelete,register",
          getContext());
      adminRightsObj.set("users", "", getContext());
      adminRightsObj.set("allow", 1, getContext());
      LOGGER.debug("XWikiPreferences missing access rights fixed for database ["
          + getWiki() + "].");
      return true;
    }
    return false;
  }

  private DocumentReference getXWikiPreferencesRef() {
    return new DocumentReference(getWiki(), "XWiki", "XWikiPreferences");
  }

  private DocumentReference getGlobalRightsRef() {
    return new DocumentReference(getWiki(), "XWiki", "XWikiGlobalRights");
  }

  private String getWiki() {
    return getContext().getDatabase();
  }

}
