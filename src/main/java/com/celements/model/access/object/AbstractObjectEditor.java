package com.celements.model.access.object;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.FluentIterable.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.object.restriction.FieldRestriction;
import com.celements.model.access.object.restriction.ObjectQueryBuilder;
import com.celements.model.classes.ClassIdentity;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

@NotThreadSafe
public abstract class AbstractObjectEditor<R extends AbstractObjectEditor<R, D, O>, D, O> extends
    ObjectQueryBuilder<R, O> implements ObjectEditor<D, O> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ObjectEditor.class);

  protected final D doc;

  protected AbstractObjectEditor(@NotNull D doc) {
    this.doc = checkNotNull(doc);
    getBridge().checkDoc(doc);
  }

  @Override
  public DocumentReference getDocRef() {
    return getBridge().getDocRef(doc);
  }

  @Override
  public Map<ClassIdentity, O> create() {
    return create(false);
  }

  @Override
  public Map<ClassIdentity, O> createIfNotExists() {
    return create(true);
  }

  private Map<ClassIdentity, O> create(boolean ifNotExists) {
    return from(getQuery().getObjectClasses()).toMap(new ObjectCreateFunction(ifNotExists));
  }

  @Override
  public O createFirst() {
    return createFirst(false);
  }

  @Override
  public O createFirstIfNotExists() {
    return createFirst(true);
  }

  private O createFirst(boolean ifNotExists) {
    Optional<ClassIdentity> classId = from(getQuery().getObjectClasses()).first();
    if (classId.isPresent()) {
      return new ObjectCreateFunction(ifNotExists).apply(classId.get());
    } else {
      throw new IllegalArgumentException("no class defined");
    }
  }

  private class ObjectCreateFunction implements Function<ClassIdentity, O> {

    private final boolean ifNotExists;

    ObjectCreateFunction(boolean ifNotExists) {
      this.ifNotExists = ifNotExists;
    }

    @Override
    public O apply(ClassIdentity classId) {
      O obj = null;
      if (ifNotExists) {
        obj = fetch().filter(classId).first().orNull();
      }
      if (obj == null) {
        obj = getBridge().createObject(doc, classId);
        getQuery().getFieldRestrictions(classId).forEach(new FieldSetter(obj));
        LOGGER.info("{} created object {} for {}", AbstractObjectEditor.this,
            getBridge().getObjectNumber(obj), classId);
      }
      return obj;
    }

    private class FieldSetter implements Consumer<FieldRestriction<O, ?>> {

      private final O obj;

      FieldSetter(O obj) {
        this.obj = obj;
      }

      @Override
      public void accept(FieldRestriction<O, ?> restriction) {
        setField(obj, restriction);
      }

      <T> void setField(O obj, FieldRestriction<O, T> restriction) {
        T value = from(restriction.getValues()).first().get();
        getBridge().setObjectField(obj, restriction.getField(), value);
        LOGGER.debug("{} set field {} on created object to value", AbstractObjectEditor.this,
            restriction.getField(), value);
      }

    }

  }

  @Override
  public List<O> delete() {
    return from(fetch().list()).filter(new ObjectDeletePredicate()).toList();
  }

  @Override
  public Optional<O> deleteFirst() {
    Optional<O> obj = fetch().first();
    if (obj.isPresent() && new ObjectDeletePredicate().apply(obj.get())) {
      return obj;
    }
    return Optional.absent();
  }

  private class ObjectDeletePredicate implements Predicate<O> {

    @Override
    public boolean apply(O obj) {
      boolean success = getBridge().deleteObject(doc, obj);
      LOGGER.info("{} deleted object {} for {}: {}", AbstractObjectEditor.this,
          getBridge().getObjectNumber(obj), getBridge().getObjectClass(obj), success);
      return success;
    }
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [doc=" + getBridge().getDocRef(doc) + ", query="
        + getQuery() + "]";
  }

  @Override
  public abstract AbstractObjectFetcher<?, D, O> fetch();

  @Override
  protected abstract @NotNull ObjectBridge<D, O> getBridge();

}
