package com.celements.model.classes.fields.list;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.AbstractClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.google.common.base.Splitter;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

public abstract class ListField<T> extends AbstractClassField<List<T>> implements
    CustomClassField<List<T>> {

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
  public Class<List<T>> getType() {
    return (Class<List<T>>) (Object) List.class;
  }

  @Override
  public List<T> resolve(Object obj) {
    List<?> list;
    if (obj instanceof String) {
      list = Splitter.on(getSeparator()).splitToList((String) obj);
    } else if (obj != null) {
      list = getType().cast(obj);
    } else {
      list = new ArrayList<>();
    }
    return resolveList(list);
  }

  @NotNull
  protected abstract List<T> resolveList(@NotNull List<?> list);

  public Boolean getMultiSelect() {
    return multiSelect;
  }

  public ListField<T> setMultiSelect(Boolean multiSelect) {
    this.multiSelect = multiSelect;
    return this;
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

  public String getSeparator() {
    if (separator != null) {
      return separator;
    } else {
      return "|";
    }
  }

  public ListField<T> setSeparator(String separator) {
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
