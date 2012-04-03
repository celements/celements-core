package com.celements.menu;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface IMenuService {

  public List<BaseObject> getMenuHeaders();

  public List<BaseObject> getSubMenuItems(Integer headerId);

  public DocumentReference getMenuBarHeaderClassRef();

  public DocumentReference getMenuBarHeaderClassRef(String database);

  public DocumentReference getMenuBarSubItemClassRef();

  public DocumentReference getMenuBarSubItemClassRef(String database);

}
