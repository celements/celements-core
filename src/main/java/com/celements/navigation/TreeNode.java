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

import org.apache.commons.lang.StringUtils;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.web.service.IWebUtilsService;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class TreeNode {

  private final String parent;

  private Integer position;

  private final DocumentReference docRef;

  private volatile String partName;

  private PartNameGetter partNameGetter;

  @Deprecated
  public TreeNode(@NotNull String fullName, String parent, Integer position,
      @NotNull String databaseName) {
    this(new DocumentReference(databaseName, fullName.split("\\.")[0], fullName.split("\\.")[1]),
        parent, position);
  }

  public TreeNode(@NotNull DocumentReference docRef, @Nullable String parent, Integer position) {
    Preconditions.checkNotNull(docRef, "Document reference for TreeNode may not be null.");
    this.docRef = new DocumentReference(docRef);
    this.parent = MoreObjects.firstNonNull(parent, "");
    this.position = MoreObjects.firstNonNull(position, new Integer(0));
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
    return getWebUtilsService().serializeRef(docRef, true);
  }

  public EntityReference getParentRef() {
    if ("".equals(parent)) {
      return docRef.getLastSpaceReference();
    } else {
      return getWebUtilsService().resolveDocumentReference(parent, getWebUtilsService().getWikiRef(
          docRef));
    }
  }

  public String getParent() {
    return parent;
  }

  public Integer getPosition() {
    return position;
  }

  @Deprecated
  public String getPartName(XWikiContext context) {
    return getPartName();
  }

  // this method should be thread safe, the method 'setPartNameInternal()' is synchronized and only
  // changes the reference of the volatile field 'partName'
  public String getPartName() {
    if (partName == null) {
      setPartNameInternal();
    }
    return partName;
  }

  private synchronized void setPartNameInternal() {
    if (partName == null) {
      String partName = "";
      if (partNameGetter != null) {
        partName = partNameGetter.getPartName(getDocumentReference());
      }
      setPartName(partName);
    }
  }

  public synchronized void setPartName(String partName) {
    if (partName == null) {
      partName = "";
    }
    this.partName = partName;
  }

  public synchronized void setPartNameGetter(PartNameGetter strategy) {
    partNameGetter = strategy;
  }

  /**
   * @deprecated since 1.140 instead use setPartNameGetter(PartNameGetter)
   */
  @Deprecated
  public void setPartNameGetStrategy(IPartNameGetStrategy strategy) {
    setPartNameGetter(new PartNameGetterWrapper(strategy));
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
    return "[ docRef = [" + docRef + "], parent = [" + parent + "], position = [" + position
        + "], partName = [" + getPartName() + "] ]";
  }

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  private static IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  @Deprecated
  private class PartNameGetterWrapper implements PartNameGetter {

    private IPartNameGetStrategy strategy;

    public PartNameGetterWrapper(IPartNameGetStrategy strategy) {
      this.strategy = strategy;
    }

    @Override
    public String getPartName(DocumentReference docRef) {
      return strategy.getPartName(docRef.getLastSpaceReference().getName() + "." + docRef.getName(),
          getContext());
    }

  }

}
