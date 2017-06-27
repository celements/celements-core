package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Optional;
import com.xpn.xwiki.user.api.XWikiUser;

public class XWikiUserMarshaller extends AbstractMarshaller<XWikiUser> {

  public XWikiUserMarshaller() {
    super(XWikiUser.class);
  }

  @Override
  public Object serialize(XWikiUser val) {
    return checkNotNull(val.getUser());
  }

  @Override
  public Optional<XWikiUser> resolve(Object val) {
    if (!val.toString().isEmpty()) {
      return Optional.of(new XWikiUser(val.toString()));
    } else {
      return Optional.absent();
    }
  }

}
