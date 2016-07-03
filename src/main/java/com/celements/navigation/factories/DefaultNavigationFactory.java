package com.celements.navigation.factories;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.NavigationConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.xpn.xwiki.XWikiContext;

@Component
public class DefaultNavigationFactory extends AbstractNavigationFactory<DocumentReference> {

  @Requirement
  private ConfigurationSource configuration;

  @Requirement(XObjectNavigationFactory.XOBJECT_NAV_FACTORY_HINT)
  NavigationFactory<DocumentReference> xobjNavFactory;

  @Requirement(PageTypeNavigationFactory.PAGETYPE_NAV_FACTORY_HINT)
  NavigationFactory<DocumentReference> pageTypeNavFactory;

  @Requirement(JavaNavigationFactory.JAVA_NAV_FACTORY_HINT)
  NavigationFactory<PageTypeReference> javaNavFactory;

  @Requirement
  IPageTypeResolverRole pageTypeResolver;

  @Requirement
  private Execution execution;

  XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  @NotNull
  protected DocumentReference getDefaultConfigReference() {
    return getContext().getDoc().getDocumentReference();
  }

  private PageTypeReference getJavaConfigReference(DocumentReference configReference) {
    PageTypeReference pageTypeRef;
    if (getDefaultConfigReference().equals(configReference)) {
      pageTypeRef = pageTypeResolver.getPageTypeRefForCurrentDoc();
    } else {
      pageTypeRef = pageTypeResolver.getPageTypeRefForDocWithDefault(configReference);
    }
    return pageTypeRef;
  }

  @Override
  @NotNull
  public NavigationConfig getNavigationConfig(DocumentReference configReference) {
    NavigationConfig theNavConfig = NavigationConfig.DEFAULTS;
    PageTypeReference javaConfigReference = getJavaConfigReference(configReference);
    if (javaNavFactory.hasNavigationConfig(javaConfigReference)) {
      theNavConfig = theNavConfig.overlay(javaNavFactory.getNavigationConfig(javaConfigReference));
    }
    if (pageTypeNavFactory.hasNavigationConfig(configReference)) {
      theNavConfig = theNavConfig.overlay(pageTypeNavFactory.getNavigationConfig(configReference));
    }
    if (xobjNavFactory.hasNavigationConfig(configReference)) {
      theNavConfig = theNavConfig.overlay(xobjNavFactory.getNavigationConfig(configReference));
    }
    return theNavConfig;
  }

  @Override
  public boolean hasNavigationConfig(DocumentReference configReference) {
    PageTypeReference javaConfigReference = getJavaConfigReference(configReference);
    return (javaNavFactory.hasNavigationConfig(javaConfigReference)
        || pageTypeNavFactory.hasNavigationConfig(configReference)
        || xobjNavFactory.hasNavigationConfig(configReference));
  }

}
