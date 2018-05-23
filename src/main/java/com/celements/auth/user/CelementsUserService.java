package com.celements.auth.user;

import static com.celements.web.classcollections.IOldCoreClassConfig.*;
import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.marshalling.ReferenceMarshaller;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAccessException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.util.ModelUtils;
import com.celements.query.IQueryExecutionServiceRole;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.classes.oldcore.XWikiGroupsClass;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.celements.web.plugin.cmd.PasswordRecoveryAndEmailValidationCommand;
import com.celements.web.plugin.cmd.SendValidationFailedException;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.Utils;

@Component
public class CelementsUserService implements UserService {

  private static Logger LOGGER = LoggerFactory.getLogger(CelementsUserService.class);

  private static final Function<String, DocumentReference> DOC_REF_RESOLVER = new ReferenceMarshaller<>(
      DocumentReference.class).getResolver();

  @Requirement(XWikiUsersClass.CLASS_DEF_HINT)
  private ClassDefinition usersClass;

  @Requirement(XWikiGroupsClass.CLASS_DEF_HINT)
  private ClassDefinition groupsClass;

  @Requirement(XWikiRightsClass.CLASS_DEF_HINT)
  private ClassDefinition rightsClass;

  @Requirement
  private QueryManager queryManager;

  @Requirement
  private IQueryExecutionServiceRole queryExecService;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private ModelContext context;

  @Override
  public SpaceReference getUserSpaceRef() {
    return getUserSpaceRef(null);
  }

  @Override
  public SpaceReference getUserSpaceRef(WikiReference wikiRef) {
    return new SpaceReference(XWIKI_USERS_CLASS_SPACE, firstNonNull(wikiRef, context.getWikiRef()));
  }

  @Override
  public DocumentReference completeUserDocRef(String accountName) {
    DocumentReference userDocRef;
    if (accountName.startsWith(getUserSpaceRef().getName() + ".")) {
      userDocRef = modelUtils.resolveRef(accountName, DocumentReference.class);
    } else {
      userDocRef = new DocumentReference(accountName, getUserSpaceRef());
    }
    return userDocRef;
  }

  @Override
  public User getUser(DocumentReference userDocRef) throws UserInstantiationException {
    User user = Utils.getComponent(User.class, CelementsUser.NAME);
    user.initialize(userDocRef);
    return user;
  }

  @Override
  public boolean isGuestUser(DocumentReference userDocRef) {
    return userDocRef.getLastSpaceReference().getName().equals(getUserSpaceRef().getName())
        && userDocRef.getName().equals(XWikiRightService.GUEST_USER);
  }

  @Override
  public Set<String> getPossibleLoginFields() {
    return ImmutableSet.copyOf(getSplitXWikiPreference(XWIKI_PREFERENCES_CELLOGIN_PROPERTY,
        "celements.login.userfields", DEFAULT_LOGIN_FIELD));
  }

  @Override
  public User createNewUser(String accountName, Map<String, String> userData, boolean validate)
      throws UserCreateException {
    userData.put("xwikiname", checkNotNull(Strings.emptyToNull(accountName)));
    return createNewUser(userData, validate);
  }

  @Override
  public synchronized User createNewUser(Map<String, String> userData, boolean validate)
      throws UserCreateException {
    try {
      DocumentReference userDocRef = getOrGenerateUserDocRef(userData.remove("xwikiname"));
      XWikiDocument userDoc = modelAccess.createDocument(userDocRef);
      if (areIdentifiersUnique(userData)) {
        createUserFromData(userDoc, userData);
        setRightsOnUser(userDoc, Arrays.asList(EAccessLevel.VIEW, EAccessLevel.EDIT,
            EAccessLevel.DELETE));
        modelAccess.saveDocument(userDoc, webUtils.getAdminMessageTool().get(
            "core.comment.createdUser"));
        addUserToDefaultGroups(userDocRef);
        if (validate) {
          new PasswordRecoveryAndEmailValidationCommand().sendNewValidationToAccountEmail(
              userDocRef);
        }
      }
      return getUser(userDocRef);
    } catch (DocumentAccessException | QueryException | SendValidationFailedException
        | UserInstantiationException exc) {
      throw new UserCreateException(exc);
    }
  }

