/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.navigation;

import static com.google.common.base.Preconditions.*;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.util.ModelUtils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.xpn.xwiki.web.Utils;

/**
 * Immutable TreeNode with optional lazy getter for PartName
 * needed as performance improvement for legacy NotMappedMenuItems
 *
 * @author Fabian Pichler, Marc Sladek
 */
@Immutable
public final class TreeNode {

  private final EntityReference parentRef;

  private final Integer position;

  private final DocumentReference docRef;

  /**
   * The partName is only once set to a non-null value. This can be directly in the constructor or
   * if a PartNameGetter strategy is provided and NO partName then the strategy is lazily evaluated
   * on the first getPartName call.
   */
  private volatile String partName;

  private final PartNameGetter partNameStrategy;

  public TreeNode(@NotNull DocumentReference docRef, @Nullable EntityReference parentRef,
      @Nullable Integer position, @Nullable String partName) {
    this(docRef, parentRef, position, partName, null);
  }

  public TreeNode(@NotNull DocumentReference docRef, @Nullable EntityReference parentRef,
      @Nullable Integer position, @NotNull PartNameGetter strategy) {
    this(docRef, parentRef, position, null, strategy);
  }

  public TreeNode(@NotNull DocumentReference docRef, @Nullable EntityReference parentRef,
      @Nullable Integer position) {
    this(docRef, parentRef, position, (String) null);
  }

  private TreeNode(@NotNull DocumentReference docRef, @Nullable EntityReference parentRef,
      @Nullable Integer position, @Nullable String partName, @Nullable PartNameGetter strategy) {
    checkNotNull(docRef, "Document reference for TreeNode may not be null.");
    this.docRef = getModelUtils().cloneRef(docRef, DocumentReference.class);
    this.parentRef = (parentRef != null) ? getModelUtils().cloneRef(parentRef) : null;
    this.position = MoreObjects.firstNonNull(position, new Integer(0));
    this.partName = (strategy == null) ? Strings.nullToEmpty(partName) : partName;
    this.partNameStrategy = strategy;
  }

  public boolean isEmptyParentRef() {
    return (parentRef == null);
  }

  /**
   * @return fullName for documentReference backing TreeNode
   * @deprecated since 2.14.0 use getDocumentReference instead
   *             2016.09.20, FP: CAUTION: this method is still used in velocity scripts.
   *             e.g. LinkPicker.vm
   */
  @Deprecated
  @NotNull
  public String getFullName() {
    return getModelUtils().serializeRefLocal(docRef);
  }

  /**
   * @deprecated since 2.81 use getParentRef instead
   *             2016.09.20, FP: CAUTION: this method may be still used in velocity scripts.
   */
  @Deprecated
  @NotNull
  public String getParent() {
    return getModelUtils().serializeRefLocal(getParentRef());
  }

  @NotNull
  public EntityReference getParentRef() {
    EntityReference ref;
    if (isEmptyParentRef()) {
      ref = getDocumentReference().getLastSpaceReference();
    } else {
      ref = parentRef;
    }
    return getModelUtils().cloneRef(ref);
  }

  @NotNull
  public Integer getPosition() {
    return position;
  }

  @NotNull
  public String getPartName() {
    if ((partName == null) && (partNameStrategy != null)) {
      synchronized (this) {
        if (partName == null) {
          this.partName = Strings.nullToEmpty(partNameStrategy.getPartName(docRef));
        }
      }
    }
    return partName;
  }

  @NotNull
  public DocumentReference getDocumentReference() {
    return docRef;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof TreeNode) { // null check included
      TreeNode node = (TreeNode) obj;
      return Objects.equals(docRef, node.docRef) && Objects.equals(position, node.position);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(docRef, position);
  }

  @Override
  @NotNull
  public String toString() {
    return new StringBuilder().append("TreeNode [docRef=").append(docRef).append(
        ", parentRef=").append(parentRef).append(", position=").append(position).append(
            ", partName=").append(partName).append("]").toString();
  }

  private static ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
