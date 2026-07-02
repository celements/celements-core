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

import static com.google.common.base.Strings.*;

import java.util.List;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.filebase.IFileBaseAccessRole;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.reference.RefBuilder;
import com.celements.pagetype.classes.PageTypeClass;
import com.xpn.xwiki.XWikiConfigSource;
import com.xpn.xwiki.XWikiConstant;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("celements.mandatory.wikipreferences")
public class XWikiXWikiPreferences extends AbstractMandatoryDocument {

  private static final Logger LOGGER = LoggerFactory.getLogger(XWikiXWikiPreferences.class);

  private final XWikiConfigSource xwikiCfg;

  @Inject
  public XWikiXWikiPreferences(XWikiConfigSource xwikiCfg) {
    this.xwikiCfg = xwikiCfg;
  }

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return List.of();
  }

  @Override
  public String getName() {
    return "CelementsXWikiPreferences";
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
    boolean dirty = setUnsetPageType(doc);
    dirty |= checkWikiPreferences(doc);
    return dirty;
  }

  @Override
  protected boolean checkDocumentsMain(XWikiDocument doc) throws XWikiException {
    boolean dirty = setUnsetPageType(doc);
    dirty |= checkWikiPreferences(doc, (prefsObj) -> {
      boolean dirtyAdditional = false;
      dirtyAdditional |= setUnsetInt(prefsObj, "authenticate_view", 1);
      dirtyAdditional |= setUnsetInt(prefsObj, "renderXWikiRadeoxRenderer", 1);
      return dirtyAdditional;
    });
    return dirty;
  }

  private boolean checkWikiPreferences(XWikiDocument wikiPrefDoc,
      Predicate<BaseObject> additionalChecks) {
    var defaultLang = xwikiCfg.getProperty("celements.admin_language", "en");
    if (isNullOrEmpty(wikiPrefDoc.getDefaultLanguage())) {
      wikiPrefDoc.setDefaultLanguage(defaultLang);
    }
    BaseObject prefsObj = XWikiObjectEditor.on(wikiPrefDoc)
        .filter(new ClassReference(getDocRef()))
        .createFirstIfNotExists();
    boolean dirty = false;
    dirty |= additionalChecks.test(prefsObj);
    dirty |= setEmptyString(prefsObj, "editor", "Text");
    dirty |= setUnsetInt(prefsObj, "renderXWikiRadeoxRenderer", 0);
    dirty |= setEmptyString(prefsObj, "pageWidth", "default");
    dirty |= setUnsetInt(prefsObj, "multilingual", 1);
    dirty |= setEmptyString(prefsObj, "languages", defaultLang);
    dirty |= setEmptyString(prefsObj, "default_language", defaultLang);
    dirty |= setEmptyString(prefsObj, "admin_language", defaultLang);
    dirty |= setUnsetInt(prefsObj, "authenticate_edit", 1);
    dirty |= setUnsetInt(prefsObj, "authenticate_view", 0);
    dirty |= setUnsetLong(prefsObj, "upload_maxsize", 104857600L);
    return dirty;
  }

  private boolean checkWikiPreferences(XWikiDocument wikiPrefDoc) {
    return checkWikiPreferences(wikiPrefDoc, (prefsObj) -> {
      boolean dirty = false;
      String documentBundles = prefsObj.getStringValue("documentBundles");
      if (isNullOrEmpty(documentBundles) || !documentBundles.contains(
          "celements2web:Celements2.Dictionary")) {
        if (isNullOrEmpty(documentBundles)) {
          documentBundles = "celements2web:Celements2.Dictionary";
        } else {
          documentBundles = documentBundles + ",celements2web:Celements2.Dictionary";
        }
        prefsObj.setStringValue("documentBundles", documentBundles);
        LOGGER.debug("XWikiPreferences added missing Celements2.Dictionary for database [{}].",
            getWiki());
        dirty = true;
      }
      dirty |= setEmptyString(prefsObj, "cel_centralfilebase",
          IFileBaseAccessRole.FILE_BASE_DEFAULT_DOC_FN);
      return dirty;
    });
  }

  private boolean setUnsetPageType(XWikiDocument wikiPrefDoc) throws XWikiException {
    boolean dirty = false;
    DocumentReference pageTypeClassRef = PageTypeClass.CLASS_REF
        .getDocRef(modelContext.getWikiRef());
    BaseObject pageTypeObj = wikiPrefDoc.getXObject(pageTypeClassRef, false,
        modelContext.getXWikiContext());
    if (pageTypeObj == null) {
      pageTypeObj = wikiPrefDoc.newXObject(pageTypeClassRef, modelContext.getXWikiContext());
      pageTypeObj.setStringValue("page_type", "WikiPreference");
      LOGGER.debug("XWikiPreferences missing page type object fixed for database [{}].", getWiki());
      dirty = true;
    }
    return dirty;
  }

  private boolean setEmptyString(BaseObject prefsObj, String field, String value) {
    if (isNullOrEmpty(prefsObj.getStringValue(field))) {
      prefsObj.setStringValue(field, value);
      LOGGER.debug("[{}] missing XWikiPref [{}], setting default: {}", getWiki(), field, value);
      return true;
    }
    return false;
  }

  private boolean setUnsetInt(BaseObject prefsObj, String field, int value) {
    if (prefsObj.getIntValue(field, -1) < 0) {
      prefsObj.setIntValue(field, value);
      LOGGER.debug("[{}] missing XWikiPref [{}], setting default: {}", getWiki(), field, value);
      return true;
    }
    return false;
  }

  private boolean setUnsetLong(BaseObject prefsObj, String field, long value) {
    if (prefsObj.getLongValue(field) <= 0) {
      prefsObj.set(field, value, modelContext.getXWikiContext());
      LOGGER.debug("[{}] missing XWikiPref [{}], setting default: {}", getWiki(), field, value);
      return true;
    }
    return false;
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

}
