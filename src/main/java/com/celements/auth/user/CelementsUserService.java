package com.celements.auth.user;

import static com.celements.web.classcollections.IOldCoreClassConfig.*;
import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Predicates.*;

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
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
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
import com.celements.model.util.References;
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
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.Utils;

@Component
public class CelementsUserService implements UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CelementsUserService.class);

  static final Function<String, DocumentReference> DOC_REF_RESOLVER = new ReferenceMarshaller<>(
      DocumentReference.class).getResolver();
  static final String XWIKI_ALL_GROUP_FN = "XWiki.XWikiAllGroup";
  static final String XWIKI_ADMIN_GROUP_FN = "XWiki.XWikiAdminGroup";

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
  public DocumentReference resolveUserDocRef(String accountName) {
    DocumentReference userDocRef = modelUtils.resolveRef(accountName, DocumentReference.class,
        getUserSpaceRef());
    return References.adjustRef(userDocRef, DocumentReference.class, new EntityReference(
        XWIKI_USERS_CLASS_SPACE, EntityType.SPACE));
  }

  @Override
  public User getUser(DocumentReference userDocRef) throws UserInstantiationException {
    User user = Utils.getComponent(User.class, CelementsUser.NAME);
    user.initialize(userDocRef);
    return user;
  }

  @Override
  public Set<String> getPossibleLoginFields() {
    Set<String> fields = FluentIterable.from(getSplitXWikiPreference(
        XWIKI_PREFERENCES_CELLOGIN_PROPERTY, "celements.login.userfields",
        DEFAULT_LOGIN_FIELD)).filter(new UserClassFieldFilter()).toSet();
    if (fields.isEmpty()) {
      fields = ImmutableSet.of(DEFAULT_LOGIN_FIELD);
    }
    return fields;
  }

  @Override
  public User createNewUser(String accountName, Map<String, String> userData, boolean validate)
      throws UserCreateException {
    userData.put("xwikiname", checkNotNull(Strings.emptyToNull(accountName)));
    return createNewUser(userData, validate);
  }

  @Override
  public User createNewUser(Map<String, String> userData, boolean validate)
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

  // TODO use instead when available: [CELDEV-692] NextFreeDocService getNextFreeRandomDocRef
  synchronized DocumentReference getOrGenerateUserDocRef(String accountName) {
    accountName = Strings.nullToEmpty(accountName);
    if (accountName.isEmpty()) {
      accountName = RandomStringUtils.randomAlphanumeric(12);
    }
    DocumentReference userDocRef = resolveUserDocRef(accountName);
    while (modelAccess.exists(userDocRef)) {
      userDocRef = new DocumentReference(RandomStringUtils.randomAlphanumeric(12),
          getUserSpaceRef());
    }
    return userDocRef;
  }

  void createUserFromData(XWikiDocument userDoc, Map<String, String> userData)
      throws DocumentAccessException {
    String userFN = modelUtils.serializeRefLocal(userDoc.getDocumentReference());
    userDoc.setParentReference((EntityReference) usersClass.getDocRef(
        userDoc.getDocumentReference().getWikiReference()));
    userDoc.setCreator(userFN);
    userDoc.setAuthor(userFN);
    userDoc.setContent("#includeForm(\"XWiki.XWikiUserSheet\")");
    userData.putIfAbsent(XWikiUsersClass.FIELD_ACTIVE.getName(), "0");
    userData.putIfAbsent(XWikiUsersClass.FIELD_PASSWORD.getName(),
        RandomStringUtils.randomAlphanumeric(24));
    try {
      BaseObject userObject = XWikiObjectEditor.on(userDoc).filter(usersClass).createFirst();
      getXWiki().getUserClass(context.getXWikiContext()).fromMap(userData, userObject);
    } catch (XWikiException xwe) {
      throw new DocumentAccessException(usersClass.getClassReference().getDocRef(), xwe);
    }
  }

  void setRightsOnUser(XWikiDocument userDoc, List<EAccessLevel> rights) {
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

  void addUserToDefaultGroups(DocumentReference userDocRef) {
    FluentIterable<String> defaultGroupNames = FluentIterable.from(getSplitXWikiPreference(
        "initialGroups", "xwiki.users.initialGroups", "")).append(XWIKI_ALL_GROUP_FN);
    for (DocumentReference groupDocRef : defaultGroupNames.transform(DOC_REF_RESOLVER).toSet()) {
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
    WikiReference wiki = userDocRef.getWikiReference();
    groupDocRef = References.adjustRef(groupDocRef, DocumentReference.class, wiki);
    XWikiDocument groupDoc = modelAccess.getDocument(groupDocRef);
    XWikiObjectEditor.on(groupDoc).filter(XWikiGroupsClass.FIELD_MEMBER,
        userDocRef).createFirstIfNotExists();
    modelAccess.saveDocument(groupDoc, webUtils.getAdminMessageTool().get(
        "core.comment.addedUserToGroup"));
    try {
      XWikiGroupService gservice = getXWiki().getGroupService(context.getXWikiContext());
      gservice.addUserToGroup(asXWikiUser(userDocRef).getUser(), wiki.getName(),
          modelUtils.serializeRefLocal(groupDocRef), context.getXWikiContext());
    } catch (XWikiException xwe) {
      LOGGER.warn("Failed to update group service cache", xwe);
    }
  }

  private boolean areIdentifiersUnique(Map<String, String> userData) throws QueryException {
    Set<String> possibleLogins = getPossibleLoginFields();
    boolean isUnique = true;
    for (String key : userData.keySet()) {
      if (!key.trim().isEmpty() && possibleLogins.contains(key)) {
        if (getUserForLoginField(userData.get(key), possibleLogins).isPresent()) {
          isUnique = false;
        }
      }
    }
    return isUnique;
  }

  @Override
  public Optional<User> getUserForLoginField(String login) {
    return getUserForLoginField(login, getPossibleLoginFields());
  }

  @Override
  public Optional<User> getUserForLoginField(String login, Collection<String> possibleLoginFields) {
    login = Strings.nullToEmpty(login).trim();
    checkArgument(!login.isEmpty());
    possibleLoginFields = FluentIterable.from(firstNonNull(possibleLoginFields,
        ImmutableSet.<String>of())).filter(new UserClassFieldFilter()).toSet();
    if (possibleLoginFields.isEmpty()) {
      possibleLoginFields = ImmutableSet.of(DEFAULT_LOGIN_FIELD);
    }
    User user = null;
    if (possibleLoginFields.contains(DEFAULT_LOGIN_FIELD)) {
      try {
        user = getUser(resolveUserDocRef(login));
      } catch (UserInstantiationException exc) {
        LOGGER.debug("getUserForData - login [{}] is not valid user name", login, exc);
      }
    }
    if (user == null) {
      user = loadUniqueUserForQuery(login, possibleLoginFields);
    }
    return Optional.fromJavaUtil(java.util.Optional.ofNullable(user)
        .filter(not(User::isSuspended)));
  }

  private User loadUniqueUserForQuery(String login, Collection<String> possibleLoginFields) {
    User user = null;
    try {
      Query query = queryManager.createQuery(buildPossibleLoginXwql(possibleLoginFields.iterator()),
          Query.XWQL);
      query.bindValue("space", getUserSpaceRef().getName());
      query.bindValue("login", login.toLowerCase().replace("'", "''"));
      List<DocumentReference> userDocRefs = queryExecService.executeAndGetDocRefs(query);
      LOGGER.info("loadUniqueUserForQuery - for login [{}] and possibleLoginFields [{}]: {}", login,
          possibleLoginFields, userDocRefs);
      if (userDocRefs.size() == 1) {
        user = getUser(userDocRefs.get(0));
      } else if (userDocRefs.size() > 1) {
        LOGGER.warn("loadUniqueUserForQuery - multiple results for [{}]", login);
      }
    } catch (QueryException | UserInstantiationException exc) {
      LOGGER.warn("getUserForData - failed for login [{}] and possibleLoginFields [{}]", login,
          possibleLoginFields, exc);
    }
    return user;
  }

  String buildPossibleLoginXwql(Iterator<String> possibleLoginFields) {
    StringBuilder xwql = new StringBuilder();
    xwql.append("from doc.object(XWiki.XWikiUsers) usr where doc.space = :space and ");
    while (possibleLoginFields.hasNext()) {
      String field = possibleLoginFields.next().toLowerCase();
      if (StringUtils.isAlphanumeric(field)) {
        xwql.append("lower(");
        if (DEFAULT_LOGIN_FIELD.equals(field)) {
          xwql.append("doc.name");
        } else {
          xwql.append("usr.").append(field);
        }
        xwql.append(") = :login");
        if (possibleLoginFields.hasNext()) {
          xwql.append(" or ");
        }
      }
    }
    return xwql.toString();
  }

  private class UserClassFieldFilter implements Predicate<String> {

    @Override
    public boolean apply(String field) {
      field = field.toLowerCase();
      return DEFAULT_LOGIN_FIELD.equals(field) || usersClass.getField(field).isPresent();
    }
  }

  private Iterable<String> getSplitXWikiPreference(String prefName, String cfgParam,
      String defaultValue) {
    String prefValue = Strings.nullToEmpty(getXWiki().getXWikiPreference(prefName, cfgParam,
        defaultValue, context.getXWikiContext())).trim();
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
