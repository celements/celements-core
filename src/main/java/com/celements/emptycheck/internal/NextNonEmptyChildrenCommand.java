package com.celements.emptycheck.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.emptycheck.service.IEmptyCheckRole;
import com.celements.navigation.TreeNode;
import com.celements.navigation.service.ITreeNodeService;
import com.xpn.xwiki.web.Utils;

public class NextNonEmptyChildrenCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      NextNonEmptyChildrenCommand.class);

  private Set<DocumentReference> visitedDocRefs;

  public DocumentReference getNextNonEmptyChildren(DocumentReference documentRef) {
    if (getEmptyCheckService().isEmptyRTEDocument(documentRef)) {
      List<TreeNode> children = getTreeNodeService().getSubNodesForParent(documentRef, "");
      for (TreeNode childNode : children) {
        getVisitedDocRefs().add(documentRef);
        DocumentReference nextChild = childNode.getDocumentReference();
        if (!getVisitedDocRefs().contains(nextChild)) {
          DocumentReference result = getNextNonEmptyChildren(nextChild);
          if (result != null) {
            return result;
          }
        } else {
          LOGGER.warn("getNextNonEmptyChildren_internal: recursion detected on [" + nextChild
              + "].");
        }
      }
      return null;
    }
    return documentRef;
  }

  Set<DocumentReference> getVisitedDocRefs() {
    if (visitedDocRefs == null) {
      visitedDocRefs = new HashSet<>();
    }
    return visitedDocRefs;
  }

  ITreeNodeService getTreeNodeService() {
    return Utils.getComponent(ITreeNodeService.class);
  }

  IEmptyCheckRole getEmptyCheckService() {
    return Utils.getComponent(IEmptyCheckRole.class);
  }

}
