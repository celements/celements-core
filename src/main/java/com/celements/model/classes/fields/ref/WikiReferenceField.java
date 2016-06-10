package com.celements.model.classes.fields.ref;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

public class WikiReferenceField extends EntityReferenceField<WikiReference> {

  public WikiReferenceField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @NotNull
  @Override
  public Class<WikiReference> getEntityClass() {
    return WikiReference.class;
  }

}
