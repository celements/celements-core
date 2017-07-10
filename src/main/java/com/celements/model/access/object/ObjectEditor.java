package com.celements.model.access.object;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

@NotThreadSafe
public interface ObjectEditor<D, O> {

  @NotNull
  List<O> create();

  @NotNull
  List<O> createIfNotExists();

  @NotNull
  List<O> remove();

}
