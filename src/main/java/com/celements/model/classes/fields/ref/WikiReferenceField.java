package com.celements.model.classes.fields.ref;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

@Immutable
public class WikiReferenceField extends EntityReferenceField<WikiReference> {

  public static class Builder extends EntityReferenceField.Builder<Builder, WikiReference> {

    public Builder(@NotNull DocumentReference classRef, @NotNull String name) {
      super(classRef, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public WikiReferenceField build() {
      return new WikiReferenceField(getThis());
    }

  }

  protected WikiReferenceField(@NotNull Builder builder) {
    super(builder);
  }

  @Override
  public Class<WikiReference> getType() {
    return WikiReference.class;
  }

}
