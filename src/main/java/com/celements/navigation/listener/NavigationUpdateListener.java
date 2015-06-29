package com.celements.navigation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.event.Event;

import com.celements.common.observation.listener.AbstractDocumentUpdateListener;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.event.NavigationCreatedEvent;
import com.celements.navigation.event.NavigationUpdatedEvent;

@Component(NavigationUpdateListener.NAME)
public class NavigationUpdateListener extends AbstractDocumentUpdateListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(NavigationUpdateListener.class);

  public static final String NAME = "NavigationUpdateListener";

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
  protected Event getUpdatingEvent(DocumentReference docRef) {
    return null;
  }

  @Override
  protected Event getUpdatedEvent(DocumentReference docRef) {
    return new NavigationUpdatedEvent();
  }

  @Override
  protected Event getDeletingEvent(DocumentReference docRef) {
    return null;
  }

  @Override
  protected Event getDeletedEvent(DocumentReference docRef) {
    return new NavigationCreatedEvent();
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected boolean includeDocFields() {
    return false;
  }

}
