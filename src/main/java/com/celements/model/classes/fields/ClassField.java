package com.celements.model.classes.fields;

import javax.validation.constraints.NotNull;

import com.celements.model.classes.ClassDefinition;
import com.xpn.xwiki.objects.PropertyInterface;

public interface ClassField<T> {

  @NotNull
  public ClassDefinition getClassDef();

  @NotNull
  public String getName();

  @NotNull
  public Class<T> getType();

  @NotNull
  public PropertyInterface getXField();

}
