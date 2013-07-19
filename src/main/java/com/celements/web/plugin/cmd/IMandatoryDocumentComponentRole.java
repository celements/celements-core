package com.celements.web.plugin.cmd;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IMandatoryDocumentComponentRole {

  public void checkAllMandatoryDocuments();

}
