package com.celements.observation.save.object;

import static com.celements.common.MoreObjectsCel.*;

import java.util.Objects;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.ClassIdentity;
import com.xpn.xwiki.objects.BaseObject;

class ObjectMeta {

  final ClassIdentity classRef;
  final int objNb;

  private ObjectMeta(ClassIdentity classRef, int objNb) {
    this.classRef = classRef;
    this.objNb = objNb;
  }

  @Override
  public int hashCode() {
    return Objects.hash(classRef, objNb);
  }

  @Override
  public boolean equals(Object obj) {
    return tryCast(obj, ObjectMeta.class)
        .map(other -> Objects.equals(this.classRef, other.classRef)
            && Objects.equals(this.objNb, other.objNb))
        .orElse(false);
  }

  public static ObjectMeta from(BaseObject xObj) {
    return new ObjectMeta(new ClassReference(xObj.getXClassReference()), xObj.getNumber());
  }

  @Override
  public String toString() {
    return "ObjectMeta [classRef=" + classRef + ", objNb=" + objNb + "]";
  }
}
