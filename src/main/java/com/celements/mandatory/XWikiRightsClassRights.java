package com.celements.mandatory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.reference.RefBuilder;
import com.xpn.xwiki.XWikiConstant;

public class XWikiRightsClassRights extends AbstractXWikiClassRights {

  private static final Logger LOGGER = LoggerFactory.getLogger(XWikiRightsClassRights.class);

  @Override
  public String getName() {
    return "XWikiRightsClassRights";
  }

  @Override
  protected DocumentReference getDocRef() {
    return new RefBuilder().with(modelContext.getWikiRef()).space(XWikiConstant.XWIKI_SPACE)
        .doc("XWikiRights").build(DocumentReference.class);
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

}
