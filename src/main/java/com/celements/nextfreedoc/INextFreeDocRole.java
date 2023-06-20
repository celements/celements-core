package com.celements.nextfreedoc;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

@ComponentRole
public interface INextFreeDocRole {

  public static final String UNTITLED_NAME = "untitled";

  public DocumentReference getNextTitledPageDocRef(SpaceReference spaceRef, String title);

  public DocumentReference getNextUntitledPageDocRef(SpaceReference spaceRef);

  /**
   * Creates a DocumentReference that doesn't exist yet in the given space from a random
   * alphanumeric string and an optional prefix.
   *
   * @param spaceRef
   * @param lengthOfRandomAlphanumeric
   *          has to be > 3
   * @param prefix
   * @return the new DocumentReference
   */
  @NotNull
  public DocumentReference getNextRandomPageDocRef(@NotNull SpaceReference spaceRef,
      int lengthOfRandomAlphanumeric, @Nullable String prefix);
}
