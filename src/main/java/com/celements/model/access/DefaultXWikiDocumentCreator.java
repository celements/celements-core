package com.celements.model.access;

import java.util.Date;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.context.ModelContext;
import com.celements.model.util.References;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class DefaultXWikiDocumentCreator implements XWikiDocumentCreator {

  @Requirement
  private ModelContext context;

  @Override
  public XWikiDocument createWithoutDefaults(DocumentReference docRef) {
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setNew(true);
    doc.setLanguage("");
    Date creationDate = new Date();
    doc.setCreationDate(creationDate);
    doc.setContentUpdateDate(creationDate);
    doc.setDate(creationDate);
    doc.setCreator(context.getUserName());
    doc.setAuthor(context.getUserName());
    doc.setTranslation(0);
    doc.setContent("");
    doc.setContentDirty(true);
    doc.setMetaDataDirty(true);
    return doc;
  }

  @Override
  public XWikiDocument create(DocumentReference docRef) {
    XWikiDocument doc = createWithoutDefaults(docRef);
    doc.setDefaultLanguage(getDefaultLangForCreatingDoc(docRef));
    doc.setSyntax(doc.getSyntax()); // assures that syntax is set, 'new' has to be true
    return doc;
  }

  /**
   * when creating doc, get default language from space. except get it from wiki directly when
   * creating web preferences
   */
  private String getDefaultLangForCreatingDoc(DocumentReference docRef) {
    Class<? extends EntityReference> toExtractClass;
    if (docRef.getName().equals(ModelContext.WEB_PREF_DOC_NAME)) {
      toExtractClass = WikiReference.class;
    } else {
      toExtractClass = SpaceReference.class;
    }
    return context.getDefaultLanguage(References.extractRef(docRef, toExtractClass).get());
  }

}
