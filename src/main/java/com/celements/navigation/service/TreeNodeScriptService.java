package com.celements.navigation.service;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.script.service.ScriptService;

import com.celements.navigation.NavigationApi;
import com.celements.navigation.TreeNode;
import com.xpn.xwiki.XWikiContext;

@Component("treeNode")
public class TreeNodeScriptService implements ScriptService {

  @Requirement
  ITreeNodeService treeNodeService;

  @Requirement
  ITreeNodeCache treeNodeCache;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public NavigationApi createNavigation() {
    return NavigationApi.createNavigation(getContext());
  }

  public void enableMappedMenuItems() {
    treeNodeService.enableMappedMenuItems();
  }

  public int getMaxConfiguredNavigationLevel() {
    return treeNodeService.getMaxConfiguredNavigationLevel();
  }

  public boolean isTreeNode(DocumentReference docRef) {
    return treeNodeService.isTreeNode(docRef);
  }

  public boolean isNavigationEnabled(String configName) {
    NavigationApi nav = NavigationApi.createNavigation(getContext());
    nav.loadConfigByName(configName);
    return nav.isNavigationEnabled();
  }

  public NavigationApi getNavigation(String configName) {
    NavigationApi nav = NavigationApi.createNavigation(getContext());
    nav.loadConfigByName(configName);
    return nav;
  }

  public String includeNavigation(String configName) {
    return getNavigation(configName).includeNavigation();
  }

  public List<TreeNode> getSubNodesForParentRef(EntityReference parentRef) {
    return treeNodeService.getSubNodesForParent(parentRef, "");
  }

  public List<TreeNode> getSubNodesForParent(EntityReference parentRef, String menuPart) {
    return treeNodeService.getSubNodesForParent(parentRef, menuPart);
  }

  public int queryCount() {
    return treeNodeCache.queryCount();
  }

}
