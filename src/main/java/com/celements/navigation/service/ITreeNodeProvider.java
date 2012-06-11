package com.celements.navigation.service;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.navigation.TreeNode;

@ComponentRole
public interface ITreeNodeProvider {

  public List<TreeNode> getTreeNodesForParent(String parentKey);

}
