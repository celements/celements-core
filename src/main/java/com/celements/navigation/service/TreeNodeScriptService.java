package com.celements.navigation.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.script.service.ScriptService;

import com.celements.emptycheck.service.IEmptyCheckRole;
import com.celements.navigation.NavigationApi;
import com.celements.navigation.TreeNode;
import com.celements.navigation.cmd.ReorderSaveCommand;
import com.celements.navigation.factories.JavaNavigationFactory;
import com.celements.navigation.factories.NavigationFactory;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

@Component("treeNode")
public class TreeNodeScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TreeNodeScriptService.class);

  @Requirement
  ITreeNodeService treeNodeService;

  @Requirement
  ITreeNodeCache treeNodeCache;

  @Requirement
  NavigationFactory<DocumentReference> navFactory;

  @Requirement(JavaNavigationFactory.JAVA_NAV_FACTORY_HINT)
  NavigationFactory<PageTypeReference> javaNavFactory;

  @Requirement
  IPageTypeRole pageTypeService;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  /**
   * @deprecated since 2.80 instead use getDefaultNavigation() or getEmptyNavigation()
   */
  @Deprecated
  public NavigationApi createNavigation() {
    return NavigationApi.createNavigation(getContext());
  }

  public NavigationApi getEmptyNavigation() {
    return NavigationApi.createNavigation(getContext());
  }

  public NavigationApi getJavaNavigation(String pageTypeName) {
    PageTypeReference configReference = pageTypeService.getPageTypeRefByConfigName(pageTypeName);
    return NavigationApi.getAPIObject(javaNavFactory.createNavigation(configReference),
        getContext());
  }

  public NavigationApi getDefaultNavigation() {
    return NavigationApi.getAPIObject(navFactory.createNavigation(), getContext());
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

  public TreeNode getPrevMenuItem(DocumentReference docRef) {
    TreeNode node = null;
    if (docRef != null) {
      try {
        node = treeNodeService.getPrevMenuItem(docRef);
      } catch (XWikiException xwe) {
        LOGGER.error("getPrevMenuItem: failed for '{}'", docRef, xwe);
      }
    }
    return node;
  }

  public TreeNode getNextMenuItem(DocumentReference docRef) {
    TreeNode node = null;
    if (docRef != null) {
      try {
        node = treeNodeService.getNextMenuItem(docRef);
      } catch (XWikiException xwe) {
        LOGGER.error("getNextMenuItem: failed for '{}'", docRef, xwe);
      }
    }
    return node;
  }

  public int queryCount() {
    return treeNodeCache.queryCount();
  }

  public String navReorderSave(DocumentReference docRef, String structureJSON) {
    return new ReorderSaveCommand().reorderSave(
        getWebUtilsService().getRefDefaultSerializer().serialize(docRef), structureJSON,
        getContext());
  }

  public DocumentReference getNextNonEmptyChildren(DocumentReference documentRef) {
    return getEmptyCheckService().getNextNonEmptyChildren(documentRef);
  }

  private IEmptyCheckRole getEmptyCheckService() {
    return Utils.getComponent(IEmptyCheckRole.class);
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  public void moveTreeDocAfter(DocumentReference moveDocRef, DocumentReference insertAfterDocRef) {
    try {
      treeNodeService.moveTreeDocAfter(moveDocRef, insertAfterDocRef);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get moveDoc [" + moveDocRef + "]", exp);
    }
  }

}
