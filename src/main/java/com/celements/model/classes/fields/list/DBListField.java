package com.celements.model.classes.fields.list;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.classes.DBListClass;
import com.xpn.xwiki.objects.classes.ListClass;

public class DBListField extends ListField {

  private String sql;

  public DBListField(@NotNull DocumentReference classRef, @NotNull String name) {
    super(classRef, name);
  }

  public String getSql() {
    return sql;
  }

  public DBListField setSql(String sql) {
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
