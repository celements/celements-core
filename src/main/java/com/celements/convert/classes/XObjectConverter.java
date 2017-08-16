package com.celements.convert.classes;

import org.xwiki.component.annotation.Requirement;

import com.celements.model.field.FieldAccessor;
import com.celements.model.field.XObjectFieldAccessor;
import com.xpn.xwiki.objects.BaseObject;

public abstract class XObjectConverter<T> extends ClassDefConverter<BaseObject, T> {

  @Requirement(XObjectFieldAccessor.NAME)
  private FieldAccessor<BaseObject> xObjAccessor;

  @Override
  public FieldAccessor<BaseObject> getFromFieldAccessor() {
    return xObjAccessor;
  }

}
