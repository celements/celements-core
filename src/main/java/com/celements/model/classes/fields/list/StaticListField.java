package com.celements.model.classes.fields.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.google.common.base.Joiner;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.StaticListClass;

@Immutable
public final class StaticListField extends StringListField {

  private final List<String> values;

  public static class Builder extends ListField.Builder<Builder, String> {

    private List<String> values;

    public Builder(@NotNull DocumentReference classRef, @NotNull String name) {
      super(classRef, name);
      values = new ArrayList<>();
    }

    @Override
    public Builder getThis() {
      return this;
    }

    public Builder values(@NotNull List<String> val) {
      values = val;
      return this;
    }

    @Override
    public StaticListField build() {
      return new StaticListField(this);
    }

  }

  protected StaticListField(@NotNull Builder builder) {
    super(builder);
    this.values = Collections.unmodifiableList(new ArrayList<>(builder.values));
  }

  public List<String> getValues() {
    return values;
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
