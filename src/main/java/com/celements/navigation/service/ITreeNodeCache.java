package com.celements.navigation.service;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.celements.navigation.cmd.GetNotMappedMenuItemsForParentCommand;

@ComponentRole
public interface ITreeNodeCache {

  public int queryCount();

  public void flushMenuItemCache();

  public GetNotMappedMenuItemsForParentCommand getNotMappedMenuItemsForParentCmd();

  public GetMappedMenuItemsForParentCommand getMappedMenuItemsForParentCmd();

}
