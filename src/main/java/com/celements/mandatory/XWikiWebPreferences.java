package com.celements.mandatory;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.CelConstant;
import com.celements.web.classes.oldcore.XWikiGlobalRightsClass;
import com.xpn.xwiki.XWikiConstant;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class XWikiWebPreferences extends AbstractMandatoryDocument {

  private static final Logger LOGGER = LoggerFactory.getLogger(XWikiWebPreferences.class);
  protected static final List<EAccessLevel> ACCESS_RIGHTS = List.of(EAccessLevel.VIEW,
      EAccessLevel.EDIT, EAccessLevel.COMMENT, EAccessLevel.DELETE, EAccessLevel.UNDELETE,
      EAccessLevel.REGISTER);

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return List.of("celements.MandatoryGroups");
  }

  @Override
  protected boolean checkDocuments(XWikiDocument doc) throws XWikiException {
    boolean isDirty = false;
    isDirty = checkSpaceLayout(doc);
    isDirty |= checkGlobalRightsObject(doc);
    return isDirty;
  }

  boolean checkSpaceLayout(XWikiDocument webPrefDoc) {
    if (!XWikiObjectFetcher.on(webPrefDoc).filter(PageTypeClass.CLASS_REF).filter(
        PageTypeClass.PAGE_LAYOUT, "SimpleLayout").exists()) {
      XWikiObjectEditor editor = XWikiObjectEditor.on(webPrefDoc).filter(PageTypeClass.CLASS_REF);
      editor.createFirstIfNotExists();
      editor.editField(PageTypeClass.PAGE_LAYOUT).first(CelConstant.SIMPLE_LAYOUT);
      LOGGER.debug("XWiki space: set missing SimpleLayout for database [{}].", getWiki());
      return true;
    }
    return false;
  }

  boolean checkGlobalRightsObject(XWikiDocument webPrefDoc) {
    if (!XWikiObjectFetcher.on(webPrefDoc).filter(XWikiGlobalRightsClass.CLASS_REF)
        .filter(XWikiGlobalRightsClass.FIELD_GROUPS, List.of("XWikiAdminGroup")).exists()) {
      XWikiObjectEditor admGrpObjEditor = XWikiObjectEditor.on(webPrefDoc)
          .filter(XWikiGlobalRightsClass.CLASS_REF);
      admGrpObjEditor.filter(XWikiGlobalRightsClass.FIELD_GROUPS, List.of("XWikiAdminGroup"));
      admGrpObjEditor.filter(XWikiGlobalRightsClass.FIELD_LEVELS, ACCESS_RIGHTS);
      admGrpObjEditor.filter(XWikiGlobalRightsClass.FIELD_ALLOW, true);
      admGrpObjEditor.createFirstIfNotExists();
      LOGGER.debug("XWiki space: set missing GlobalRights for XWikiAdminGroup for database [{}].",
          getWiki());
      return true;
    }
    return false;
  }

  @Override
  protected boolean checkDocumentsMain(XWikiDocument doc) throws XWikiException {
    return checkSpaceLayout(doc);
  }

  @Override
  protected DocumentReference getDocRef() {
    return new RefBuilder().with(modelContext.getWikiRef()).space(XWikiConstant.XWIKI_SPACE)
        .doc(XWikiConstant.WEB_PREF_DOC_NAME).build(DocumentReference.class);
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public String getName() {
    return "XWikiWebPrefs";
  }

  @Override
  protected boolean skip() {
    return false;
  }

}
