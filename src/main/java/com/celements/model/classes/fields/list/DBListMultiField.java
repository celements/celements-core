package com.celements.model.classes.fields.list;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

public class DBListMultiField extends DBListField<List<String>> {

  public DBListMultiField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name, true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<List<String>> getType() {
    return (Class<List<String>>) (Object) List.class;
  }

}
