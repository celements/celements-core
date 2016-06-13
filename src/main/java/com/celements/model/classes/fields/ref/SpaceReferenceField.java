package com.celements.model.classes.fields.ref;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

public class SpaceReferenceField extends EntityReferenceField<SpaceReference> {

  public SpaceReferenceField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @Override
  public Class<SpaceReference> getType() {
    return SpaceReference.class;
  }

}
