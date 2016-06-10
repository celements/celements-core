package com.celements.model.classes.fields.ref;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

public class AttachmentReferenceField extends EntityReferenceField<AttachmentReference> {

  public AttachmentReferenceField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @NotNull
  @Override
  public Class<AttachmentReference> getEntityClass() {
    return AttachmentReference.class;
  }

}
