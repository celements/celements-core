package com.celements.model.access.object;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface XObjectHandler {

  public @NotNull XObjectHandler onDoc(@NotNull XWikiDocument doc);

  public @NotNull XObjectHandler filter(@NotNull DocumentReference classRef);

  public @NotNull <T> XObjectHandler filter(@NotNull ClassField<T> field, @Nullable T value);

  public @NotNull <T> XObjectHandler filter(@NotNull ClassField<T> field,
      @NotNull Collection<T> values);

  /**
   * @deprecated instead use {{@link #filter(ClassField, Object)}
   */
  @Deprecated
  public @NotNull XObjectHandler filter(@NotNull DocumentReference classRef, @NotNull String key,
      @Nullable Object value);

  /**
   * @deprecated instead use {{@link #filter(ClassField, Collection)
   */
  @Deprecated
  public @NotNull XObjectHandler filter(@NotNull DocumentReference classRef, @NotNull String key,
      @NotNull Collection<?> values);

  public @NotNull Optional<BaseObject> fetchFirst();

  public @NotNull Optional<BaseObject> fetchNumber(int objNb);

  public @NotNull List<BaseObject> fetchList();

  public @NotNull Map<DocumentReference, List<BaseObject>> fetchMap();

  public @NotNull List<BaseObject> create();

  public @NotNull List<BaseObject> createIfNotExists();

  public @NotNull List<BaseObject> remove();

}
