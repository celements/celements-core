package com.celements.pagetype.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeReference;

@Component(JavaPageTypeProvider.PROVIDER_HINT)
public class JavaPageTypeProvider implements IPageTypeProviderRole {

  public static final String PROVIDER_HINT = "com.celements.JavaPageTypeProvider";

  @Requirement
  Map<String, IJavaPageTypeRole> javaPageTypesMap;

  volatile Map<PageTypeReference, IJavaPageTypeRole> javaPageTypeRefsMap;

  @Override
  public List<PageTypeReference> getPageTypes() {
    return new ArrayList<PageTypeReference>(getPageTypeRefsMap().keySet());
  }

  private Map<PageTypeReference, IJavaPageTypeRole> getPageTypeRefsMap() {
    if (javaPageTypeRefsMap == null) {
      initilizeTypeRefsMap();
    }
    return javaPageTypeRefsMap;
  }

  synchronized void initilizeTypeRefsMap() {
    if (javaPageTypeRefsMap == null) {
      Map<PageTypeReference, IJavaPageTypeRole> theNewMap = new HashMap<>();
      for (IJavaPageTypeRole javaPageType : javaPageTypesMap.values()) {
        PageTypeReference thePageTypeRef = new PageTypeReference(javaPageType.getName(),
            PROVIDER_HINT, new ArrayList<String>(javaPageType.getCategoryNames()));
        theNewMap.put(thePageTypeRef, javaPageType);
      }
      javaPageTypeRefsMap = Collections.unmodifiableMap(theNewMap);
    }
  }

  @Override
  public IPageTypeConfig getPageTypeByReference(PageTypeReference pageTypeRef) {
    if (getPageTypeRefsMap().containsKey(pageTypeRef)) {
      return new DefaultPageTypeConfig(getPageTypeRefsMap().get(pageTypeRef));
    }
    return null;
  }

}
