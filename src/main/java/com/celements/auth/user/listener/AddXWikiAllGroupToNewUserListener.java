package com.celements.auth.user.listener;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.observation.event.Event;

import com.celements.auth.user.CelementsUserService;
import com.celements.auth.user.User;
import com.celements.auth.user.UserInstantiationException;
import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.celements.model.access.exception.DocumentSaveException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public class AddXWikiAllGroupToNewUserListener
    extends AbstractLocalEventListener<XWikiDocument, XWikiContext> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(AddXWikiAllGroupToNewUserListener.class);

  private static final String NAME = "addXWikiAllGroupToNewUser";

  private final CelementsUserService celUserService;

  @Inject
  public AddXWikiAllGroupToNewUserListener(CelementsUserService celUserService) {
    super();
    this.celUserService = celUserService;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.<Event>asList(new DocumentCreatedEvent());
  }

  @Override
  protected void onEventInternal(@NotNull Event event, XWikiDocument source, XWikiContext data) {
    LOGGER.trace("onEvent in addXWikiAllGroupToNewUser for source [{}] and context [{}].", source,
        context);
    try {
      User user = celUserService.getUser(source.getDocRef());
      // Dokument zur XWikiAllGroup hinzufügen (celUserService.addUserToDefaultGroups wäre perfekt,
      // ist aber nicht public.)
      ClassReference groupRef = null;
      try {
        celUserService.addUserToGroup(user, groupRef);
      } catch (DocumentSaveException dse) {
        LOGGER.debug("source {} couldn't be added to groupRef {}", source, groupRef, dse);
      }
    } catch (UserInstantiationException uie) {
      LOGGER.debug("source {} is no user document", source, uie);
    }
  }
}
