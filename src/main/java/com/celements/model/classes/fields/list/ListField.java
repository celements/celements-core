package com.celements.model.classes.fields.list;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.python.google.common.base.Joiner;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.AbstractClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.google.common.base.Splitter;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

public abstract class ListField extends AbstractClassField<List<String>> implements
    CustomClassField<List<String>> {

  private Boolean multiSelect;
  private Integer size;
  private String displayType;
  private Boolean picker;
  private String separator;

  public ListField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<List<String>> getType() {
    return (Class<List<String>>) (Object) List.class;
  }

  @Override
  @Nullable
  public Object serialize(@Nullable List<String> value) {
    return Joiner.on(getSeparator()).join(value);
  }

  @Override
  @Nullable
  public List<String> resolve(@Nullable Object obj) {
    if (obj instanceof String) {
      return Splitter.on(getSeparator()).splitToList((String) obj);
    } else {
      return getType().cast(obj);
    }
  }

  public Boolean getMultiSelect() {
    return multiSelect;
  }

  public ListField setMultiSelect(Boolean multiSelect) {
    this.multiSelect = multiSelect;
    return this;
  }

  public Integer getSize() {
    return size;
  }

  public ListField setSize(Integer size) {
    this.size = size;
    return this;
  }

  public String getDisplayType() {
    return displayType;
  }

  public ListField setDisplayType(String displayType) {
    this.displayType = displayType;
    return this;
  }

  public Boolean getPicker() {
    return picker;
  }

  public ListField setPicker(Boolean picker) {
    this.picker = picker;
    return this;
  }

  public String getSeparator() {
    if (separator != null) {
      return separator;
    } else {
      return "|";
    }
  }

  public ListField setSeparator(String separator) {
    this.separator = separator;
    return this;
  }

  @Override
  protected PropertyClass getPropertyClass() {
    ListClass element = getListClass();
    if (multiSelect != null) {
      element.setMultiSelect(multiSelect);
    }
    if (size != null) {
      element.setSize(size);
    }
    if (displayType != null) {
      element.setDisplayType(displayType);
    }
    if (picker != null) {
      element.setPicker(picker);
    }
    element.setSeparators(getSeparator());
    return element;
  }

  @NotNull
  protected abstract ListClass getListClass();

}
