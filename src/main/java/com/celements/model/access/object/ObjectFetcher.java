package com.celements.model.access.object;

import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.google.common.base.Optional;

/**
 * Fetches objects O on a document D for the defined filter.
 *
 * @param <D>
 *          document type
 * @param <O>
 *          object type
 */
@Immutable
public interface ObjectFetcher<D, O> {

  /**
   * @return true if no objects were fetched
   */
  boolean isEmpty();

  /**
   * @return the first fetched object
   */
  @NotNull
  Optional<O> first();

  /**
   * @param objNb
   * @return the fetched object with the specified object number
   */
  @NotNull
  Optional<O> number(int objNb);

  /**
   * @return a list of all fetched objects
   */
  @NotNull
  List<O> list();

  /**
   * @return a map of all fetched objects indexed by their {@link ClassReference}
   */
  @NotNull
  Map<ClassReference, List<O>> map();

  /**
   * @return a new {@link ObjectHandler} with an equal filter
   */
  @NotNull
  ObjectHandler<D, O> handle();

}
