package com.celements.cells.attribute;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

@NotThreadSafe
public class AttributeBuilder {

  private static final Splitter CSS_CLASS_SPLITTER = Splitter.on(
      " ").trimResults().omitEmptyStrings();

  private final Map<String, DefaultCellAttribute.Builder> attributeMap = new LinkedHashMap<>();

  public AttributeBuilder addNonEmptyAttribute(@NotNull String attrName,
      @Nullable String attrValue) {
    checkNotNull(attrName);
    if (!attrName.isEmpty() && !Strings.isNullOrEmpty(attrValue)) {
      getAttributeBuilder(attrName).addValue(attrValue);
    }
    return this;
  }

  public AttributeBuilder addNonEmptyAttribute(@NotNull String attrName,
      @NotNull Iterable<String> attrValues) {
    checkNotNull(attrName);
    checkNotNull(attrValues);
    for (String attrValue : attrValues) {
      addNonEmptyAttribute(attrName, attrValue);
    }
    return this;
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

  public AttributeBuilder addId(@Nullable String idname) {
    return addNonEmptyAttribute("id", idname);
  }

  public AttributeBuilder addCssClasses(@Nullable String cssClasses) {
    if (cssClasses != null) {
      addNonEmptyAttribute("class", CSS_CLASS_SPLITTER.split(cssClasses));
    }
    return this;
  }

  public AttributeBuilder addStyles(@Nullable String cssStyles) {
    return addNonEmptyAttribute("style", cssStyles);
  }

}
