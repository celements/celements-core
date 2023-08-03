package com.celements.auth.user.listener;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;

import com.celements.auth.user.UserInstantiationException;
import com.celements.auth.user.UserPageType;
import com.celements.auth.user.UserService;
import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiUser;

@Component
public class CheckCorrectnessOfNewUserAndAddDefaultValuesListener
    extends AbstractLocalEventListener<XWikiDocument, Object> {

  private static final String NAME = "checkCorrectnessOfNewUserAndAddDefaultValues";
  private static final String XWIKI_ADMIN_GROUP_FN = "XWiki.XWikiAdminGroup";

  private final UserService userService;
  private final ClassDefinition rightsClass;
  private final ModelUtils modelUtils;

  @Inject
  public CheckCorrectnessOfNewUserAndAddDefaultValuesListener(
      UserService userService,
      @Named(XWikiRightsClass.CLASS_DEF_HINT) ClassDefinition rightsClass,
      ModelUtils modelUtils) {
    super();
    this.userService = userService;
    this.rightsClass = rightsClass;
    this.modelUtils = modelUtils;

  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return List.of(new DocumentCreatingEvent(), new DocumentUpdatingEvent());
  }

  @Override
  protected void onEventInternal(@NotNull Event event, @NotNull XWikiDocument source, Object data) {
    LOGGER.trace(
        "onEvent in checkCorrectnessOfNewUserAndAddDefaultValues for source {} and data {}", source,
        data);
    if (isUser(source)) {
      addPageTypeOnUser(source);
      setRightsOnUser(source, Arrays.asList(EAccessLevel.VIEW, EAccessLevel.EDIT,
          EAccessLevel.DELETE));
    }

  }

  private boolean isUser(XWikiDocument source) {
    try {
      userService.getUser(source.getDocRef());
      return true;
    } catch (UserInstantiationException uie) {
      LOGGER.debug("source {} is no user document", source, uie);
      return false;
    }
  }

  private void addPageTypeOnUser(XWikiDocument userDoc) {
    XWikiObjectEditor userPageTypeEditor = XWikiObjectEditor.on(userDoc)
        .filter(PageTypeClass.CLASS_REF);
    userPageTypeEditor.filter(PageTypeClass.FIELD_PAGE_TYPE, UserPageType.PAGETYPE_NAME);
    userPageTypeEditor.createFirstIfNotExists();
  }

  private void setRightsOnUser(XWikiDocument userDoc, List<EAccessLevel> rights) {
    XWikiObjectEditor userRightObjEditor = XWikiObjectEditor.on(userDoc).filter(rightsClass);
    userRightObjEditor.filter(XWikiRightsClass.FIELD_USERS, Arrays.asList(asXWikiUser(
        userDoc.getDocumentReference())));
    userRightObjEditor.filter(XWikiRightsClass.FIELD_LEVELS, rights);
    userRightObjEditor.filter(XWikiRightsClass.FIELD_ALLOW, true);
    userRightObjEditor.createFirst();
    XWikiObjectEditor admGrpObjEditor = XWikiObjectEditor.on(userDoc).filter(rightsClass);
    admGrpObjEditor.filter(XWikiRightsClass.FIELD_GROUPS, Arrays.asList(XWIKI_ADMIN_GROUP_FN));
    admGrpObjEditor.filter(XWikiRightsClass.FIELD_LEVELS, rights);
    admGrpObjEditor.filter(XWikiRightsClass.FIELD_ALLOW, true);
    admGrpObjEditor.createFirst();
  }

  private XWikiUser asXWikiUser(DocumentReference userDocRef) {
    return new XWikiUser(modelUtils.serializeRefLocal(userDocRef));
  }

}
