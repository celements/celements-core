package com.celements.navigation.service;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.context.Execution;

import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.celements.navigation.cmd.GetNotMappedMenuItemsForParentCommand;
import com.xpn.xwiki.XWikiContext;

/**
 * TreeNodeCache must be a singleton to ensure efficient caching
 * 
 * @author Fabian Pichler
 *
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
public class TreeNodeCache implements ITreeNodeCache {

  private GetMappedMenuItemsForParentCommand injected_GetMappedMenuItemCommand;

  private GetNotMappedMenuItemsForParentCommand notMappedMenuItemCommand;

  @Requirement
  Execution execution;
  
  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#queryCount()
   */
  public int queryCount() {
    return getNotMappedMenuItemsForParentCmd().queryCount();
  }

  void inject_GetNotMappedMenuItemsForParentCmd(
      GetNotMappedMenuItemsForParentCommand testGetMenuItemCommand) {
    notMappedMenuItemCommand = testGetMenuItemCommand;
  }

  public GetNotMappedMenuItemsForParentCommand getNotMappedMenuItemsForParentCmd() {
    if (notMappedMenuItemCommand == null) {
      notMappedMenuItemCommand = new GetNotMappedMenuItemsForParentCommand();
    }
    return notMappedMenuItemCommand;
  }

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#flushMenuItemCache(com.xpn.xwiki.XWikiContext)
   */
  public void flushMenuItemCache() {
    getNotMappedMenuItemsForParentCmd().flushMenuItemCache(getContext());
  }

  void inject_GetMappedMenuItemsForParentCmd(
      GetMappedMenuItemsForParentCommand testGetMenuItemCommand) {
    injected_GetMappedMenuItemCommand = testGetMenuItemCommand;
  }

  public GetMappedMenuItemsForParentCommand getMappedMenuItemsForParentCmd() {
    if (injected_GetMappedMenuItemCommand != null) {
      return injected_GetMappedMenuItemCommand;
    }
    XWikiContext context = getContext();
    if (context.get(
        GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY) != null
        && context.get(GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY
            ) instanceof GetMappedMenuItemsForParentCommand) {
      return (GetMappedMenuItemsForParentCommand)context.get(
          GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY);
    }
    GetMappedMenuItemsForParentCommand cmd = new GetMappedMenuItemsForParentCommand();
    cmd.set_isActive(false);
    return cmd;
  }

}
