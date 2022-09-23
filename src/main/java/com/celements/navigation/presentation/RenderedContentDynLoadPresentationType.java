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
  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem, INavigation nav) {
    LOGGER.debug("writeNodeContent for [{}].", docRef);
    outStream.append("<div ");
    outStream.append(nav.addCssClasses(docRef, true, isFirstItem, isLastItem, isLeaf, numItem)
        + " ");
    outStream.append(nav.addUniqueElementId(docRef) + ">\n");
    outStream.append("<cel-lazy-load ");
    String loadSrc = getLoadSrcUrl(docRef);
    outStream.append("src=\"" + loadSrc + "\" size=32 ");
    outStream.append("</cel-lazy-load>\n");
    outStream.append("</div>\n");
  }

  private @NotNull String getLoadSrcUrl(DocumentReference docRef) {
    return urlSrv.getURL(docRef, "view",
        "xpage=ajax&ajax_mode=pageTypeWithLayout&ajax=1&overwriteLayout=SimpleLayout");
  }

}
