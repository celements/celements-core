package com.celements.model.classes.fields.ref;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

public class DocumentReferenceField extends EntityReferenceField<DocumentReference> {

  public DocumentReferenceField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @Override
  public Class<DocumentReference> getType() {
    return DocumentReference.class;
  }

}
