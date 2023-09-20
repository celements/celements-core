package com.celements.mandatory;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.pagetype.classes.PageTypeClass;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class XWikiWebPreferences extends AbstractMandatoryDocument {

  private static final Logger LOGGER = LoggerFactory.getLogger(XWikiWebPreferences.class);

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean checkDocuments(XWikiDocument doc) throws XWikiException {
    // muss true zurückliefern, wenn es etwas zu speichern gibt
    boolean isDirty = checkSpaceLayout(doc);
    return isDirty;
  }

  boolean checkSpaceLayout(XWikiDocument webPrefDoc) {
    if (!XWikiObjectFetcher.on(webPrefDoc).filter(PageTypeClass.CLASS_REF).filter(
        PageTypeClass.PAGE_LAYOUT, "SimpleLayout").exists()) {
      XWikiObjectEditor editor = XWikiObjectEditor.on(webPrefDoc).filter(PageTypeClass.CLASS_REF);
      editor.createFirstIfNotExists();
      editor.editField(PageTypeClass.PAGE_LAYOUT).first("SimpleLayout");
      LOGGER.debug("Content space: set missing SimpleLayout for database [{}].", getWiki());
      return true;
    }
    return false;
  }

  @Override
  protected boolean checkDocumentsMain(XWikiDocument doc) throws XWikiException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected DocumentReference getDocRef() {
    return new RefBuilder().with(modelContext.getWikiRef()).space("XWiki")
        .doc(ModelContext.WEB_PREF_DOC_NAME).build(DocumentReference.class);
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
    // TODO Auto-generated method stub
    return false;
  }

}
