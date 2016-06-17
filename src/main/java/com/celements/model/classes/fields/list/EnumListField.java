package com.celements.model.classes.fields.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.StaticListClass;

public class EnumListField<T extends Enum<T>> extends ListField<T> {

  private final Class<T> enumType;

  public EnumListField(@NotNull DocumentReference classRef, @NotNull String name,
      Class<T> enumType) {
    super(classRef, name);
    this.enumType = Preconditions.checkNotNull(enumType);
  }

  @Override
  public Object serialize(List<T> value) {
    Object ret = null;
    if (value != null) {
      StringBuilder sb = new StringBuilder();
      for (T val : value) {
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
  protected List<T> resolveList(List<?> list) {
    List<T> ret = new ArrayList<>();
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
