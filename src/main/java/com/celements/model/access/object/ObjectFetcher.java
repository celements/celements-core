package com.celements.model.access.object;

import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.google.common.base.Optional;

@Immutable
public interface ObjectFetcher<D, O> {

  boolean hasValues();

  @NotNull
  Optional<O> first();

  @NotNull
  Optional<O> number(int objNb);

  @NotNull
  List<O> list();

  @NotNull
  Map<ClassReference, List<O>> map();

  @NotNull
  List<ClassReference> getClassRefs();

}
