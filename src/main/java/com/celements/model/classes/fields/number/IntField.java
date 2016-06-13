package com.celements.model.classes.fields.number;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

public class IntField extends NumberField<Integer> {

  public IntField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @Override
  public Class<Integer> getType() {
    return Integer.class;
  }

}
