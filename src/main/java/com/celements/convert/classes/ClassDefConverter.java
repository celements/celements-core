package com.celements.convert.classes;

import javax.validation.constraints.NotNull;

import com.celements.convert.ConversionException;
import com.celements.convert.Converter;
import com.celements.model.access.field.FieldAccessException;
import com.celements.model.access.field.FieldAccessor;
import com.celements.model.access.field.FieldMissingException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;

/**
 * abstract {@link Converter} simplifying conversions based on a specific {@link ClassDefinition} by
 * using {@link FieldAccessor} implementations.
 */
public abstract class ClassDefConverter<A, B> implements Converter<A, B> {

  protected abstract @NotNull ClassDefinition getClassDef();

  protected abstract @NotNull B createInstance();

  protected abstract @NotNull FieldAccessor<A> getFromFieldConverter();

  protected abstract @NotNull FieldAccessor<B> getToFieldConverter();

  @Override
  public B apply(A data) throws ConversionException {
    B instance = createInstance();
    if (data != null) {
      for (ClassField<?> field : getClassDef().getFields()) {
        try {
          convertField(field, getToFieldConverter(), instance, getFromFieldConverter(), data);
        } catch (FieldAccessException exc) {
          handle(exc);
        }
      }
    }
    return instance;
  }

  private static <V, A, B> void convertField(ClassField<V> field, FieldAccessor<A> toConverter,
      A to, FieldAccessor<B> fromConverter, B from) throws FieldAccessException {
    toConverter.setValue(to, field, fromConverter.getValue(from, field));
  }

  private void handle(FieldAccessException exc) throws ConversionException {
    if ((exc instanceof FieldMissingException) && omitIncompletness()) {
      // TODO log it
    } else {
      throw new ConversionException(exc);
    }
  }

  /**
   * allows the implementation to omit incompleteness if a {@link FieldAccessor} throws a
   * {@link FieldMissingException}
   */
  protected boolean omitIncompletness() {
    return false;
  }

}
