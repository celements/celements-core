package com.celements.model.classes.fields.ref;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

public class DocumentReferenceField extends EntityReferenceField<DocumentReference> {

  public DocumentReferenceField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @NotNull
  @Override
  public Class<DocumentReference> getEntityClass() {
    return DocumentReference.class;
  }

}
