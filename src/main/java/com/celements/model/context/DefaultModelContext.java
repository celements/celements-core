package com.celements.model.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.util.IModelUtils;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component
public class DefaultModelContext implements IModelContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModelContext.class);

  @Requirement
  ConfigurationSource cfgSrc;

  @Requirement
  private Execution execution;

  @Override
  public XWikiContext getXWikiContext() {
    return (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public WikiReference getWiki() {
    return new WikiReference(getXWikiContext().getDatabase());
  }

  @Override
  public WikiReference setWiki(WikiReference wikiRef) {
    WikiReference oldWiki = getWiki();
    getXWikiContext().setDatabase(wikiRef.getName());
    return oldWiki;
  }

  @Override
  public DocumentReference getDoc() {
    XWikiDocument doc = getXWikiContext().getDoc();
    return doc != null ? doc.getDocumentReference() : null;
  }

  @Override
  public DocumentReference setDoc(DocumentReference docRef) {
    DocumentReference oldDocRef = getDoc();
    XWikiDocument doc = null;
    if (docRef != null) {
      doc = getModelAccess().getOrCreateDocument(docRef);
    }
    getXWikiContext().setDoc(doc);
    return oldDocRef;
  }

  @Override
  public String getDefaultLanguage() {
    return getDefaultLanguage(getWiki());
  }

  @Override
  public String getDefaultLanguage(EntityReference ref) {
    WikiReference wikiBefore = getWiki();
    DocumentReference docBefore = getDoc();
    try {
      setWiki(getModelUtils().extractRef(ref, getWiki(), WikiReference.class));
      String defaultLang = getDefaultLangFromDoc(ref);
      if (Strings.isNullOrEmpty(defaultLang)) {
        defaultLang = getDefaultLangFromCfgSrc(ref);
      }
      LOGGER.trace("getDefaultLanguage: for '{}' got lang" + " '{}'", ref, defaultLang);
      return defaultLang;
    } finally {
      setWiki(wikiBefore);
      setDoc(docBefore);
    }
  }

  private String getDefaultLangFromDoc(EntityReference ref) {
    String ret = "";
    DocumentReference docRef = getModelUtils().extractRef(ref, DocumentReference.class);
    if (docRef != null) {
      try {
        ret = getModelAccess().getDocument(docRef).getDefaultLanguage();
      } catch (DocumentNotExistsException exc) {
        LOGGER.info("trying to get language for inexistent document '{}'", docRef);
      }
    }
    return ret;
  }

  private String getDefaultLangFromCfgSrc(EntityReference ref) {
    SpaceReference spaceRef = getModelUtils().extractRef(ref, SpaceReference.class);
    if (spaceRef != null) {
      setDoc(new DocumentReference(WEB_PREF_DOC_NAME, spaceRef));
    }
    return cfgSrc.getProperty(CFG_KEY_DEFAULT_LANG, FALLBACK_DEFAULT_LANG);
  }

  private IModelUtils getModelUtils() {
    return Utils.getComponent(IModelUtils.class);
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

}
