package com.celements.cells.attribute;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public interface AttributeBuilder {

  /**
   * addAttribute adds <code>attrName</code> with <code>attrValue</code>.
   * If <code>attrValue</code> is null or empty it adds an empty value.
   */
  @NotNull
  AttributeBuilder addAttribute(@NotNull String attrName, @Nullable String attrValue);

  /**
   * addEmptyAttribute adds <code>attrName</code> with an absent value.
   * For XHTML this should be rendered as e.g. <code>selected="selected"</code> and for HTML5 as
   * e.g. <code>selected</code>
   */
  @NotNull
  AttributeBuilder addEmptyAttribute(@NotNull String attrName);

  /**
   * addNonEmptyAttribute only adds <code>attrName</code> if <code>attrValue</code> is not null and
   * not empty. If <code>attrValue</code> is null or empty it just returns.
   */
  @NotNull
  AttributeBuilder addNonEmptyAttribute(@NotNull String attrName, @Nullable String attrValue);

  @NotNull
  AttributeBuilder addNonEmptyAttribute(@NotNull String attrName,
      @NotNull Iterable<String> attrValues);

  @NotNull
  List<CellAttribute> build();

  @NotNull
  AttributeBuilder addId(@Nullable String idname);

  @NotNull
  AttributeBuilder addCssClasses(@Nullable String cssClasses);

  @NotNull
  AttributeBuilder addCssClasses(@NotNull Iterable<String> cssClasses);

  @NotNull
  AttributeBuilder addStyles(@Nullable String cssStyles);

}
