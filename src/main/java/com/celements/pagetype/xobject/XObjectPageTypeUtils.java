package com.celements.pagetype.xobject;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.pagetype.PageTypeReference;
import com.celements.web.service.IWebUtilsService;

@Component
public class XObjectPageTypeUtils implements XObjectPageTypeUtilsRole {

  private static final String DEFAULT_PAGE_TYPES_SPACE = "PageTypes";

  @Requirement
  private IWebUtilsService webUtilsService;

  @Override
  @NotNull
  public DocumentReference getDocRefForPageType(@NotNull String configName) {
    DocumentReference pageTypeDocRef = new DocumentReference(configName, new SpaceReference(
        DEFAULT_PAGE_TYPES_SPACE, webUtilsService.getWikiRef()));
    return pageTypeDocRef;
  }

  @Override
  @NotNull
  public DocumentReference getDocRefForPageType(@NotNull PageTypeReference pageTypeRef) {
    return getDocRefForPageType(pageTypeRef.getConfigName());
  }

}
