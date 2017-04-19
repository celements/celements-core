package com.celements.convert.classes;

import org.xwiki.component.annotation.Requirement;

import com.celements.model.access.field.FieldAccessor;
import com.celements.model.access.field.XObjectFieldAccessor;
import com.xpn.xwiki.objects.BaseObject;

public abstract class XObjectConverter<T> extends ClassDefConverter<BaseObject, T> {

  @Requirement(XObjectFieldAccessor.NAME)
  private FieldAccessor<BaseObject> accessor;

  @Override
  public FieldAccessor<BaseObject> getFromFieldAccessor() {
    return accessor;
  }

}
