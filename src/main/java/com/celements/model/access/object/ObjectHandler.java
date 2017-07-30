package com.celements.model.access.object;

import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.object.restriction.ObjectQuery;
import com.celements.model.access.object.restriction.ObjectRestriction;
import com.celements.model.classes.fields.ClassField;

/**
 * Handles retrieval and manipulation of objects O on a document D. Use {@link #filter()}
 * methods to construct the desired query, then use {@link #fetch()} to retrieve (read-only) and
 * {@link #edit()} to manipulate objects.
 *
 * @param <D>
 *          document type
 * @param <O>
 *          object type
 */
public interface ObjectHandler<D, O> {

  @NotNull
  DocumentReference getDocRef();

  /**
   * @return clone of the current query
   */
  @NotNull
  ObjectQuery<O> getQuery();

  /**
   * adds all restrictions from the the given {@link ObjectQuery}
   */
  @NotNull
  ObjectHandler<D, O> with(@NotNull ObjectQuery<O> query);

  /**
   * restricts to objects for the given {@link ObjectRestriction}
   */
  @NotNull
  ObjectHandler<D, O> filter(@NotNull ObjectRestriction<O> restriction);

  /**
   * restricts to objects with the given {@link ClassField}
   */
  @NotNull
  ObjectHandler<D, O> filter(@NotNull ClassReference classRef);

  /**
   * restricts to objects for the given {@link ClassField} and value<br>
   * <br>
   * NOTE: value may not be null, instead use {@link #filterAbsent(ClassField)}
   */
  @NotNull
  <T> ObjectHandler<D, O> filter(@NotNull ClassField<T> field, @NotNull T value);

  /**
   * restricts to objects for the given {@link ClassField} and possible values (logical OR)
   */
  @NotNull
  <T> ObjectHandler<D, O> filter(@NotNull ClassField<T> field, @NotNull Collection<T> values);

  /**
   * restricts to objects with no value for the given {@link ClassField}
   */
  @NotNull
  ObjectHandler<D, O> filterAbsent(@NotNull ClassField<?> field);

  /**
   * @return a new {@link ObjectFetcher} for object retrieval. All objects returned by this fetcher
   *         will be clones and are therefore only useful for read-only operations.
   */
  @NotNull
  ObjectFetcher<D, O> fetch();

  /**
   * @return a new {@link ObjectEditor} for object manipulation
   */
  @NotNull
  ObjectEditor<D, O> edit();

}
