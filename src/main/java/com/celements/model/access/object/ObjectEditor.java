package com.celements.model.access.object;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.object.restriction.ObjectQuery;

/**
 * Manipulates objects O on a document D for the defined query or fetches them for manipulation. Use
 * {@link ObjectFetcher} instead for read-only operations.
 *
 * @param <D>
 *          document type
 * @param <O>
 *          object type
 */
public interface ObjectEditor<D, O> {

  @NotNull
  DocumentReference getDocRef();

  /**
   * @return clone of the current query
   */
  @NotNull
  ObjectQuery<O> getQuery();

  /**
   * creates all objects defined by the query and also sets fields if any
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
   * @return a list of all removed objects defined by the query
   */
  @NotNull
  List<O> remove();

  /**
   * @return a fetcher which returns objects for manipulation
   */
  @NotNull
  ObjectFetcher<D, O> fetch();

}
