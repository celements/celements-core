package com.celements.common.classes;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.classes.BaseClass;

public abstract class ClassDefinition {

  private final DocumentReference classRef;
  private final boolean internalMapping;

  public ClassDefinition(DocumentReference classRef, boolean internalMapping) {
    this.classRef = new DocumentReference(classRef);
    this.internalMapping = internalMapping;
  }

  public DocumentReference getClassRef() {
    return new DocumentReference(classRef);
  }

  public boolean isInternalMapping() {
    return internalMapping;
  }

  /**
   * is called in {@link IClassCreatorRole#createClasses()} and used to customise and define the
   * provided base class
   *
   * @param bClass
   * @return true if update/save is needed
   */
  public abstract boolean defineProperties(BaseClass bClass);

}
