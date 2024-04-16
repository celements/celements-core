package com.celements.mandatory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.reference.RefBuilder;
import com.xpn.xwiki.XWikiConstant;

@Component
public class TestXWikiClassRights extends AbstractXWikiClassRights {

  private final Logger logger = LoggerFactory.getLogger(TestXWikiClassRights.class);

  @Override
  public String getName() {
    return "TestXWikiClassRights";
  }

  @Override
  protected DocumentReference getDocRef() {
    return new RefBuilder().wiki(getWiki()).space(XWikiConstant.XWIKI_SPACE)
        .doc("TestXWikiClass").build(DocumentReference.class);
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

}
