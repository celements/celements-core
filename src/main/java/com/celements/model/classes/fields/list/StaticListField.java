package com.celements.model.classes.fields.list;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

@Immutable
public final class StaticListField extends StringListField {

  protected StaticListField(@NotNull Builder builder) {
    super(builder);
  }

}
