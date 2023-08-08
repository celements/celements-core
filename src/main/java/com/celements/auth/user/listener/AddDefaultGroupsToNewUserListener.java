package com.celements.auth.user.listener;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.observation.event.Event;

import com.celements.auth.user.User;
import com.celements.auth.user.UserInstantiationException;
import com.celements.auth.user.UserService;
import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentSaveException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class AddDefaultGroupsToNewUserListener
    extends AbstractLocalEventListener<XWikiDocument, Object> {

  private static final String NAME = "addDefaultGroupsToNewUser";

  private final UserService userService;

  @Inject
  public AddDefaultGroupsToNewUserListener(UserService userService) {
    super();
    this.userService = userService;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return List.of(new DocumentCreatedEvent());
  }

  @Override
  protected void onEventInternal(@NotNull Event event, XWikiDocument source, Object data) {
    LOGGER.trace("onEvent in addDefaultGroupsToNewUser for source [{}] and data [{}].", source,
        data);
    try {
      User user = userService.getUser(source.getDocRef());
      userService.addUserToDefaultGroups(user);
    } catch (DocumentSaveException dse) {
      LOGGER.error("source {} couldn't be added to defaultGroups", source, dse);
      deleteDanglingUser(source);
    } catch (UserInstantiationException uie) {
      LOGGER.debug("source {} is no user document", source, uie);
    }
  }

  private void deleteDanglingUser(XWikiDocument source) {
    try {
      modelAccess.deleteDocument(source.getDocRef(), false);
    } catch (DocumentDeleteException delExc) {
      LOGGER.debug("unable to delete dangling user [{}]", source.getDocRef());
    }
  }
}
