package com.celements.convert.classes;

import org.xwiki.component.annotation.Requirement;

import com.celements.model.access.field.FieldAccessor;
import com.celements.model.access.field.XObjectFieldAccessor;
import com.xpn.xwiki.objects.BaseObject;

public abstract class XObjectConverter<T> extends ClassDefConverter<BaseObject, T> {

  @Requirement(XObjectFieldAccessor.NAME)
  private FieldAccessor<BaseObject> fieldConverter;

  @Override
  public FieldAccessor<BaseObject> getFromFieldConverter() {
    return fieldConverter;
  }

}
