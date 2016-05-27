package com.celements.menu.access;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiException;

@ComponentRole
public interface IMenuAccessProviderRole {

  public boolean hasview(DocumentReference menuBarDocRef) throws NoAccessDefinedException,
      XWikiException;

  public boolean denyView(DocumentReference menuBarDocRef);

}
