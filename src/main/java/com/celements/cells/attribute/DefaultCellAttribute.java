package com.celements.cells.attribute;

import static com.google.common.base.Preconditions.*;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

@Immutable
public class DefaultCellAttribute implements CellAttribute {

  public static class Builder {

    private static final Joiner VALUES_JOINER = Joiner.on(" ");

    private final Set<String> values = new LinkedHashSet<>();
    private String name;

    public Builder attrName(@NotNull String attrName) {
      this.name = checkNotNull(attrName);
      return this;
    }

    public Builder addValue(@NotNull String attrValue) {
      values.add(checkNotNull(attrValue).replaceAll("[\n\r]", ""));
      return this;
    }

    public DefaultCellAttribute build() {
      checkState(!Strings.isNullOrEmpty(name));
      String attrValue = null;
      if (!values.isEmpty()) {
        attrValue = VALUES_JOINER.join(values).trim();
      }
      return new DefaultCellAttribute(name, attrValue);
    }

  }

  private final String name;
  private final String value;

  private DefaultCellAttribute(@NotNull String attrName, @Nullable String attrValue) {
    this.name = attrName;
    this.value = attrValue;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Optional<String> getValue() {
    return Optional.ofNullable(value);
  }

}
