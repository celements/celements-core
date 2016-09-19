package com.celements.cells.attribute;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

import groovy.lang.Immutable;

@Immutable
public class DefaultCellAttribute implements CellAttribute {

  public static class Builder {

    private static final Joiner VALUES_JOINER = Joiner.on(" ");

    private final Set<String> values = new LinkedHashSet<>();
    private String name;

    public Builder attrName(String attrName) {
      this.name = attrName;
      return this;
    }

    public Builder addValue(String attrValue) {
      values.add(attrValue);
      return this;
    }

    public DefaultCellAttribute build() {
      return new DefaultCellAttribute(name, VALUES_JOINER.join(values));
    }

  }

  private final String name;
  private final String value;

  public DefaultCellAttribute(@NotNull String attrName, @Nullable String attrValue) {
    this.name = attrName;
    this.value = Strings.emptyToNull(attrValue);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Optional<String> getValue() {
    return Optional.fromNullable(value);
  }

}
