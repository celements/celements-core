package com.celements.cells.attribute;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

@NotThreadSafe
public class DefaultAttributeBuilder implements AttributeBuilder {

  private static final Splitter CSS_CLASS_SPLITTER = Splitter.on(
      " ").trimResults().omitEmptyStrings();

  private final Map<String, DefaultCellAttribute.Builder> attributeMap = new LinkedHashMap<>();

  @Override
  public AttributeBuilder addAttribute(String attrName, String attrValue) {
    checkNotNull(attrName);
    if (!attrName.isEmpty()) {
      getCellAttributeBuilder(attrName).addValue(Strings.nullToEmpty(attrValue));
    }
    return this;
  }

  @Override
  public AttributeBuilder addEmptyAttribute(String attrName) {
    checkNotNull(attrName);
    if (!attrName.isEmpty()) {
      getCellAttributeBuilder(attrName);
    }
    return this;
  }

  @Override
  public AttributeBuilder addNonEmptyAttribute(String attrName, String attrValue) {
    if (!Strings.isNullOrEmpty(attrValue)) {
      addAttribute(attrName, attrValue);
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
  public AttributeBuilder addUniqAttribute(String attrName, String attrValue) {
    attributeMap.remove(attrName);
    return addNonEmptyAttribute(attrName, attrValue);
  }

  @Override
  public List<CellAttribute> build() {
    List<CellAttribute> attributeList = new ArrayList<>();
    for (DefaultCellAttribute.Builder builder : attributeMap.values()) {
      attributeList.add(builder.build());
    }
    return Collections.unmodifiableList(attributeList);
  }

  @Override
  public Optional<String> getAttribute(String attrName) {
    return getCellAttributeBuilder(attrName).build().getValue().toJavaUtil();
  }

  DefaultCellAttribute.Builder getCellAttributeBuilder(String attrName) {
    return attributeMap.computeIfAbsent(attrName,
        key -> new DefaultCellAttribute.Builder().attrName(key));
  }

  @Override
  public AttributeBuilder addId(String idname) {
    return addUniqAttribute("id", idname);
  }

  @Override
  public AttributeBuilder addCssClasses(String cssClasses) {
    if (cssClasses != null) {
      addCssClasses(CSS_CLASS_SPLITTER.split(cssClasses));
    }
    return this;
  }

  @Override
  public AttributeBuilder addCssClasses(Iterable<String> cssClasses) {
    return addNonEmptyAttribute("class", cssClasses);
  }

  @Override
  public AttributeBuilder addStyles(String cssStyles) {
    return addNonEmptyAttribute("style", cssStyles);
  }

}
