package com.celements.mandatory;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiException;

@ComponentRole
public interface IMandatoryDocumentRole {

  public void checkDocuments() throws XWikiException;

}
