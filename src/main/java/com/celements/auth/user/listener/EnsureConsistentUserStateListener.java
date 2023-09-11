package com.celements.auth.user.listener;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.event.Event;

import com.celements.auth.user.UserPageType;
import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.util.ModelUtils;
import com.celements.observation.save.SaveEventOperation;
import com.celements.observation.save.object.ObjectEvent;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiUser;

@Component(EnsureConsistentUserStateListener.NAME)
public class EnsureConsistentUserStateListener
    extends AbstractLocalEventListener<XWikiDocument, Object> {

  public static final String NAME = "ensureConsistentUserState";
  private static final String XWIKI_ADMIN_GROUP_FN = "XWiki.XWikiAdminGroup";

  private final ModelUtils modelUtils;

  @Inject
  public EnsureConsistentUserStateListener(ModelUtils modelUtils) {
    super();
    this.modelUtils = modelUtils;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return List.of(new ObjectEvent(SaveEventOperation.CREATING, XWikiUsersClass.CLASS_REF));
  }

  @Override
  protected void onEventInternal(@NotNull Event event, @NotNull XWikiDocument source, Object data) {
    LOGGER.trace("onObjectEvent in ensureConsistentUserState for source {} and data {}",
        source, data);
    addPageTypeOnUser(source);
    setRightsOnUser(source, Arrays.asList(EAccessLevel.VIEW, EAccessLevel.EDIT,
        EAccessLevel.DELETE));
    setDefaultValuesOnNewUser(source);
  }

  void addPageTypeOnUser(XWikiDocument userDoc) {
    XWikiObjectEditor userPageTypeEditor = XWikiObjectEditor.on(userDoc)
        .filter(PageTypeClass.CLASS_REF);
    userPageTypeEditor.filter(PageTypeClass.FIELD_PAGE_TYPE, UserPageType.PAGETYPE_NAME);
    userPageTypeEditor.createFirstIfNotExists();
  }

  void setRightsOnUser(XWikiDocument userDoc, List<EAccessLevel> rights) {
    XWikiObjectEditor userRightObjEditor = XWikiObjectEditor.on(userDoc)
        .filter(XWikiRightsClass.CLASS_REF);
    userRightObjEditor.filter(XWikiRightsClass.FIELD_USERS, Arrays.asList(asXWikiUser(
        userDoc.getDocumentReference())));
    userRightObjEditor.filter(XWikiRightsClass.FIELD_LEVELS, rights);
    userRightObjEditor.filter(XWikiRightsClass.FIELD_ALLOW, true);
    userRightObjEditor.createFirstIfNotExists();
    XWikiObjectEditor admGrpObjEditor = XWikiObjectEditor.on(userDoc)
        .filter(XWikiRightsClass.CLASS_REF);
    admGrpObjEditor.filter(XWikiRightsClass.FIELD_GROUPS, Arrays.asList(XWIKI_ADMIN_GROUP_FN));
    admGrpObjEditor.filter(XWikiRightsClass.FIELD_LEVELS, rights);
    admGrpObjEditor.filter(XWikiRightsClass.FIELD_ALLOW, true);
    admGrpObjEditor.createFirstIfNotExists();
  }

  void setDefaultValuesOnNewUser(XWikiDocument userDoc) {
    String userFN = modelUtils.serializeRefLocal(userDoc.getDocumentReference());
    userDoc.setParentReference((EntityReference) XWikiUsersClass.CLASS_REF.getDocRef(
        userDoc.getDocumentReference().getWikiReference()));
    userDoc.setCreator(userFN);
    userDoc.setAuthor(userFN);
    XWikiObjectEditor.on(userDoc)
        .filter(XWikiUsersClass.CLASS_REF)
        .filterAbsent(XWikiUsersClass.FIELD_PASSWORD)
        .editField(XWikiUsersClass.FIELD_PASSWORD)
        .first(RandomStringUtils.randomAlphanumeric(24));
  }

  private XWikiUser asXWikiUser(DocumentReference userDocRef) {
    return new XWikiUser(modelUtils.serializeRefLocal(userDocRef));
  }

}
