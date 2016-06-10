package com.celements.model.classes.fields;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.PropertyInterface;

public interface CelObjectField<T> {

  @NotNull
  public DocumentReference getClassRef();

  @NotNull
  public String getName();

  @NotNull
  public PropertyInterface getXField();

  @Nullable
  public T resolveFromXFieldValue(@Nullable Object obj);

  @Nullable
  public Object serializeToXFieldValue(@Nullable T value);

  @NotNull
  public String toString(boolean local);

}
