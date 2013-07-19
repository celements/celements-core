package com.celements.web.plugin.cmd;

import com.xpn.xwiki.web.Utils;

public class CheckMandatoryDocuments {

  public void checkMandatoryDocuments() {
    IMandatoryDocumentComponentRole mandatoryDocCmp = Utils.getComponent(
        IMandatoryDocumentComponentRole.class);
    if(mandatoryDocCmp != null) {
      mandatoryDocCmp.checkAllMandatoryDocuments();
    }
  }

}
