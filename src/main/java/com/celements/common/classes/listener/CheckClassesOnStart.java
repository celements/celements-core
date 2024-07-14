package com.celements.common.classes.listener;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.celements.common.classes.IClassesCompositorComponent;
import com.celements.init.CelementsStartedEvent;
import com.celements.wiki.WikiService;
import com.xpn.xwiki.XWikiConfigSource;

@Component
public class CheckClassesOnStart implements ApplicationListener<CelementsStartedEvent>, Ordered {

  private static final Logger LOGGER = LoggerFactory.getLogger(CheckClassesOnStart.class);

  private final XWikiConfigSource xwikiCfg;
  private final WikiService wikiService;
  private final IClassesCompositorComponent classesCompositor;

  @Inject
  public CheckClassesOnStart(
      XWikiConfigSource xwikiCfg,
      WikiService wikiService,
      IClassesCompositorComponent classesCompositor) {
    this.xwikiCfg = xwikiCfg;
    this.wikiService = wikiService;
    this.classesCompositor = classesCompositor;
  }

  @Override
  public int getOrder() {
    return 100;
  }

  @Override
  public void onApplicationEvent(CelementsStartedEvent event) {
    LOGGER.trace("onApplicationEvent: {}", event);
    if ("1".equals(xwikiCfg.getProperty("celements.classCollections.checkOnStart", "1"))) {
      LOGGER.info("checking classes");
      wikiService.streamAllWikis()
          .forEach(classesCompositor::checkClasses);
    }
  }

}
