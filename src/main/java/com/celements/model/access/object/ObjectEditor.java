package com.celements.model.access.object;

import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

/**
 * Manipulates objects O on a document D for the defined filter or fetches them for manipulation.
 *
 * @param <D>
 *          document type
 * @param <O>
 *          object type
 */
@Immutable
public interface ObjectEditor<D, O> {

  /**
   * creates all objects defined by the filter and also sets fields if any
   *
   * @return a map of all created objects indexed by their {@link ClassReference}
   */
  @NotNull
  Map<ClassReference, O> create();

  /**
   * like {@link #create()} but fetches an object if it exists already
   *
   * @return a map of all created or fetched objects indexed by their {@link ClassReference}
   */
  @NotNull
  Map<ClassReference, O> createIfNotExists();

  /**
   * @return a list of all removed objects defined in by filter
   */
  @NotNull
  List<O> remove();

  /**
   * @return a new {@link ObjectHandler} with an equal filter
   */
  @NotNull
  ObjectHandler<D, O> handle();

  /**
   * @return a fetcher which returns objects for manipulation
   */
  @NotNull
  ObjectFetcher<D, O> fetch();

}
