package com.celements.web.classcollections;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

@Component
public class OldCoreClassConfig implements IOldCoreClassConfig {

  public DocumentReference getFromStorageClassRef(WikiReference wikiRef) {
    return new DocumentReference(FORM_STORAGE_CLASS_DOC, new SpaceReference(
        FORM_STORAGE_CLASS_SPACE, wikiRef));
  }

}
