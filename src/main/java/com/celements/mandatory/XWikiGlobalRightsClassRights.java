package com.celements.mandatory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.reference.RefBuilder;
import com.xpn.xwiki.XWikiConstant;

public class XWikiGlobalRightsClassRights extends AbstractXWikiClassRights {

  private static final Logger LOGGER = LoggerFactory.getLogger(XWikiGlobalRightsClassRights.class);

  @Override
  public String getName() {
    return "XWikiGlobalRightsClassRights";
  }

  @Override
  protected DocumentReference getDocRef() {
    return new RefBuilder().with(modelContext.getWikiRef()).space(XWikiConstant.XWIKI_SPACE)
        .doc("XWikiGlobalRights").build(DocumentReference.class);
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

}
