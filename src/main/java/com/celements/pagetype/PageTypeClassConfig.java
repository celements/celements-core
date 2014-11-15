package com.celements.pagetype;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

@Component
public class PageTypeClassConfig implements IPageTypeClassConfig {

  @Override
  public DocumentReference getPageTypePropertiesClassRef(String wikiName) {
    return new DocumentReference(wikiName, PAGE_TYPE_PROPERTIES_CLASS_SPACE,
        PAGE_TYPE_PROPERTIES_CLASS_DOC);
  }

  @Override
  public DocumentReference getPageTypeClassRef(String wikiName) {
    return new DocumentReference(wikiName, PAGE_TYPE_CLASS_SPACE, PAGE_TYPE_CLASS_DOC);
  }

}
