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

  private final ObjectQuery<O> query;

  public ObjectQueryBuilder() {
    this.query = new ObjectQuery<>();
  }

  protected abstract @NotNull ObjectBridge<?, O> getBridge();

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
    return filter(new ClassRestriction<>(getBridge(), classRef));
  }

  /**
   * restricts to objects for the given {@link ClassField} and value<br>
   * <br>
   * NOTE: value may not be null, instead use {@link #filterAbsent(ClassField)}
   */
  public final @NotNull <T> B filter(@NotNull ClassField<T> field, @NotNull T value) {
    return filter(new FieldRestriction<>(getBridge(), field, value));
  }

  /**
   * restricts to objects for the given {@link ClassField} and possible values (logical OR)
   */
  public final @NotNull <T> B filter(@NotNull ClassField<T> field, @NotNull Collection<T> values) {
    return filter(new FieldRestriction<>(getBridge(), field, values));
  }

  /**
   * restricts to objects with no value for the given {@link ClassField}
   */
  public final @NotNull B filterAbsent(@NotNull ClassField<?> field) {
    return filter(new FieldAbsentRestriction<>(getBridge(), field));
  }

  /**
   * restricts to objects with the given number
   */
  public final @NotNull B filter(int number) {
    return filter(new NumberRestriction<>(getBridge(), number));
  }

  /**
   * restricts to the given object
   */
  public final @NotNull B filter(O obj) {
    return filter(new IdentityRestriction<>(getBridge(), obj));
  }

  public final ObjectQuery<O> getQuery() {
    return new ObjectQuery<>(query);
  }

  protected abstract B getThis();

  @Override
  public String toString() {
    return query.toString();
  }

}
