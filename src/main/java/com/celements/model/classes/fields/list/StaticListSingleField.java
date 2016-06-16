package com.celements.model.classes.fields.list;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

public class StaticListSingleField extends StaticListField<String> {

  public StaticListSingleField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name, false);
  }

  @Override
  public Class<String> getType() {
    return String.class;
  }

}
