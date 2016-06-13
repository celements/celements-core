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

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class TreeNode {

  private String parent;

  private Integer position;

  private String partName;

  private IPartNameGetStrategy partNameGetStrategy;

  private final DocumentReference docRef;

  @Deprecated
  public TreeNode(@NotNull String fullName, String parent, Integer position,
      @NotNull String databaseName) {
    this(new DocumentReference(databaseName, fullName.split("\\.")[0], fullName.split("\\.")[1]),
        parent, position);
  }

  public TreeNode(@NotNull DocumentReference docRef, String parent, Integer position) {
    Preconditions.checkNotNull(docRef, "document reference for TreeNode may not be null.");
    this.docRef = docRef;
    setParent(parent);
    setPosition(position);
  }

  public TreeNode(@NotNull DocumentReference docRef, DocumentReference parentRef,
      Integer position) {
    this(docRef, getWebUtilsService().getRefLocalSerializer().serialize(parentRef), position);
  }

  public TreeNode(@NotNull DocumentReference docRef, SpaceReference parentRef, String partName,
      Integer position) {
    this(docRef, "", position);
    if (!StringUtils.isEmpty(partName)) {
      setPartName(partName);
    }
  }

  /**
   * @return fullName
   * @deprecated since 2.14.0 use getDocumentReference instead
   */
  @Deprecated
  public String getFullName() {
    return docRef.getLastSpaceReference().getName() + "." + docRef.getName();
  }

  public EntityReference getParentRef() {
    if ("".equals(parent)) {
      return docRef.getLastSpaceReference();
    } else {
      return getWebUtilsService().resolveDocumentReference(parent,
          (WikiReference) docRef.getLastSpaceReference().getParent());
    }
  }

  public String getParent() {
    return parent;
  }

  void setParent(String parent) {
    if (parent == null) {
      parent = "";
    }
    this.parent = parent;
  }

  public Integer getPosition() {
    if (position == null) {
      position = 0;
    }
    return position;
  }

  void setPosition(Integer position) {
    this.position = position;
  }

  public String getPartName(XWikiContext context) {
    if (partName == null) {
      if (partNameGetStrategy != null) {
        partName = partNameGetStrategy.getPartName(getFullName(), context);
      } else {
        partName = "";
      }
    }
    return partName;
  }

  public void setPartName(String partName) {
    if (partName == null) {
      partName = "";
    }
    this.partName = partName;
  }

  public void setPartNameGetStrategy(IPartNameGetStrategy strategy) {
    this.partNameGetStrategy = strategy;
  }

  public DocumentReference getDocumentReference() {
    return docRef;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TreeNode)) { // null check included
      return false;
    }
    // object must be Test at this point
    TreeNode node = (TreeNode) obj;
    return Objects.equal(docRef, node.docRef) && Objects.equal(position, node.position);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = (31 * hash) + (position == null ? 0 : position.hashCode());
    hash = (31 * hash) + (docRef == null ? 0 : docRef.hashCode());
    return hash;
  }

  @Override
  public String toString() {
    return "[ docRef = [" + docRef + "], parent = [" + parent + "], position = [" + position
        + "], partName = [" + getPartName(getContext()) + "] ]";
  }

  private XWikiContext getContext() {
    return (XWikiContext) getExecutionContext().getProperty("xwikicontext");
  }

  private ExecutionContext getExecutionContext() {
    return Utils.getComponent(Execution.class).getContext();
  }

  private static IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

}
