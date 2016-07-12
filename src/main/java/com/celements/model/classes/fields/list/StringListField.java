package com.celements.model.classes.fields.list;

import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Joiner;

@Immutable
public abstract class StringListField extends ListField<String> {

  protected StringListField(@NotNull Builder<?, String> builder) {
    super(builder);
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
