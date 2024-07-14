package com.celements.mandatory.listener;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.celements.init.CelementsStartedEvent;
import com.celements.mandatory.IMandatoryDocumentCompositorRole;
import com.celements.wiki.WikiService;
import com.xpn.xwiki.XWikiConfigSource;

@Component
public class CheckMandatoryOnStart implements ApplicationListener<CelementsStartedEvent>, Ordered {

  private static final Logger LOGGER = LoggerFactory.getLogger(CheckMandatoryOnStart.class);

  private final XWikiConfigSource xwikiCfg;
  private final WikiService wikiService;
  private final IMandatoryDocumentCompositorRole mandatoryCompositor;

  @Inject
  public CheckMandatoryOnStart(
      XWikiConfigSource xwikiCfg,
      WikiService wikiService,
      IMandatoryDocumentCompositorRole mandatoryCompositor) {
    this.xwikiCfg = xwikiCfg;
    this.wikiService = wikiService;
    this.mandatoryCompositor = mandatoryCompositor;
  }

  @Override
  public int getOrder() {
    return 200;
  }

  @Override
  public void onApplicationEvent(CelementsStartedEvent event) {
    LOGGER.trace("onApplicationEvent: {}", event);
    if ("1".equals(xwikiCfg.getProperty("celements.mandatory.checkOnStart", "1"))) {
      LOGGER.info("checking mandatory documents");
      wikiService.streamAllWikis()
          .forEach(mandatoryCompositor::checkAllMandatoryDocuments);
    }
  }

}
