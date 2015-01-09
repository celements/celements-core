package com.celements.docform;

import org.apache.commons.lang.ObjectUtils;
import org.xwiki.model.reference.DocumentReference;

public class DocFormRequestKey {

  private final String keyString;
  private final DocumentReference docRef;
  private final DocumentReference classRef;
  private final boolean remove;
  private final Integer objNb;
  private final String fieldName;
  
  public DocFormRequestKey(String key, DocumentReference docRef, 
      DocumentReference classRef, boolean remove, Integer objNb, String fieldName) {
    this.keyString = key;
    this.docRef = docRef;
    this.classRef = classRef;
    this.remove = remove;
    this.objNb = objNb;
    this.fieldName = fieldName;
  }

  public String getKeyString() {
    return keyString;
  }

  public DocumentReference getDocRef() {
    return docRef;
  }

  public DocumentReference getClassRef() {
    return classRef;
  }

  public boolean isRemove() {
    return remove;
  }

  public Integer getObjNb() {
    return objNb;
  }

  public String getFieldName() {
    return fieldName;
  }

  public boolean sameObject(DocFormRequestKey other) {
    System.out.println("compare " + this);
    System.out.println("with " + other);
    return (this.classRef != null) && (this.objNb != null) && (this.objNb >= 0)
        && ObjectUtils.equals(this.classRef, other.classRef) 
        && ObjectUtils.equals(this.objNb, other.objNb);
  }

  @Override
  public String toString() {
    return "DocFormRequestKey [keyString=" + keyString + ", docRef=" + docRef 
        + ", classRef=" + classRef + ", remove=" + remove + ", objNb=" + objNb 
        + ", fieldName=" + fieldName + "]";
  }

}
