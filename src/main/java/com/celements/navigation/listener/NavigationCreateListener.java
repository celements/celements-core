package com.celements.navigation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractDocumentCreateListener;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.event.NavigationCreatedEvent;

@Component(NavigationCreateListener.NAME)
public class NavigationCreateListener extends AbstractDocumentCreateListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(NavigationCreateListener.class);

  public static final String NAME = "NavigationCreateListener";

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
  protected Event getCreatingEvent(DocumentReference docRef) {
    return null;
  }

  @Override
  protected Event getCreatedEvent(DocumentReference docRef) {
    return new NavigationCreatedEvent();
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
