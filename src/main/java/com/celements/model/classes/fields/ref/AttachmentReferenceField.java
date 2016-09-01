package com.celements.model.classes.fields.ref;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.AttachmentReference;


@Immutable
public final class AttachmentReferenceField extends EntityReferenceField<AttachmentReference> {

  public static class Builder extends EntityReferenceField.Builder<Builder, AttachmentReference> {

    public Builder(@NotNull String className, @NotNull String name) {
      super(className, name);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public AttachmentReferenceField build() {
      return new AttachmentReferenceField(getThis());
    }

  }

  protected AttachmentReferenceField(@NotNull Builder builder) {
    super(builder);
  }

  @Override
  public Class<AttachmentReference> getType() {
    return AttachmentReference.class;
  }

}
