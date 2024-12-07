package com.celements.mandatory;

import java.util.List;

import org.springframework.stereotype.Component;

import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public abstract class AbstractXWikiClassRights extends AbstractMandatoryDocument {

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return List.of("celements.mandatory.MandatoryXClasses");
  }

  boolean checkRightsObject(XWikiDocument classDoc) {
    if (!XWikiObjectFetcher.on(classDoc).filter(XWikiRightsClass.CLASS_REF)
        .filter(XWikiRightsClass.FIELD_GROUPS, List.of("XWikiAllGroup")).exists()) {
      XWikiObjectEditor allGrpObjEditor = XWikiObjectEditor.on(classDoc)
          .filter(XWikiRightsClass.CLASS_REF);
      allGrpObjEditor.filter(XWikiRightsClass.FIELD_GROUPS, List.of("XWikiAllGroup"));
      allGrpObjEditor.filter(XWikiRightsClass.FIELD_LEVELS, List.of(EAccessLevel.VIEW));
      allGrpObjEditor.filter(XWikiRightsClass.FIELD_ALLOW, true);
      allGrpObjEditor.createFirstIfNotExists();
      getLogger().debug("XWiki space: set missing Rights for XWikiAllGroup for database [{}].",
          getWiki());
      return true;
    }
    return false;
  }

  @Override
  protected boolean skip() {
    return false;
  }

  @Override
  protected boolean checkDocuments(XWikiDocument doc) throws XWikiException {
    boolean isDirty = false;
    isDirty = checkRightsObject(doc);
    return isDirty;
  }

  @Override
  protected boolean checkDocumentsMain(XWikiDocument doc) throws XWikiException {
    return false;
  }

}
