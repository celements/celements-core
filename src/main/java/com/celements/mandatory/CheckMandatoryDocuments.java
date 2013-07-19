package com.celements.mandatory;

import com.xpn.xwiki.web.Utils;

public class CheckMandatoryDocuments {

  public void checkMandatoryDocuments() {
    IMandatoryDocumentCompositorRole mandatoryDocCmp = Utils.getComponent(
        IMandatoryDocumentCompositorRole.class);
    if(mandatoryDocCmp != null) {
      mandatoryDocCmp.checkAllMandatoryDocuments();
    }
  }

}
