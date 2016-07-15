package com.celements.navigation.factories;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.NavigationConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.service.IPageTypeRole;
import com.celements.pagetype.xobject.XObjectPageTypeUtilsRole;
import com.xpn.xwiki.XWikiContext;

@Component(PageTypeNavigationFactory.PAGETYPE_NAV_FACTORY_HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
@Singleton
public class PageTypeNavigationFactory extends AbstractNavigationFactory<DocumentReference> {

  public static final String PAGETYPE_NAV_FACTORY_HINT = "pageType";

  @Requirement(XObjectNavigationFactory.XOBJECT_NAV_FACTORY_HINT)
  NavigationFactory<DocumentReference> xobjNavFactory;

  @Requirement
  IPageTypeResolverRole pageTypeResolver;

  @Requirement
  IPageTypeRole pageTypeService;

  @Requirement
  XObjectPageTypeUtilsRole xobjPageTypeUtils;

  @Requirement
  private Execution execution;

  XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  protected DocumentReference getDefaultConfigReference() {
    return getContext().getDoc().getDocumentReference();
  }

  @Override
  public NavigationConfig getNavigationConfig(DocumentReference docRef) {
    DocumentReference pageTypeDocRef = getPageTypeDocRef(docRef);
    return xobjNavFactory.getNavigationConfig(pageTypeDocRef);
  }

  @Override
  public boolean hasNavigationConfig(DocumentReference docRef) {
    DocumentReference pageTypeDocRef = getPageTypeDocRef(docRef);
    return xobjNavFactory.hasNavigationConfig(pageTypeDocRef);
  }

  private DocumentReference getPageTypeDocRef(DocumentReference configReference) {
    PageTypeReference pageTypeRef = getPageTypeRefForConfigReference(configReference);
    return xobjPageTypeUtils.getDocRefForPageType(pageTypeRef);
  }

  private PageTypeReference getPageTypeRefForConfigReference(DocumentReference configReference) {
    if (getDefaultConfigReference().equals(configReference)) {
      return pageTypeResolver.getPageTypeRefForCurrentDoc();
    } else {
      return pageTypeResolver.getPageTypeRefForDocWithDefault(configReference);
    }
  }

}
