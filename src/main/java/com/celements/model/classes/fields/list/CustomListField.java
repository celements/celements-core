package com.celements.model.classes.fields.list;

import static com.google.common.base.MoreObjects.*;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.StaticListClass;

public abstract class CustomListField<T> extends ListField<T> {

  protected CustomListField(ListField.Builder<?, T> builder) {
    super(builder);
  }

  @Override
  public Object serialize(List<T> value) {
    value = firstNonNull(value, Collections.<T>emptyList());
    return FluentIterable.from(value).transform(getSerializeFunction()).filter(
        Predicates.notNull()).join(Joiner.on(getSeparator()));
  }

  protected abstract Function<T, Object> getSerializeFunction();

  @Override
  protected List<T> resolveList(List<?> list) {
    list = firstNonNull(list, Collections.emptyList());
    return FluentIterable.from(list).transform(getResolveFunction()).filter(
        Predicates.notNull()).toList();
  }

  protected abstract Function<Object, T> getResolveFunction();

  @Override
  protected ListClass getListClass() {
    StaticListClass element = new StaticListClass();
    element.setValues((String) serialize(getPossibleValues()));
    return element;
  }

  protected abstract List<T> getPossibleValues();

}
