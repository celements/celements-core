package com.celements.cells.attribute;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public interface AttributeBuilder {

  AttributeBuilder addEmptyAttribute(@NotNull String attrName);

  AttributeBuilder addNonEmptyAttribute(@NotNull String attrName, @Nullable String attrValue);

  AttributeBuilder addNonEmptyAttribute(@NotNull String attrName,
      @NotNull Iterable<String> attrValues);

  @NotNull
  List<CellAttribute> build();

  @NotNull
  AttributeBuilder addId(@Nullable String idname);

  @NotNull
  AttributeBuilder addCssClasses(@Nullable String cssClasses);

  @NotNull
  AttributeBuilder addStyles(@Nullable String cssStyles);

}
