package com.celements.model.classes.fields.list;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

public class DBListSingleField extends DBListField<String> {

  public DBListSingleField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name, false);
  }

  @Override
  public Class<String> getType() {
    return String.class;
  }

}
