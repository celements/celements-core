package com.celements.menu;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface IMenuService {

  public List<BaseObject> getMenuHeaders();

}
