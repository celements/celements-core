package com.celements.model.classes.fields;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.PropertyInterface;

public interface ClassField<T> {

  @NotNull
  public DocumentReference getClassRef();

  @NotNull
  public String getName();

  @NotNull
  public PropertyInterface getXField();

  @NotNull
  public String toString(boolean local);

}
