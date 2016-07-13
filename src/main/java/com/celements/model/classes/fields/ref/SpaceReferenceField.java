package com.celements.model.classes.fields.ref;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

@Immutable
public final class SpaceReferenceField extends EntityReferenceField<SpaceReference> {

  public static class Builder extends EntityReferenceField.Builder<Builder, SpaceReference> {

    public Builder(@NotNull DocumentReference classRef, @NotNull String name) {
      super(classRef, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public SpaceReferenceField build() {
      return new SpaceReferenceField(getThis());
    }

  }

  protected SpaceReferenceField(@NotNull Builder builder) {
    super(builder);
  }

  @Override
  public Class<SpaceReference> getType() {
    return SpaceReference.class;
  }

}
