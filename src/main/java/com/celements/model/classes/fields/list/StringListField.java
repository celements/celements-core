package com.celements.model.classes.fields.list;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.google.common.base.Joiner;

public abstract class StringListField extends ListField<String> {

  public StringListField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @Override
  public Object serialize(List<String> value) {
    Object ret = null;
    if (value != null) {
      ret = Joiner.on(getSeparator()).join(value);
    }
    return ret;
  }

  @Override
  protected List<String> resolveList(List<?> list) {
    return getType().cast(list);
  }

}
