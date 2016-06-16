package com.celements.model.classes.fields.list;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.google.common.base.Joiner;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.StaticListClass;

public abstract class StaticListField<T> extends ListField<T> {

  private List<String> values;

  public StaticListField(@NotNull DocumentReference classRef, @NotNull String name,
      boolean multiSelect) {
    super(classRef, name, multiSelect);
  }

  public List<String> getValues() {
    return values;
  }

  public StaticListField<T> setValues(@NotNull List<String> values) {
    this.values = values;
    return this;
  }

  @Override
  protected ListClass getListClass() {
    StaticListClass element = new StaticListClass();
    if (values != null) {
      element.setValues(Joiner.on('|').join(values));
    }
    return element;
  }

}
