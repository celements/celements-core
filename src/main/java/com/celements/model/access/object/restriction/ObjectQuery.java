package com.celements.model.access.object.restriction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xwiki.model.reference.ClassReference;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

public class ObjectQuery<O> extends ArrayList<ObjectRestriction<O>> {

  public ObjectQuery() {
    super();
  }

  public ObjectQuery(Collection<? extends ObjectRestriction<O>> coll) {
    super(coll);
  }

  public List<ClassReference> getClassRefs() {
    return FluentIterable.from(this).filter(ClassRestriction.<O>getGenericClass()).transform(
        new Function<ClassRestriction<O>, ClassReference>() {

          @Override
          public ClassReference apply(ClassRestriction<O> restr) {
            return restr.getClassRef();
          }
        }).toList();
  }

  private static final long serialVersionUID = 1L;

}
