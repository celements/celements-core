package com.celements.model.access.object;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.object.restriction.ObjectQuery;
import com.google.common.base.Optional;

/**
 * Fetches objects O on a document D for the defined query. Returned objects are intended for
 * read-only operations. Use {@link ObjectEditor} instead for manipulations.
 *
 * @param <D>
 *          document type
 * @param <O>
 *          object type
 */
public interface ObjectFetcher<D, O> {

  @NotNull
  DocumentReference getDocRef();

  /**
   * @return clone of the current query
   */
  @NotNull
  ObjectQuery<O> getQuery();

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

}
