package com.celements.pagetype.classcollection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWikiException;

/**
 * @deprecated instead use PageTypeClasses
 */
@Component("com.celements.pagetype.classcollection")
@Deprecated
public class PageTypeClassCollection extends AbstractClassCollection {

  public static final String PAGE_TYPE_CLASS_DOC = "PageType";
  public static final String PAGE_TYPE_CLASS_SPACE = "Celements2";
  public static final String PAGE_TYPE_CLASSNAME = PAGE_TYPE_CLASS_SPACE + "."
       + PAGE_TYPE_CLASS_DOC;
  public static final String PAGE_TYPE_FIELD = "page_type";

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      PageTypeClassCollection.class);

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

  @Override
  protected void initClasses() throws XWikiException {
    // TODO Auto-generated method stub
    
  }

  public String getConfigName() {
    return "celPageType";
  }

  public DocumentReference getPageTypeClassRef(String wikiName) {
    return new DocumentReference(wikiName, PAGE_TYPE_CLASS_SPACE, PAGE_TYPE_CLASS_DOC);
  }

}
