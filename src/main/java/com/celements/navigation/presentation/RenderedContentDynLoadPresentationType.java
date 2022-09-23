package com.celements.navigation.presentation;

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
    outStream.append("<cel-lazy-load ");
    outStream.append(nav.addCssClasses(docRef, true, isFirstItem, isLastItem, isLeaf, numItem)
        + " ");
    String loadSrc = urlSrv.getExternalURL(docRef, "view",
        "xpage=ajax&ajax_mode=pageTypeWithLayout");
    outStream.append("src=\"" + loadSrc + "\" size=32 ");
    outStream.append(nav.addUniqueElementId(docRef) + ">\n");
    outStream.append("</cel-lazy-load>\n");
  }

}
