package com.celements.model.access.object;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;

/**
 * Bridge for effective access on document and objects, primarily used by {@link ObjectHandler}s to
 * allow generic implementations
 *
 * @param <D>
 *          document type
 * @param <O>
 *          object type
 */
@Immutable
@Singleton
@ComponentRole
public interface ObjectBridge<D, O> {

  @NotNull
  Class<D> getDocumentType();

  @NotNull
  Class<O> getObjectType();

  void checkDoc(@NotNull D doc) throws IllegalArgumentException;

  @NotNull
  DocumentReference getDocRef(@NotNull D doc);

  @NotNull
  List<ClassReference> getDocClassRefs(@NotNull D doc);

  @NotNull
  List<O> getObjects(@NotNull D doc, @NotNull ClassReference classRef);

  int getObjectNumber(@NotNull O obj);

  @NotNull
  ClassReference getObjectClassRef(@NotNull O obj);

  @NotNull
  O cloneObject(@NotNull O obj);

  @NotNull
  O createObject(@NotNull D doc, @NotNull ClassReference classRef);

  boolean removeObject(@NotNull D doc, @NotNull O obj);

  @NotNull
  <T> Optional<T> getObjectField(@NotNull O obj, @NotNull ClassField<T> field);

  <T> boolean setObjectField(@NotNull O obj, @NotNull ClassField<T> field, @Nullable T value);

}
