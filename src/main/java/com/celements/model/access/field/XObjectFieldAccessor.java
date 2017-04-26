package com.celements.model.access.field;

import static com.google.common.base.Preconditions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.fields.ClassField;
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
  public <V> V getValue(BaseObject obj, ClassField<V> field) throws FieldAccessException {
    checkClassRef(obj, field);
    V ret = modelAccess.getFieldValue(obj, field).orNull();
    LOGGER.info("getValue: '{}' for '{}' from '{} - {} - {}'", ret, field,
        obj.getDocumentReference(), obj.getXClassReference(), obj.getNumber());
    return ret;
  }

  @Override
  public <V> void setValue(BaseObject obj, ClassField<V> field, V value)
      throws FieldAccessException {
    checkClassRef(obj, field);
    modelAccess.setProperty(obj, field, value);
    LOGGER.info("setValue: '{}' for '{}' from '{} - {} - {}'", value, field,
        obj.getDocumentReference(), obj.getXClassReference(), obj.getNumber());
  }

  private void checkClassRef(BaseObject obj, ClassField<?> field) throws FieldAccessException {
    DocumentReference classRef = checkNotNull(obj).getXClassReference();
    if ((classRef == null) || !classRef.equals(checkNotNull(field).getClassDef().getClassRef(
        classRef.getWikiReference()))) {
      throw new FieldAccessException("BaseObject uneligible for '" + field + "', it's of class '"
          + classRef + "'");
    }
  }

}
