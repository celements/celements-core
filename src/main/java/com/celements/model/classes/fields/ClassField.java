package com.celements.model.classes.fields;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.PropertyInterface;

@Immutable
public interface ClassField<T> {

  @NotNull
  public DocumentReference getClassRef();

  @NotNull
  public String getName();

  @NotNull
  public Class<T> getType();

  @NotNull
  public PropertyInterface getXField();

  @NotNull
  public String toString(boolean local);

}
