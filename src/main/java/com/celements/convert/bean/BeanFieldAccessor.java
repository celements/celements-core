package com.celements.convert.bean;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;

import com.celements.model.access.field.FieldAccessException;
import com.celements.model.access.field.FieldAccessor;
import com.celements.model.access.field.FieldMissingException;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.list.ListField;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * {@link FieldAccessor} using {@link PropertyUtils} to call getters/setters of a simple bean. see
 * {{@link #getBeanMethodName(ClassField)} to check expected naming.
 */
@Component(BeanFieldAccessor.NAME)
public class BeanFieldAccessor<T> implements FieldAccessor<T> {

  private final static Logger LOGGER = LoggerFactory.getLogger(BeanFieldAccessor.class);

  public static final String NAME = "bean";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public <V> V getValue(T obj, ClassField<V> field) throws FieldAccessException {
    V ret;
    try {
      ret = field.getType().cast(PropertyUtils.getProperty(obj, getBeanMethodName(field)));
    } catch (NoSuchMethodException exc) {
      throw new FieldMissingException(exc);
    } catch (ReflectiveOperationException | ClassCastException exc) {
      throw new FieldAccessException(exc);
    }
    LOGGER.info("getValue: '{}' for '{}' from '{}'", ret, field, obj);
    return ret;
  }

  @Override
  public <V> void setValue(T obj, ClassField<V> field, V value) throws FieldAccessException {
    try {
      PropertyUtils.setProperty(obj, getBeanMethodName(field), resolveSetValue(field, value));
    } catch (NoSuchMethodException exc) {
      throw new FieldMissingException(exc);
    } catch (ReflectiveOperationException exc) {
      throw new FieldAccessException(exc);
    }
    LOGGER.info("setValue: '{}' for '{}' from '{}'", value, field, obj);
  }

  /**
   * resolves the correct value for single select fields so that the bean can have a
   */
  private Object resolveSetValue(ClassField<?> field, Object value) {
    try {
      // set first value for single select list fields
      if ((value != null) && !((ListField<?>) field).isMultiSelect()) {
        LOGGER.debug("resolveSetValue: for '{}' with value '{}'", field, value);
        value = Iterables.getFirst((List<?>) value, null);
      }
    } catch (ClassCastException exc) {
      LOGGER.trace("resolveSetValue: expected for non-list field '{}'", field, exc);
    }
    return value;
  }

  /**
   * creates a bean method name only containing letters and digits in camel case from a
   * ClassField<br>
   * e.g. <b>some_class.Field</b> -> <b>someClassField</b>
   */
  private String getBeanMethodName(ClassField<?> field) {
    StringBuilder sb = new StringBuilder();
    Iterator<Character> iter = Lists.charactersOf(field.getName()).iterator();
    boolean toUpperCase = false;
    while (iter.hasNext()) {
      Character c = iter.next();
      if (Character.isLetterOrDigit(c)) {
        sb.append(toUpperCase ? Character.toUpperCase(c) : c);
        toUpperCase = false;
      } else {
        // capitalize next letter if not first
        toUpperCase = sb.length() > 0;
      }
    }
    LOGGER.debug("getBeanMethodName: '{}' for '{}'", sb, field);
    return sb.toString();
  }

}
