package com.celements.navigation.presentation;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.INavigation;
import com.celements.web.service.UrlService;

@Component("renderedContentDynLoad")
public class RenderedContentDynLoadPresentationType extends RenderedContentPresentationType
    implements IPresentationTypeRole<INavigation> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RenderedContentDynLoadPresentationType.class);

  @Requirement
  private UrlService urlSrv;

  @Override
  protected void addRenderedContent(StringBuilder outStream, DocumentReference docRef) {
    outStream.append("<cel-lazy-load src=\"");
    outStream.append(getLoadSrcUrl(docRef));
    outStream.append("\" size=32 >");
    outStream.append("</cel-lazy-load>\n");
  }

  private @NotNull String getLoadSrcUrl(DocumentReference docRef) {
    return urlSrv.getURL(docRef, "view",
        "xpage=ajax&ajax_mode=rendering/renderDocumentWithPageType&ajax=1");
  }

}
