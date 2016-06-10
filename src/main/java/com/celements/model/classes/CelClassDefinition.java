package com.celements.model.classes;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.CelObjectField;

@ComponentRole
public interface CelClassDefinition {

  public String getName();

  public DocumentReference getClassRef();

  public boolean isInternalMapping();

  public abstract List<CelObjectField<?>> getFields();

}
