package com.celements.model.access.object.restriction;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;

import com.celements.model.access.object.ObjectBridge;

@Immutable
public class ClassRestriction<O> extends ObjectRestriction<O> {

  private final ClassReference ref;

  public ClassRestriction(@NotNull ObjectBridge<?, O> bridge, @NotNull ClassReference ref) {
    super(bridge);
    this.ref = checkNotNull(ref);
  }

  @Override
  public boolean apply(@NotNull O obj) {
    return ref.equals(getBridge().getObjectClassRef(obj));
  }

  public ClassReference getClassRef() {
    return ref;
  }

}
