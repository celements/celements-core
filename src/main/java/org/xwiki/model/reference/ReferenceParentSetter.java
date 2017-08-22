package org.xwiki.model.reference;

import static com.celements.model.util.References.*;
import static com.google.common.base.Preconditions.*;

import java.lang.reflect.Field;

import javax.validation.constraints.NotNull;

public class ReferenceParentSetter {

  /**
   * Sets the parent for any mutable reference via reflection. This is required because
   * {@link EntityReference#setParent(EntityReference)} calls
   * {@link EntityReference#setChild(EntityReference)} on the parent and would consequently fail for
   * immutable parents.
   */
  public static void set(@NotNull EntityReference reference, @NotNull EntityReference parent) {
    checkArgument(!(reference instanceof ImmutableReference),
        "unable to set parent for an immutable reference");
    setWithoutMutabilityCheck(reference, parent);
  }

  /**
   * IMPORTANT: only use in setters of {@link ImmutableReference}. Like
   * {@link #set(EntityReference, EntityReference)} but without mutability check.
   */
  static void setWithoutMutabilityCheck(@NotNull EntityReference reference,
      @NotNull EntityReference parent) {
    try {
      Field parentField = EntityReference.class.getDeclaredField("parent");
      parentField.setAccessible(true);
      parentField.set(reference, getParentWithChild(parent, reference));
    } catch (ReflectiveOperationException exc) {
      throw new RuntimeException("Failed to set parent", exc);
    }
  }

  private static EntityReference getParentWithChild(EntityReference parent, EntityReference child) {
    // clone parent to make sure it's mutable and to not change the input
    if (parent instanceof ImmutableReference) {
      parent = ((ImmutableReference) parent).getMutable();
    } else {
      parent = parent.clone();
    }
    parent.setChild(child);
    return cloneRef(parent); // return cloned/immutable parent
  }

}
