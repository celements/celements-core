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

import java.util.Objects;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Immutable TreeNode
 *
 * @author Fabian Pichler, Marc Sladek
 */
public final class TreeNode {

  private final EntityReference parentRef;

  private final Integer position;

  private final DocumentReference docRef;

  private final String partName;

  public TreeNode(@NotNull DocumentReference docRef, @Nullable EntityReference parentRef,
      @Nullable Integer position, @Nullable String partName) {
    Preconditions.checkNotNull(docRef, "Document reference for TreeNode may not be null.");
    this.docRef = new DocumentReference(docRef.clone());
    this.parentRef = (parentRef != null) ? parentRef.clone() : null;
    this.position = MoreObjects.firstNonNull(position, new Integer(0));
    this.partName = Strings.nullToEmpty(partName);
  }

  public TreeNode(@NotNull DocumentReference docRef, @Nullable EntityReference parentRef,
      @Nullable Integer position, @NotNull PartNameGetter strategy) {
    this(docRef, parentRef, position, strategy.getPartName(docRef));
  }

  public TreeNode(@NotNull DocumentReference docRef, @Nullable EntityReference parentRef,
      @Nullable Integer position) {
    this(docRef, parentRef, position, (String) null);
  }

  public boolean isEmptyParentRef() {
    return (parentRef == null);
  }

  public EntityReference getParentRef() {
    if (isEmptyParentRef()) {
      return new SpaceReference(getDocumentReference().getLastSpaceReference().clone());
    } else {
      return parentRef.clone();
    }
  }

  public Integer getPosition() {
    return position;
  }

  public String getPartName() {
    return partName;
  }

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
  public String toString() {
    return new StringBuilder().append("TreeNode [docRef=").append(docRef).append(
        ", parentRef=").append(parentRef).append(", position=").append(position).append(
            ", partName=").append(partName).append("]").toString();
  }

}
