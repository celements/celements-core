package com.celements.model.access.object.restriction;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.model.access.object.ObjectBridge;
import com.celements.model.classes.fields.ClassField;

@Immutable
public class FieldAbsentRestriction<O, T> extends ClassRestriction<O> {

  private final ClassField<T> field;

  public FieldAbsentRestriction(@NotNull ObjectBridge<?, O> bridge, @NotNull ClassField<T> field) {
    super(bridge, field.getClassDef().getClassReference());
    this.field = checkNotNull(field);
  }

  public ClassField<T> getField() {
    return field;
  }

  @Override
  public boolean apply(@NotNull O obj) {
    return super.apply(obj) && !getBridge().getObjectField(obj, field).isPresent();
  }

}
