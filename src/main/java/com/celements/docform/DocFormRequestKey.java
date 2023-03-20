package com.celements.docform;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Strings.*;

import java.util.Comparator;
import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

@Immutable
public class DocFormRequestKey implements Comparable<DocFormRequestKey> {

  public enum Type {
    DOC_FIELD, OBJ_FIELD, OBJ_REMOVE;
  }

  private final String keyString;
  private final Type type;
  private final DocumentReference docRef;
  private final ClassReference classRef;
  private final int objNb;
  private final boolean remove;
  private final String fieldName;

  public static DocFormRequestKey createDocFieldKey(String key,
      DocumentReference docRef, String fieldName) {
    checkArgument(emptyToNull(fieldName) != null, key);
    return new DocFormRequestKey(key, Type.DOC_FIELD, docRef, null, 0, false, fieldName);
  }

  public static DocFormRequestKey createObjFieldKey(String key,
      DocumentReference docRef, ClassReference classRef, Integer objNb, String fieldName) {
    checkArgument(classRef != null, key);
    checkArgument(objNb != null, key);
    checkArgument(emptyToNull(fieldName) != null, key);
    return new DocFormRequestKey(key, Type.OBJ_FIELD, docRef, classRef, objNb,
        false, fieldName);
  }

  public static DocFormRequestKey createObjRemoveKey(String key,
      DocumentReference docRef, ClassReference classRef, Integer objNb) {
    checkArgument(classRef != null, key);
    checkArgument(objNb != null, key);
    checkArgument(objNb >= 0, key);
    return new DocFormRequestKey(key, Type.OBJ_REMOVE, docRef, classRef,
        objNb, true, null);
  }

  private DocFormRequestKey(String key, Type type, DocumentReference docRef,
      ClassReference classRef, int objNb, boolean remove, String fieldName) {
    this.keyString = checkNotNull(emptyToNull(key));
    this.type = checkNotNull(type);
    this.docRef = checkNotNull(docRef);
    this.classRef = classRef;
    this.objNb = objNb;
    this.remove = remove;
    this.fieldName = nullToEmpty(fieldName);
  }

  public String getKeyString() {
    return keyString;
  }

  public Type getType() {
    return type;
  }

  public DocumentReference getDocRef() {
    return docRef;
  }

  public ClassReference getClassRef() {
    return classRef;
  }

  public int getObjNb() {
    return objNb;
  }

  public int getObjHash() {
    return Objects.hash(docRef, classRef, objNb);
  }

  public boolean isRemove() {
    return remove;
  }

  public String getFieldName() {
    return fieldName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(docRef, classRef, objNb, (remove ? remove : fieldName));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof DocFormRequestKey) {
      DocFormRequestKey that = (DocFormRequestKey) obj;
      return Objects.equals(this.docRef, that.docRef)
          && Objects.equals(this.classRef, that.classRef)
          && Objects.equals(this.objNb, that.objNb)
          && Objects.equals(this.remove, that.remove)
          && Objects.equals(this.fieldName, that.fieldName);
    }
    return false;
  }

  @Override
  public int compareTo(DocFormRequestKey that) {
    return ComparisonChain.start()
        // primarily sorted by doc
        .compare(this.docRef, that.docRef)
        // secondary by class, but object fields come before document fields (classRef == null)
        .compare(this.classRef, that.classRef, Ordering.natural().nullsLast())
        // positive numbers first sorted asc, then negativ desc
        .compare(this.objNb, that.objNb, new ObjNbComparator())
        // remove come last
        .compareFalseFirst(this.remove, that.remove)
        .compare(this.fieldName, that.fieldName)
        .result();
  }

  /**
   * sorts: [0, 1, 2, ..., -1, -2, ...]
   */
  private class ObjNbComparator implements Comparator<Integer> {

    @Override
    public int compare(Integer i1, Integer i2) {
      if ((i1 < 0) && (i2 < 0)) {
        return i2.compareTo(i1);
      } else if (i1 < 0) {
        return 1;
      } else if (i2 < 0) {
        return -1;
      } else {
        return i1.compareTo(i2);
      }
    }
  }

  @Override
  public String toString() {
    return "DocFormRequestKey [keyString=" + keyString + ", type=" + type
        + ", docRef=" + docRef + ", classRef=" + classRef + ", objNb=" + objNb
        + ", remove=" + remove + ", fieldName=" + fieldName + "]";
  }

}
