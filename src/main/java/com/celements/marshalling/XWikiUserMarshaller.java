package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Optional;
import com.xpn.xwiki.user.api.XWikiUser;

@Immutable
public final class XWikiUserMarshaller extends AbstractMarshaller<XWikiUser> {

  public XWikiUserMarshaller() {
    super(XWikiUser.class);
  }

  @Override
  public String serialize(XWikiUser val) {
    return checkNotNull(val.getUser());
  }

  @Override
  public Optional<XWikiUser> resolve(String val) {
    if (!checkNotNull(val).isEmpty()) {
      return Optional.of(new XWikiUser(val));
    } else {
      return Optional.absent();
    }
  }

}