  private DocumentReference getOrGenerateUserDocRef(String accountName) {
    accountName = Strings.nullToEmpty(accountName);
    if (accountName.isEmpty()) {
      accountName = RandomStringUtils.randomAlphanumeric(12);
    }
    DocumentReference userDocRef = completeUserDocRef(accountName);
    while (modelAccess.exists(userDocRef)) {
      userDocRef = new DocumentReference(RandomStringUtils.randomAlphanumeric(12),
          getUserSpaceRef());
    }
    return userDocRef;
  }

  public void createUserFromData(XWikiDocument userDoc, Map<String, String> userData)
      throws DocumentAccessException {
    String userFN = modelUtils.serializeRefLocal(userDoc.getDocumentReference());
    userDoc.setParentReference(usersClass.getClassReference());
    userDoc.setCreator(userFN);
    userDoc.setAuthor(userFN);
    userDoc.setContent("#includeForm(\"XWiki.XWikiUserSheet\")");
    userData.putIfAbsent(XWikiUsersClass.FIELD_ACTIVE.getName(), "0");
    userData.putIfAbsent(XWikiUsersClass.FIELD_PASSWORD.getName(),
        RandomStringUtils.randomAlphanumeric(8));
    try {
      BaseObject userObject = XWikiObjectEditor.on(userDoc).filter(usersClass).createFirst();
      getXWiki().getUserClass(context.getXWikiContext()).fromMap(userData, userObject);
    } catch (XWikiException xwe) {
      throw new DocumentAccessException(usersClass.getClassReference().getDocRef(), xwe);
    }
  }

  private void setRightsOnUser(XWikiDocument doc, List<EAccessLevel> rights) {
    XWikiObjectEditor userRightObjEditor = XWikiObjectEditor.on(doc).filter(rightsClass);
    userRightObjEditor.filter(XWikiRightsClass.FIELD_USERS, Arrays.asList(asXWikiUser(
        doc.getDocumentReference())));
    userRightObjEditor.filter(XWikiRightsClass.FIELD_LEVELS, rights);
    userRightObjEditor.filter(XWikiRightsClass.FIELD_ALLOW, true);
    userRightObjEditor.createFirst();
    XWikiObjectEditor admGrpObjEditor = XWikiObjectEditor.on(doc).filter(rightsClass);
    admGrpObjEditor.filter(XWikiRightsClass.FIELD_GROUPS, Arrays.asList(getAdminGroupName()));
    admGrpObjEditor.filter(XWikiRightsClass.FIELD_LEVELS, rights);
    admGrpObjEditor.filter(XWikiRightsClass.FIELD_ALLOW, true);
    admGrpObjEditor.createFirst();
  }

  private void addUserToDefaultGroups(DocumentReference userDocRef) {
    FluentIterable<String> defaultGroupNames = FluentIterable.from(getSplitXWikiPreference(
        "initialGroups", "xwiki.users.initialGroups", getAdminGroupName()));
    for (DocumentReference groupDocRef : defaultGroupNames.transform(DOC_REF_RESOLVER)) {
      try {
        addUserToGroup(userDocRef, groupDocRef);
        LOGGER.info("createUser - added user '{}' to group '{}'", userDocRef, groupDocRef);
      } catch (DocumentAccessException exc) {
        LOGGER.warn("createUser - failed adding user '{}' to group '{}'", userDocRef, groupDocRef,
            exc);
      }
    }
  }

