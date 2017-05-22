package com.celements.marshalling;

import com.xpn.xwiki.user.api.XWikiUser;

public class XWikiUserMarshaller extends AbstractMarshaller<XWikiUser> {

  public XWikiUserMarshaller() {
    super(XWikiUser.class);
  }

  @Override
  public Object serialize(XWikiUser val) {
    return val.getUser();
  }

  @Override
  public XWikiUser resolve(Object val) {
    return new XWikiUser(val.toString());
  }

}
