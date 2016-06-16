package com.celements.model.classes.fields.list;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.AbstractClassField;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

public abstract class ListField<T> extends AbstractClassField<T> {

  private final boolean multiSelect;

  private Integer size;
  private String displayType;
  private Boolean picker;

  public ListField(@NotNull DocumentReference classRef, @NotNull String name, boolean multiSelect) {
    super(classRef, name);
    this.multiSelect = multiSelect;
  }

  public boolean getMultiSelect() {
    return multiSelect;
  }

  public Integer getSize() {
    return size;
  }

  public ListField<T> setSize(Integer size) {
    this.size = size;
    return this;
  }

  public String getDisplayType() {
    return displayType;
  }

  public ListField<T> setDisplayType(String displayType) {
    this.displayType = displayType;
    return this;
  }

  public Boolean getPicker() {
    return picker;
  }

  public ListField<T> setPicker(Boolean picker) {
    this.picker = picker;
    return this;
  }

  @Override
  protected PropertyClass getPropertyClass() {
    ListClass element = getListClass();
    element.setMultiSelect(multiSelect);
    if (size != null) {
      element.setSize(size);
    }
    if (displayType != null) {
      element.setDisplayType(displayType);
    }
    if (picker != null) {
      element.setPicker(picker);
    }
    return element;
  }

  @NotNull
  protected abstract ListClass getListClass();

}
