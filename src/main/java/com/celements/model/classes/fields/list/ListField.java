package com.celements.model.classes.fields.list;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.python.google.common.base.Objects;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.AbstractClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

public abstract class ListField<T> extends AbstractClassField<List<T>> implements
    CustomClassField<List<T>> {

  private final Boolean multiSelect;
  private final Integer size;
  private final String displayType;
  private final Boolean picker;
  private final String separator;

  public abstract static class Builder<B extends Builder<B, T>, T> extends
      AbstractClassField.Builder<B, List<T>> {

    private Boolean multiSelect;
    private Integer size;
    private String displayType;
    private Boolean picker;
    private String separator;

    public Builder(@NotNull DocumentReference classRef, @NotNull String name) {
      super(classRef, name);
    }

    public B multiSelect(@Nullable Boolean val) {
      multiSelect = val;
      return getThis();
    }

    public B size(@Nullable Integer val) {
      size = val;
      return getThis();
    }

    public B displayType(@Nullable String val) {
      displayType = val;
      return getThis();
    }

    public B picker(@Nullable Boolean val) {
      picker = val;
      return getThis();
    }

    public B separator(@Nullable String val) {
      separator = val;
      return getThis();
    }

  }

  protected ListField(@NotNull Builder<?, T> builder) {
    super(builder);
    this.multiSelect = builder.multiSelect;
    this.size = builder.size;
    this.displayType = builder.displayType;
    this.picker = builder.picker;
    this.separator = Objects.firstNonNull(Strings.emptyToNull(builder.separator), "|");
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

  public Integer getSize() {
    return size;
  }

  public String getDisplayType() {
    return displayType;
  }

  public Boolean getPicker() {
    return picker;
  }

  public String getSeparator() {
    return separator;
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
