package com.celements.model.classes.fields.ref;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.SpaceReference;

@Immutable
public final class SpaceReferenceField extends ReferenceField<SpaceReference> {

  public static class Builder extends ReferenceField.Builder<Builder, SpaceReference> {

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name);
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
