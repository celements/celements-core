package com.celements.web;

import static com.celements.web.classcollections.IOldCoreClassConfig.*;
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
import com.xpn.xwiki.user.api.XWikiUser;

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
    return new SpaceReference(XWIKI_USERS_CLASS_SPACE, context.getWikiRef());
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
  public XWikiUser newXWikiUser(DocumentReference userDocRef) {
    return new XWikiUser(modelUtils.serializeRefLocal(userDocRef));
  }

  @Override
  public Set<String> getPossibleLoginFields() {
    return ImmutableSet.copyOf(getSplitXWikiPreference(XWIKI_PREFERENCES_CELLOGIN_PROPERTY,
        "celements.login.userfields", DEFAULT_LOGIN_FIELD));
  }

  @Override
  public XWikiUser createUser(String accountName, Map<String, String> userData, boolean validate)
      throws UserCreateException {
    userData.put("xwikiname", checkNotNull(Strings.emptyToNull(accountName)));
    return createUser(userData, validate);
  }

  @Override
  public synchronized XWikiUser createUser(Map<String, String> userData, boolean validate)
      throws UserCreateException {
    try {
      DocumentReference userDocRef = getOrGenerateUserDocRef(userData.remove("xwikiname"));
      XWikiDocument userDoc = modelAccess.createDocument(userDocRef);
      XWikiUser user = newXWikiUser(userDocRef);
      if (areIdentifiersUnique(userData)) {
        createUserFromData(userDoc, userData);
        setRightsOnUser(userDoc, Arrays.asList(EAccessLevel.VIEW, EAccessLevel.EDIT,
            EAccessLevel.DELETE));
        modelAccess.saveDocument(userDoc, webUtils.getAdminMessageTool().get(
            "core.comment.createdUser"));
        addUserToDefaultGroups(user);
        if (validate) {
          new PasswordRecoveryAndEmailValidationCommand().sendNewValidationToAccountEmail(
              user.getUser());
        }
      }
      return user;
    } catch (DocumentAccessException | QueryException | SendValidationFailedException exc) {
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
    XWikiUser user = newXWikiUser(userDoc.getDocumentReference());
    userDoc.setParentReference(usersClass.getClassReference());
    userDoc.setCreator(user.getUser());
    userDoc.setAuthor(user.getUser());
    userDoc.setContent("#includeForm(\"XWiki.XWikiUserSheet\")");
    userData.putIfAbsent("active", "0");
    userData.putIfAbsent("password", RandomStringUtils.randomAlphanumeric(8));
    try {
      BaseObject userObject = XWikiObjectEditor.on(userDoc).filter(usersClass).createFirst();
      getXWiki().getUserClass(context.getXWikiContext()).fromMap(userData, userObject);
    } catch (XWikiException xwe) {
      throw new DocumentAccessException(usersClass.getClassReference().getDocRef(), xwe);
    }
  }

  private void setRightsOnUser(XWikiDocument doc, List<EAccessLevel> rights) {
    XWikiObjectEditor userRightObjEditor = XWikiObjectEditor.on(doc).filter(rightsClass);
    userRightObjEditor.filter(XWikiRightsClass.FIELD_USERS, Arrays.asList(newXWikiUser(
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

  private void addUserToDefaultGroups(XWikiUser user) {

    FluentIterable<String> defaultGroupNames = FluentIterable.from(getSplitXWikiPreference(
        "initialGroups", "xwiki.users.initialGroups", getAdminGroupName()));
    for (DocumentReference groupDocRef : defaultGroupNames.transform(DOC_REF_RESOLVER)) {
      try {
        addUserToGroup(user, groupDocRef);
        LOGGER.info("createUser - added user '{}' to group '{}'", user, groupDocRef);
      } catch (DocumentAccessException exc) {
        LOGGER.warn("createUser - failed adding user '{}' to group '{}'", user, groupDocRef, exc);
      }
    }
  }

  private void addUserToGroup(XWikiUser user, DocumentReference groupDocRef)
      throws DocumentNotExistsException, DocumentSaveException {
    XWikiDocument groupDoc = modelAccess.getDocument(groupDocRef);
    XWikiObjectEditor.on(groupDoc).filter(XWikiGroupsClass.FIELD_MEMBER,
        user).createFirstIfNotExists();
    modelAccess.saveDocument(groupDoc, webUtils.getAdminMessageTool().get(
        "core.comment.addedUserToGroup"));
    try {
      XWikiGroupService gservice = getXWiki().getGroupService(context.getXWikiContext());
      gservice.addUserToGroup(user.getUser(), context.getWikiRef().getName(),
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
        Optional<XWikiUser> user = getUserForData(userData.get(key), possibleLogins);
        if (user.isPresent()) {
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
  public Optional<XWikiUser> getUserForData(String login) throws QueryException {
    return getUserForData(login, getPossibleLoginFields());
  }

  @Override
  public Optional<XWikiUser> getUserForData(String login, Collection<String> possibleLoginFields)
      throws QueryException {
    checkArgument(!Strings.nullToEmpty(login).trim().isEmpty());
    checkNotNull(possibleLoginFields);
    if (possibleLoginFields.isEmpty()) {
      possibleLoginFields.add(DEFAULT_LOGIN_FIELD);
    }
    List<DocumentReference> userDocRefs = queryExecService.executeAndGetDocRefs(
        getUserQueryForPossibleLogin(login, possibleLoginFields));
    LOGGER.info("getUserForData - for login [{}] and possibleLoginFields [{}]:", userDocRefs.get(0),
        login, possibleLoginFields, userDocRefs);
    if (userDocRefs.size() == 1) {
      return Optional.of(newXWikiUser(userDocRefs.get(0)));
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

}
