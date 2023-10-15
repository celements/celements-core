package com.celements.navigation.presentation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.context.ModelContext;
import com.celements.navigation.INavigation;
import com.celements.web.service.UrlService;

@Component("renderedContentDynLoad")
public class RenderedContentDynLoadPresentationType extends RenderedContentPresentationType
    implements IPresentationTypeRole<INavigation> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RenderedContentDynLoadPresentationType.class);

  private final UrlService urlSrv;
  private final ModelContext mContext;

  private final Set<String> keyBlackList = Set.of("ajax", "xpage", "ajax_mode");

  @Inject
  public RenderedContentDynLoadPresentationType(UrlService urlSrv, ModelContext mContext) {
    this.urlSrv = urlSrv;
    this.mContext = mContext;
  }

  @Override
  protected void addRenderedContent(@NotNull StringBuilder outStream,
      @NotNull DocumentReference docRef) {
    outStream.append("<cel-lazy-load src=\"");
    outStream.append(getLoadSrcUrl(docRef));
    outStream.append("\" size=32 >");
    outStream.append("</cel-lazy-load>\n");
  }

  private @NotNull String getLoadSrcUrl(@NotNull DocumentReference docRef) {
    String loadSrcUrl = urlSrv.getURL(docRef, "view",
        "xpage=ajax&ajax_mode=rendering/renderDocumentWithPageType&ajax=1"
            + getPassThroughParams());
    LOGGER.debug("getLoadSrcUrl returning {}", loadSrcUrl);
    return loadSrcUrl;
  }

  private @NotNull String getPassThroughParams() {
    return mContext.request()
    .map(r -> r.getParameterMap().entrySet())
    .orElse(Set.of())
    .stream()
    .filter(entry1 -> !keyBlackList.contains(entry1.getKey()))
        .flatMap(entry -> List.of(entry.getValue()).stream()
            .map(v -> entry.getKey() + "=" + v))
        .collect(() -> new StringJoiner("&", "&", "").setEmptyValue(""), StringJoiner::add,
            StringJoiner::merge)
        .toString();
  }

}
