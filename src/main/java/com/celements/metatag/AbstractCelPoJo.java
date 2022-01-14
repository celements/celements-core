package com.celements.metatag;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

public abstract class AbstractCelPoJo {

  private Long id;
  private DocumentReference documentReference;
  private Integer objNum;
  private ClassReference classReference;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public DocumentReference getDocumentReference() {
    return documentReference;
  }

  public void setDocumentReference(DocumentReference documentReference) {
    this.documentReference = documentReference;
  }

  public Integer getNumber() {
    return objNum;
  }

  public void setNumber(Integer number) {
    this.objNum = number;
  }

  public ClassReference getClassReference() {
    return classReference;
  }

  public void setClassReference(ClassReference classReference) {
    this.classReference = classReference;
  }

}
