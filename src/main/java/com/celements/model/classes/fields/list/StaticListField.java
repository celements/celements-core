package com.celements.model.classes.fields.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.google.common.base.Joiner;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.StaticListClass;

public class StaticListField extends StringListField {

  private volatile List<String> values;

  public StaticListField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  public List<String> getValues() {
    return values;
  }

  public StaticListField setValues(@NotNull List<String> values) {
    this.values = Collections.unmodifiableList(new ArrayList<>(values));
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
