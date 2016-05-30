package com.celements.navigation.listener;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.cache.IDocumentReferenceCache;
import com.celements.navigation.NavigationCache;
import com.celements.navigation.event.NavigationCreatedEvent;
import com.celements.navigation.event.NavigationDeletedEvent;
import com.celements.navigation.event.NavigationUpdatedEvent;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(NavigationCache.NAME)
public class NavigationCacheFlushingListener implements EventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      NavigationCacheFlushingListener.class);

  @Requirement(NavigationCache.NAME)
  IDocumentReferenceCache<String> navCache;

  @Requirement
  IWebUtilsService webUtils;

  @Override
  public String getName() {
    return NavigationCache.NAME;
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(new NavigationCreatedEvent(), new NavigationUpdatedEvent(),
        new NavigationDeletedEvent());
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    LOGGER.debug("onEvent: event '{}', source '{}', data '{}'", event, source, data);
    if (source instanceof XWikiDocument) {
      navCache.flush(webUtils.getWikiRef((XWikiDocument) source));
    } else {
      LOGGER.error("onEvent: unable to flush cache for source '{}'", source);
    }
  }

}
