package com.celements.model.classes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.classes.fields.ClassField;
import com.celements.model.util.ModelUtils;

public abstract class AbstractClassDefinition implements ClassDefinition {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClassDefinition.class);

  @Requirement
  protected ModelUtils modelUtils;

  @Requirement
  protected ConfigurationSource configSrc;

  private volatile List<ClassField<?>> fields;

  @Override
  public DocumentReference getClassRef() {
    return getClassRef(null);
  }

  @Override
  public DocumentReference getClassRef(WikiReference wikiRef) {
    return modelUtils.adjustRef(getClassRefInternal(), DocumentReference.class, wikiRef);
  }

  /**
   * @return class ref for this class definition. wiki will be adjusted
   */
  protected abstract DocumentReference getClassRefInternal();

  @Override
  public boolean isBlacklisted() {
    boolean ret = false;
    Object prop = configSrc.getProperty(CFG_SRC_KEY);
    if (prop instanceof List) {
      ret = ((List<?>) prop).contains(getName());
    }
    LOGGER.debug("isBlacklisted: '{}' for '{}'", ret, getName());
    return ret;
  }

  @Override
  public List<ClassField<?>> getFields() {
    if (fields == null) {
      fields = new ArrayList<>();
      for (Field field : this.getClass().getDeclaredFields()) {
        try {
          if (ClassField.class.isAssignableFrom(field.getType())) {
            fields.add((ClassField<?>) field.get(this));
          }
        } catch (IllegalAccessException | IllegalArgumentException exc) {
          LOGGER.error("failed to get field '{}", field, exc);
        }
      }
    }
    return Collections.unmodifiableList(fields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClassRef());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ClassDefinition) {
      ClassDefinition other = (ClassDefinition) obj;
      return Objects.equals(this.getClassRef(), other.getClassRef());
    }
    return false;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(modelUtils.serializeRefLocal(getClassRef())).toString();
  }

}
