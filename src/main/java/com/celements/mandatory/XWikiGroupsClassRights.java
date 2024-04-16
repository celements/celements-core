package com.celements.mandatory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.reference.RefBuilder;
import com.xpn.xwiki.XWikiConstant;

public class XWikiGroupsClassRights extends AbstractXWikiClassRights {

  private static final Logger LOGGER = LoggerFactory.getLogger(XWikiGroupsClassRights.class);

  @Override
  public String getName() {
    return "XWikiGroupsClassRights";
  }

  @Override
  protected DocumentReference getDocRef() {
    return new RefBuilder().with(modelContext.getWikiRef()).space(XWikiConstant.XWIKI_SPACE)
        .doc("XWikiGroups").build(DocumentReference.class);
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

}
