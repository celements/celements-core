package com.celements.marshalling;

import static com.google.common.base.Preconditions.*;

import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeRole;
import com.google.common.base.Optional;
import com.xpn.xwiki.web.Utils;

public class PageTypeMarshaller extends AbstractMarshaller<PageTypeReference> {

  public PageTypeMarshaller() {
    super(PageTypeReference.class);
  }

  @Override
  public Object serialize(PageTypeReference val) {
    return checkNotNull(val.getConfigName());
  }

  @Override
  public Optional<PageTypeReference> resolve(Object val) {
    PageTypeReference pageTypeRef = getPageTypeService().getPageTypeRefByConfigName(val.toString());
    return Optional.fromNullable(pageTypeRef);
  }

  protected static IPageTypeRole getPageTypeService() {
    return Utils.getComponent(IPageTypeRole.class);
  }

}
