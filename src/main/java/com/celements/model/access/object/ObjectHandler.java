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
  ObjectBridge<D, O> getBridge();

  @NotNull
  DocumentReference getDocRef();

  /**
   * @return clone of the current query
   */
  @NotNull
  ObjectQuery<O> getQuery();

  @NotNull
  ObjectHandler<D, O> with(@NotNull ObjectQuery<O> query);

  @NotNull
  ObjectHandler<D, O> filter(@NotNull ObjectRestriction<O> restriction);

  /**
   * filters objects with the given {@link ClassField}
   *
   * @param classRef
   */
  @NotNull
  ObjectHandler<D, O> filter(@NotNull ClassReference classRef);

  /**
   * filters objects for the given {@link ClassField} and value<br>
   * <br>
   * NOTE: value may not be null, instead use {@link #filterAbsent(ClassField)}
   *
   * @param field
   * @param value
   */
  @NotNull
  <T> ObjectHandler<D, O> filter(@NotNull ClassField<T> field, @NotNull T value);

  /**
   * filters objects for the given {@link ClassField} and possible values (logical OR)
   *
   * @param field
   * @param values
   */
  @NotNull
  <T> ObjectHandler<D, O> filter(@NotNull ClassField<T> field, @NotNull Collection<T> values);

  /**
   * filters objects with no value for the given {@link ClassField}
   *
   * @param field
   * @return
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
