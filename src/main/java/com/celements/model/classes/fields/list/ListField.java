package com.celements.model.classes.fields.list;

import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.marshalling.Marshaller;
import com.celements.model.classes.fields.AbstractClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

public abstract class ListField<T> extends AbstractClassField<List<T>> implements
    CustomClassField<List<T>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListField.class);

  protected static final String DEFAULT_SEPARATOR = "|";

  protected final Marshaller<T> marshaller;
  private final Boolean multiSelect;
  private final Integer size;
  private final String displayType;
  private final Boolean picker;
  private final String separator;

  public abstract static class Builder<B extends Builder<B, T>, T> extends
      AbstractClassField.Builder<B, List<T>> {

    private final Marshaller<T> marshaller;
    private Boolean multiSelect;
    private Integer size;
    private String displayType;
    private Boolean picker;
    private String separator;

    public Builder(@NotNull String classDefName, @NotNull String name,
        @NotNull Marshaller<T> marshaller) {
      super(classDefName, name);
      this.marshaller = checkNotNull(marshaller);
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
    this.marshaller = builder.marshaller;
    this.multiSelect = builder.multiSelect;
    this.size = firstNonNull(builder.size, 2);
    this.displayType = builder.displayType;
    this.picker = builder.picker;
    this.separator = firstNonNull(Strings.emptyToNull(builder.separator), DEFAULT_SEPARATOR);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<List<T>> getType() {
    return (Class<List<T>>) (Object) List.class;
  }

  @Override
  public String serialize(List<T> values) {
    return serialize(values, getSeparator().substring(0, 1));
  }

  public String serialize(@NotNull List<T> values, @Nullable String separator) {
    values = firstNonNull(values, ImmutableList.<T>of());
    return FluentIterable.from(values).transform(marshaller.getSerializer()).filter(
        Predicates.notNull()).join(Joiner.on(firstNonNull(separator, DEFAULT_SEPARATOR)));
  }

  @Override
  public List<T> resolve(Object obj) {
    return FluentIterable.from(asStringList(obj)).transform(marshaller.getResolver()).filter(
        Predicates.notNull()).toList();
  }

  private List<String> asStringList(Object obj) {
    List<String> list = ImmutableList.of();
    if (obj instanceof String) {
      list = Splitter.onPattern("[" + getSeparator() + "]").splitToList((String) obj);
    } else if (obj instanceof List) {
      list = FluentIterable.from((List<?>) obj).filter(Predicates.notNull()).transform(
          Functions.toStringFunction()).toList();
    } else if (obj != null) {
      LOGGER.warn("unable to resolve value '{}' for '{}'", obj, this);
    }
    return list;
  }

  public Marshaller<T> getMarshaller() {
    return marshaller;
  }

  public boolean isMultiSelect() {
    return firstNonNull(multiSelect, false);
  }

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
