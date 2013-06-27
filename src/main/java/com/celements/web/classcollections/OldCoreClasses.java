package com.celements.web.classcollections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.oldCoreClasses")
public class OldCoreClasses extends AbstractClassCollection {

  private static Log LOGGER = LogFactory.getFactory().getInstance(OldCoreClasses.class);

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

  public String getConfigName() {
    return "oldCoreClasses";
  }

  @Override
  protected void initClasses() throws XWikiException {
    getRedirectClass();
  }

  private BaseClass getRedirectClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = new DocumentReference(getContext().getDatabase(),
        "Celements2", "Redirect");

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error(exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("redirect", "Redirect", 30);
    needsUpdate |= bclass.addTextField("querystr", "Query String", 30);
    needsUpdate |= addBooleanField(bclass, "show_included", "Query String", "yesno", 0);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

}
