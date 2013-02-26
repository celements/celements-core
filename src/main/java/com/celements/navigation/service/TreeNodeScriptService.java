package com.celements.navigation.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.QueryManager;
import org.xwiki.script.service.ScriptService;

import com.celements.appScript.IAppScriptService;
import com.celements.navigation.NavContextMenuApi;
import com.celements.navigation.NavigationApi;
import com.celements.navigation.TreeNode;
import com.celements.validation.IFormValidationRole;
import com.celements.web.service.CelementsWebScriptService;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

@Component("treeNode")
public class TreeNodeScriptService implements ScriptService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      TreeNodeScriptService.class);

  @Requirement("local")
  EntityReferenceSerializer<String> modelSerializer;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  ITreeNodeService treeNodeService;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public NavContextMenuApi getNavContextMenu() {
    return NavContextMenuApi.getNavContextMenu(getContext());
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

}
