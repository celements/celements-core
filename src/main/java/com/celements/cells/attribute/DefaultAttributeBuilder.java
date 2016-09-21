package com.celements.cells.attribute;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

@NotThreadSafe
public class DefaultAttributeBuilder implements AttributeBuilder {

  private static final Splitter CSS_CLASS_SPLITTER = Splitter.on(
      " ").trimResults().omitEmptyStrings();

  private final Map<String, DefaultCellAttribute.Builder> attributeMap = new LinkedHashMap<>();

  @Override
  public AttributeBuilder addEmptyAttribute(String attrName) {
    if (!attrName.isEmpty()) {
      getAttributeBuilder(attrName);
    }
    return this;
  }

  @Override
  public AttributeBuilder addNonEmptyAttribute(String attrName, String attrValue) {
    checkNotNull(attrName);
    if (!attrName.isEmpty() && !Strings.isNullOrEmpty(attrValue)) {
      getAttributeBuilder(attrName).addValue(attrValue);
    }
    return this;
  }

  @Override
  public AttributeBuilder addNonEmptyAttribute(String attrName, Iterable<String> attrValues) {
    checkNotNull(attrName);
    checkNotNull(attrValues);
    for (String attrValue : attrValues) {
      addNonEmptyAttribute(attrName, attrValue);
    }
    return this;
  }

  @Override
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

  @Override
  public AttributeBuilder addId(String idname) {
    return addNonEmptyAttribute("id", idname);
  }

  @Override
  public AttributeBuilder addCssClasses(String cssClasses) {
    if (cssClasses != null) {
      addNonEmptyAttribute("class", CSS_CLASS_SPLITTER.split(cssClasses));
    }
    return this;
  }

  @Override
  public AttributeBuilder addStyles(String cssStyles) {
    return addNonEmptyAttribute("style", cssStyles);
  }

}
