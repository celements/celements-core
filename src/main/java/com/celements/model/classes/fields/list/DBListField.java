package com.celements.model.classes.fields.list;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.classes.DBListClass;
import com.xpn.xwiki.objects.classes.ListClass;

public abstract class DBListField<T> extends ListField<T> {

  private String sql;

  public DBListField(@NotNull DocumentReference classRef, @NotNull String name,
      boolean multiSelect) {
    super(classRef, name, multiSelect);
  }

  public String getSql() {
    return sql;
  }

  public DBListField<T> setSql(String sql) {
    this.sql = sql;
    return this;
  }

  @Override
  protected ListClass getListClass() {
    DBListClass element = new DBListClass();
    if (sql != null) {
      element.setSql(sql);
    }
    return element;
  }

}
