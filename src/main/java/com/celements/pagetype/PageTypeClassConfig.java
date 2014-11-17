package com.celements.pagetype;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

@Component
public class PageTypeClassConfig implements IPageTypeClassConfig {

  @Override
  public DocumentReference getPageTypePropertiesClassRef(WikiReference wikiRef) {
    return new DocumentReference(PAGE_TYPE_PROPERTIES_CLASS_DOC, new SpaceReference(
        PAGE_TYPE_PROPERTIES_CLASS_SPACE, wikiRef));
  }

  @Override
  public DocumentReference getPageTypeClassRef(WikiReference wikiRef) {
    return new DocumentReference(PAGE_TYPE_CLASS_DOC, new SpaceReference(
        PAGE_TYPE_CLASS_SPACE, wikiRef));
  }

}
