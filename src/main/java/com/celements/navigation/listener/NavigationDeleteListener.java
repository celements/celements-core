package com.celements.navigation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractDocumentDeleteListener;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.event.NavigationDeletedEvent;

@Component(NavigationDeleteListener.NAME)
public class NavigationDeleteListener extends AbstractDocumentDeleteListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(NavigationDeleteListener.class);

  public static final String NAME = "NavigationDeleteListener";

  @Requirement
  private INavigationClassConfig navClassConf;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected DocumentReference getRequiredObjClassRef(WikiReference wikiRef) {
    return navClassConf.getNavigationConfigClassRef(wikiRef);
  }

  @Override
  protected Event getDeletingEvent(DocumentReference docRef) {
    return null;
  }

  @Override
  protected Event getDeletedEvent(DocumentReference docRef) {
    return new NavigationDeletedEvent();
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
