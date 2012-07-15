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

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;

public class TreeNode {

  private String parent;
  
  private Integer position;

  private String partName;

  private IPartNameGetStrategy partNameGetStrategy;

  private DocumentReference docRef;

  private String databaseName;
  
  @Deprecated
  public TreeNode(String fullName, String parent, Integer position, String databaseName) {
    this.databaseName = databaseName;
    setFullName(fullName);
    setParent(parent);
    setPosition(position);
  }

  public TreeNode(DocumentReference docRef, String parent, Integer position) {
    setDocumentReference(docRef);
    setParent(parent);
    setPosition(position);
  }

  /**
   * 
   * @return fullName
   * 
   * @deprecated since 2.14.0 use getDocumentReference instead
   */
  @Deprecated
  public String getFullName() {
    return docRef.getLastSpaceReference().getName() + "." + docRef.getName();
  }

  void setDocumentReference(DocumentReference docRef) {
    this.docRef = docRef;
  }

  @Deprecated
  void setFullName(String fullName) {
    setDocumentReference(new DocumentReference(databaseName, fullName.split("\\.")[0],
        fullName.split("\\.")[1]));
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

}
