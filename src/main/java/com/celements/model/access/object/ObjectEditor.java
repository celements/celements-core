package com.celements.model.access.object;

import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

@Immutable
public interface ObjectEditor<D, O> {

  @NotNull
  Map<ClassReference, O> create();

  @NotNull
  Map<ClassReference, O> createIfNotExists();

  @NotNull
  List<O> remove();

  @NotNull
  ObjectHandler<D, O> handle();

  @NotNull
  ObjectFetcher<D, O> fetch();

}
