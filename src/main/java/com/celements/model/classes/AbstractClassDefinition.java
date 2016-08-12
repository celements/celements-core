package com.celements.model.classes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.model.classes.fields.ClassField;

public abstract class AbstractClassDefinition implements ClassDefinition {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClassDefinition.class);

  @Requirement
  protected ConfigurationSource configSrc;

  private volatile List<ClassField<?>> fields;

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

}
