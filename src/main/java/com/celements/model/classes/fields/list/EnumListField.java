package com.celements.model.classes.fields.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.StaticListClass;

@Immutable
public final class EnumListField<E extends Enum<E>> extends ListField<E> {

  private final Class<E> enumType;

  public static class Builder<E extends Enum<E>> extends ListField.Builder<Builder<E>, E> {

    private final Class<E> enumType;

    public Builder(@NotNull DocumentReference classRef, @NotNull String name,
        @NotNull Class<E> enumType) {
      super(classRef, name);
      this.enumType = Preconditions.checkNotNull(enumType);
    }

    @Override
    public Builder<E> getThis() {
      return this;
    }

    @Override
    public EnumListField<E> build() {
      return new EnumListField<E>(getThis());
    }

  }

  protected EnumListField(@NotNull Builder<E> builder) {
    super(builder);
    this.enumType = builder.enumType;
  }

  @Override
  public Object serialize(List<E> value) {
    Object ret = null;
    if (value != null) {
      StringBuilder sb = new StringBuilder();
      for (E val : value) {
        if (sb.length() > 0) {
          sb.append(getSeparator());
        }
        sb.append(val.name());
      }
      ret = sb.toString();
    }
    return ret;
  }

  @Override
  protected List<E> resolveList(List<?> list) {
    List<E> ret = new ArrayList<>();
    for (Object elem : (Collection<?>) list) {
      ret.add(Enum.valueOf(enumType, elem.toString()));
    }
    return Collections.unmodifiableList(ret);
  }

  @Override
  protected ListClass getListClass() {
    StaticListClass element = new StaticListClass();
    element.setValues(Joiner.on('|').join(enumType.getEnumConstants()));
    return element;
  }

}
