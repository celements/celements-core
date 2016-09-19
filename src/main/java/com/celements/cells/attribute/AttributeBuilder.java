package com.celements.cells.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import com.google.common.base.Strings;

@NotThreadSafe
public class AttributeBuilder {

  private final Map<String, DefaultCellAttribute.Builder> attributeMap = new LinkedHashMap<>();

  public void addNonEmptyAttribute(@NotNull String attrName, @Nullable String attrValue) {
    if (!Strings.isNullOrEmpty(attrValue)) {
      getAttributeBuilder(attrName).addValue(attrValue);
    }
  }

  /**
   * addNonEmptyAttribute
   *
   * @param attrName
   * @param attrValues
   */
  public void addNonEmptyAttribute(@NotNull String attrName, @NotNull Iterable<String> attrValues) {
    for (String attrValue : attrValues) {
      addNonEmptyAttribute(attrName, attrValue);
    }
  }

  @NotNull
  public List<CellAttribute> build() {
    List<CellAttribute> attributeList = new ArrayList<>();
    for (DefaultCellAttribute.Builder builder : attributeMap.values()) {
      attributeList.add(builder.build());
    }
    return Collections.unmodifiableList(attributeList);
  }

  DefaultCellAttribute.Builder getAttributeBuilder(String attrName) {
    if (!attributeMap.containsKey(attrName)) {
      attributeMap.put(attrName, new DefaultCellAttribute.Builder().attrName(attrName));
    }
    return attributeMap.get(attrName);
  }

}
