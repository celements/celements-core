package com.celements.model.access.field;

import static com.google.common.base.Preconditions.*;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;
import com.xpn.xwiki.objects.BaseObject;

/**
 * {@link FieldAccessor} for accessing {@link BaseObject} properties
 */
@Component(XObjectFieldAccessor.NAME)
public class XObjectFieldAccessor implements FieldAccessor<BaseObject> {

  private final static Logger LOGGER = LoggerFactory.getLogger(XObjectFieldAccessor.class);

  public static final String NAME = "xobject";

  @Requirement
  private IModelAccessFacade modelAccess;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public <V> Optional<V> getValue(BaseObject obj, ClassField<V> field) throws FieldAccessException {
    checkClassRef(obj, field);
    Optional<V> value = modelAccess.getFieldValue(obj, field);
    LOGGER.info("getValue: '{}' for '{}' from '{} - {} - {}'", value.orNull(), field,
        obj.getDocumentReference(), obj.getXClassReference(), obj.getNumber());
    return value;
  }

  @Override
  public <V> boolean setValue(BaseObject obj, ClassField<V> field, V value)
      throws FieldAccessException {
    checkClassRef(obj, field);
    boolean dirty = modelAccess.setProperty(obj, field, value);
    if (dirty) {
      LOGGER.info("setValue: '{}' for '{}' from '{} - {} - {}'", value, field,
          obj.getDocumentReference(), obj.getXClassReference(), obj.getNumber());
    }
    return dirty;
  }

  private void checkClassRef(BaseObject obj, ClassField<?> field) throws FieldAccessException {
    checkNotNull(obj);
    checkNotNull(field);
    ClassReference classRef = new ClassReference(obj.getXClassReference());
    if (!classRef.equals(field.getClassDef().getClassReference())) {
      throw new FieldAccessException(MessageFormat.format(
          "BaseObject uneligible for ''{0}'', it's of class ''{1}''", field, classRef));
    }
  }

}
