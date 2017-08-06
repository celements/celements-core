package com.celements.model.access.object;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.object.restriction.ObjectQuery;
import com.celements.model.classes.ClassIdentity;
import com.google.common.base.Optional;

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
   * @return a map of all created objects indexed by their {@link ClassIdentity}
   */
  @NotNull
  Map<ClassIdentity, O> create();

  /**
   * like {@link #create()} but fetches an object if it exists already
   *
   * @return a map of all created or fetched objects indexed by their {@link ClassIdentity}
   */
  @NotNull
  Map<ClassIdentity, O> createIfNotExists();

  /**
   * creates the first object defined by the query and also sets fields if any
   */
  @NotNull
  O createFirst();

  /**
   * like {@link #createFirst()} but fetches an object if it exists already
   */
  @NotNull
  O createFirstIfNotExists();

  /**
   * deletes all objects defined by the query
   *
   * @return a list of all deleted objects
   */
  @NotNull
  List<O> delete();

  /**
   * deletes the first object defined by the query
   */
  @NotNull
  Optional<O> deleteFirst();

  /**
   * @return a fetcher which returns objects for manipulation
   */
  @NotNull
  ObjectFetcher<D, O> fetch();

}
