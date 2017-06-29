package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.Immutable;

import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeRole;
import com.google.common.base.Optional;
import com.xpn.xwiki.web.Utils;

@Immutable
public final class PageTypeMarshaller extends AbstractMarshaller<PageTypeReference> {

  public PageTypeMarshaller() {
    super(PageTypeReference.class);
  }

  @Override
  public String serialize(PageTypeReference val) {
    return checkNotNull(val.getConfigName());
  }

  @Override
  public Optional<PageTypeReference> resolve(String val) {
    checkNotNull(val);
    PageTypeReference pageTypeRef = getPageTypeService().getPageTypeRefByConfigName(val);
    return Optional.fromNullable(pageTypeRef);
  }

  protected static IPageTypeRole getPageTypeService() {
    return Utils.getComponent(IPageTypeRole.class);
  }

}
