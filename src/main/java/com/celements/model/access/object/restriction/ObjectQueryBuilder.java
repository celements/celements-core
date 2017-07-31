package com.celements.model.access.object.restriction;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.ObjectBridge;
import com.celements.model.classes.fields.ClassField;

@NotThreadSafe
public abstract class ObjectQueryBuilder<B extends ObjectQueryBuilder<B, O>, O> {

  protected final ObjectBridge<?, O> bridge;
  private final ObjectQuery<O> query;

  public ObjectQueryBuilder(@NotNull ObjectBridge<?, O> bridge) {
    this.query = new ObjectQuery<>();
    this.bridge = checkNotNull(bridge);
  }

  public final ObjectBridge<?, O> getBridge() {
    return bridge;
  }

  /**
   * adds all restrictions from the the given {@link ObjectQuery}
   */
  public final @NotNull B with(@NotNull ObjectQuery<O> query) {
    this.query.addAll(query);
    return getThis();
  }

  /**
   * restricts to objects for the given {@link ObjectRestriction}
   */
  public final @NotNull B filter(@NotNull ObjectRestriction<O> restriction) {
    query.add(checkNotNull(restriction));
    return getThis();
  }

  /**
   * restricts to objects with the given {@link ClassField}
   */
  public final @NotNull B filter(@NotNull ClassReference classRef) {
    filter(new ClassRestriction<>(getBridge(), classRef));
    return getThis();
  }

  /**
   * restricts to objects for the given {@link ClassField} and value<br>
   * <br>
   * NOTE: value may not be null, instead use {@link #filterAbsent(ClassField)}
   */
  public final @NotNull <T> B filter(@NotNull ClassField<T> field, @NotNull T value) {
    filter(new FieldRestriction<>(getBridge(), field, value));
    return getThis();
  }

  /**
   * restricts to objects for the given {@link ClassField} and possible values (logical OR)
   */
  public final @NotNull <T> B filter(@NotNull ClassField<T> field, @NotNull Collection<T> values) {
    filter(new FieldRestriction<>(getBridge(), field, values));
    return getThis();
  }

  /**
   * restricts to objects with no value for the given {@link ClassField}
   */
  public final @NotNull B filterAbsent(@NotNull ClassField<?> field) {
    filter(new FieldAbsentRestriction<>(getBridge(), field));
    return getThis();
  }

  public final ObjectQuery<O> buildQuery() {
    return new ObjectQuery<>(query);
  }

  protected abstract B getThis();

  @Override
  public String toString() {
    return query.toString();
  }

}
