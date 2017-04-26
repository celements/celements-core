package com.celements.convert.classes;

import org.xwiki.component.annotation.Requirement;

import com.celements.model.access.field.FieldAccessor;
import com.celements.model.access.field.XObjectFieldAccessor;
import com.xpn.xwiki.objects.BaseObject;

public abstract class XObjectDeconverter<T> extends ClassDefConverter<T, BaseObject> {

  @Requirement(XObjectFieldAccessor.NAME)
  private FieldAccessor<BaseObject> xObjAccessor;

  @Override
  public BaseObject createInstance() {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(getClassDef().getClassRef());
    return obj;
  }

  @Override
  public FieldAccessor<BaseObject> getToFieldAccessor() {
    return xObjAccessor;
  }

}