  private void addUserToGroup(DocumentReference userDocRef, DocumentReference groupDocRef)
      throws DocumentNotExistsException, DocumentSaveException {
    XWikiDocument groupDoc = modelAccess.getDocument(groupDocRef);
    XWikiObjectEditor.on(groupDoc).filter(XWikiGroupsClass.FIELD_MEMBER,
        userDocRef).createFirstIfNotExists();
    modelAccess.saveDocument(groupDoc, webUtils.getAdminMessageTool().get(
        "core.comment.addedUserToGroup"));
    try {
      XWikiGroupService gservice = getXWiki().getGroupService(context.getXWikiContext());
      gservice.addUserToGroup(asXWikiUser(userDocRef).getUser(), context.getWikiRef().getName(),
          modelUtils.serializeRefLocal(groupDocRef), context.getXWikiContext());
    } catch (XWikiException xwe) {
      LOGGER.warn("Failed to update group service cache", xwe);
    }
  }

  private boolean areIdentifiersUnique(Map<String, String> userData) throws QueryException {
    Set<String> possibleLogins = getPossibleLoginFields();
    boolean isUnique = true;
    for (String key : userData.keySet()) {
      if (!"".equals(key.trim()) && possibleLogins.contains(key)) {
        if (getUserForData(userData.get(key), possibleLogins).isPresent()) {
          isUnique = false;
        }
      }
    }
    return isUnique;
  }

  private String getAdminGroupName() {
    return "XWiki.XWikiAdminGroup";
  }

  @Override
  public Optional<User> getUserForData(String login) {
    return getUserForData(login, getPossibleLoginFields());
  }

  @Override
  public Optional<User> getUserForData(String login, Collection<String> possibleLoginFields) {
    checkArgument(!Strings.nullToEmpty(login).trim().isEmpty());
    checkNotNull(possibleLoginFields);
    if (possibleLoginFields.isEmpty()) {
      possibleLoginFields.add(DEFAULT_LOGIN_FIELD);
    }
    try {
      List<DocumentReference> userDocRefs = queryExecService.executeAndGetDocRefs(
          getUserQueryForPossibleLogin(login, possibleLoginFields));
      LOGGER.info("getUserForData - for login [{}] and possibleLoginFields [{}]: {}", login,
          possibleLoginFields, userDocRefs);
      if (userDocRefs.size() == 1) {
        return Optional.of(getUser(userDocRefs.get(0)));
      }
    } catch (QueryException | UserInstantiationException exc) {
      LOGGER.info("getUserForData - failed for login [{}] and possibleLoginFields [{}]", login,
          possibleLoginFields, exc);
    }
    return Optional.absent();
  }

  private Query getUserQueryForPossibleLogin(String login, Collection<String> possibleLoginFields)
      throws QueryException {
    StringBuilder xwql = new StringBuilder();
    xwql.append("from doc.object(XWiki.XWikiUsers) usr where doc.space = :space and ");
    Iterator<String> iter = possibleLoginFields.iterator();
    while (iter.hasNext()) {
      String field = iter.next();
      if (StringUtils.isAlphanumeric(field)) {
        xwql.append("lower(");
        xwql.append(DEFAULT_LOGIN_FIELD.equals(field) ? "doc" : "usr").append(".").append(field);
        xwql.append(") = :login");
        if (iter.hasNext()) {
          xwql.append(" or ");
        }
      }
    }
    Query query = queryManager.createQuery(xwql.toString(), Query.XWQL);
    query.bindValue("space", getUserSpaceRef().getName());
    query.bindValue("login", login.toLowerCase().replace("'", "''"));
    return query;
  }

  private Iterable<String> getSplitXWikiPreference(String prefName, String cfgParam,
      String defaultValue) {
    String prefValue = Strings.nullToEmpty(getXWiki().getXWikiPreference(prefName, cfgParam,
        defaultValue, context.getXWikiContext()));
    return Splitter.on(",").omitEmptyStrings().split(prefValue);
  }

  @Deprecated
  private XWiki getXWiki() {
    return context.getXWikiContext().getWiki();
  }

  private XWikiUser asXWikiUser(DocumentReference userDocRef) {
    return new XWikiUser(modelUtils.serializeRefLocal(userDocRef));
  }

}
