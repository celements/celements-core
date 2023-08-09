package com.celements.auth.user;

import static com.celements.common.MoreOptional.*;
import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.celements.web.classcollections.IOldCoreClassConfig.*;
import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Predicates.*;
import static com.google.common.collect.ImmutableSet.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.init.XWikiProvider;
import com.celements.marshalling.ReferenceMarshaller;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentAccessException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.query.IQueryExecutionServiceRole;
import com.celements.web.classes.oldcore.XWikiGroupsClass;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.celements.web.plugin.cmd.PasswordRecoveryAndEmailValidationCommand;
import com.celements.web.plugin.cmd.SendValidationFailedException;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

@Component
public class CelementsUserService implements UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CelementsUserService.class);

  static final Function<String, DocumentReference> DOC_REF_RESOLVER = new ReferenceMarshaller<>(
      DocumentReference.class).getResolver();
  static final String XWIKI_ALL_GROUP_FN = "XWiki.XWikiAllGroup";
  static final String XWIKI_ADMIN_GROUP_FN = "XWiki.XWikiAdminGroup";

  private final ClassDefinition usersClass;
  private final QueryManager queryManager;
  private final IQueryExecutionServiceRole queryExecService;
  private final IModelAccessFacade modelAccess;
  private final ModelUtils modelUtils;
  private final IWebUtilsService webUtils;
  private final ModelContext context;
  private final INextFreeDocRole nextFreeDoc;
  private final XWikiProvider xwiki;

  @Inject
  public CelementsUserService(
      @Named(XWikiUsersClass.CLASS_DEF_HINT) ClassDefinition usersClass,
      QueryManager queryManager,
      IQueryExecutionServiceRole queryExecService,
      IModelAccessFacade modelAccess,
      ModelUtils modelUtils,
      IWebUtilsService webUtils,
      ModelContext context,
      INextFreeDocRole nextFreeDoc,
      XWikiProvider xwiki) {
    super();
    this.usersClass = usersClass;
    this.queryManager = queryManager;
    this.queryExecService = queryExecService;
    this.modelAccess = modelAccess;
    this.modelUtils = modelUtils;
    this.webUtils = webUtils;
    this.context = context;
    this.nextFreeDoc = nextFreeDoc;
    this.xwiki = xwiki;
  }

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
    return RefBuilder.from(modelUtils.resolveRef(accountName, DocumentReference.class,
        getUserSpaceRef()))
        .space(XWIKI_USERS_CLASS_SPACE)
        .build(DocumentReference.class);
  }

  @Override
  public User getUser(DocumentReference userDocRef) throws UserInstantiationException {
    User user = Utils.getComponent(User.class, CelementsUser.NAME);
    user.initialize(userDocRef);
    return user;
  }

  @Override
  public User getUser(@NotNull String accountName) throws UserInstantiationException {
    try {
      return getUser(resolveUserDocRef(accountName));
    } catch (IllegalArgumentException iae) {
      throw new UserInstantiationException(iae);
    }
  }

  @Override
  public Set<String> getPossibleLoginFields() {
    Set<String> fields = getSplitXWikiPreference(XWIKI_PREFERENCES_CELLOGIN_PROPERTY,
        "celements.login.userfields", DEFAULT_LOGIN_FIELD)
            .filter(new UserClassFieldFilter())
            .collect(toImmutableSet());
    if (fields.isEmpty()) {
      fields = Set.of(DEFAULT_LOGIN_FIELD);
    }
    return fields;
  }

  @Override
  public User createNewUser(String accountName, Map<String, String> userData, boolean validate)
      throws UserCreateException {
    userData.put(USERNAME_FIELD, checkNotNull(Strings.emptyToNull(accountName)));
    return createNewUser(userData, validate);
  }

  @Override
  public User createNewUser(Map<String, String> userData, boolean validate)
      throws UserCreateException {
    checkIdentifiersForExistingUser(userData).ifPresent(rethrowConsumer(user -> {
      throw new UserCreateException("unable to create user with existing identifiers on " + user);
    }));
    User user = createNewUser(userData);
    if (validate) {
      sendValidationMail(user);
    }
    return user;
  }

  @Override
  public User getOrCreateNewUser(Map<String, String> userData) throws UserCreateException {
    return checkIdentifiersForExistingUser(userData)
        .orElseGet(rethrow(() -> createNewUser(userData)));
  }

  private User createNewUser(Map<String, String> userData) throws UserCreateException {
    DocumentReference userDocRef = getOrGenerateUserDocRef(userData.remove(USERNAME_FIELD));
    try {
      XWikiDocument userDoc = modelAccess.createDocument(userDocRef);
      fillInUserData(userDoc, userData);
      modelAccess.saveDocument(userDoc, getMessage("core.comment.createdUser"));
    } catch (DocumentAccessException dae) {
      throw new UserCreateException(dae);
    }
    try {
      return getUser(userDocRef);
    } catch (UserInstantiationException exc) {
      throw new IllegalStateException("should not happen", exc);
    }
  }

  synchronized DocumentReference getOrGenerateUserDocRef(String accountName) {
    accountName = Strings.nullToEmpty(accountName);
    DocumentReference userDocRef = null;
    if (!accountName.isEmpty()) {
      userDocRef = resolveUserDocRef(accountName);
      if (modelAccess.exists(userDocRef)) {
        userDocRef = null;
      }
    }
    if (userDocRef == null) {
      userDocRef = nextFreeDoc.getNextRandomPageDocRef(getUserSpaceRef(), 12, null);
    }
    return userDocRef;
  }

  void fillInUserData(XWikiDocument userDoc, Map<String, String> userData)
      throws DocumentAccessException {
    userData.putIfAbsent(XWikiUsersClass.FIELD_ACTIVE.getName(), "0");
    userData.putIfAbsent(XWikiUsersClass.FIELD_PASSWORD.getName(),
        RandomStringUtils.randomAlphanumeric(24));
    try {
      BaseObject userObject = XWikiObjectEditor.on(userDoc).filter(usersClass).createFirst();
      xwiki.get()
          .orElseThrow()
          .getUserClass(context.getXWikiContext())
          .fromMap(userData, userObject);
    } catch (XWikiException xwe) {
      throw new DocumentAccessException(usersClass.getClassReference().getDocRef(), xwe);
    }
  }

  @Override
  public boolean addUserToDefaultGroups(User user) throws DocumentSaveException {
    checkNotNull(user);
    return getSplitXWikiPreference("initialGroups", "xwiki.users.initialGroups", "")
        .append(XWIKI_ALL_GROUP_FN)
        .filter(Objects::nonNull)
        .distinct()
        .map(ClassReference::new)
        .map(rethrowFunction(groupRef -> addUserToGroup(user, groupRef)))
        .reduce(false, (x, y) -> x || y);
  }

  @Override
  public boolean addUserToGroup(User user, ClassReference groupRef) throws DocumentSaveException {
    WikiReference wikiRef = checkNotNull(user).getDocRef().getWikiReference();
    XWikiDocument groupDoc = modelAccess.getOrCreateDocument(
        checkNotNull(groupRef).getDocRef(wikiRef));
    XWikiObjectEditor editor = XWikiObjectEditor.on(groupDoc)
        .filter(XWikiGroupsClass.FIELD_MEMBER, user.getDocRef());
    if (editor.fetch().exists()) {
      LOGGER.debug("addUserToGroup - user [{}] already exists in group [{}]", user, groupRef);
      return false;
    } else {
      editor.createFirstIfNotExists();
      modelAccess.saveDocument(groupDoc, getMessage("core.comment.addedUserToGroup"));
      LOGGER.info("addUserToGroup - user [{}] to group [{}]", user, groupRef);
      return true;
    }
  }

  private Optional<User> checkIdentifiersForExistingUser(Map<String, String> userData) {
    final Set<String> possibleLogins = getPossibleLoginFields();
    return EntryStream.of(userData)
        .mapKeys(String::trim)
        .filterKeys(not(Strings::isNullOrEmpty))
        .filterKeys(possibleLogins::contains)
        .flatMapValues(login -> stream(getUserForLoginField(login, possibleLogins).toJavaUtil()))
        .peekValues(user -> LOGGER.info("user already exists [{}]", user))
        .values().findAny();
  }

  @Override
  public com.google.common.base.Optional<User> getUserForLoginField(String login) {
    return getUserForLoginField(login, getPossibleLoginFields());
  }

  @Override
  public com.google.common.base.Optional<User> getUserForLoginField(String login,
      Collection<String> possibleLoginFields) {
    login = Strings.nullToEmpty(login).trim();
    checkArgument(!login.isEmpty());
    possibleLoginFields = Optional.ofNullable(possibleLoginFields)
        .map(Collection::stream).orElseGet(Stream::empty)
        .filter(new UserClassFieldFilter())
        .collect(toImmutableSet());
    if (possibleLoginFields.isEmpty()) {
      possibleLoginFields = Set.of(DEFAULT_LOGIN_FIELD);
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
    return com.google.common.base.Optional.fromJavaUtil(java.util.Optional.ofNullable(user)
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

  @Override
  public boolean sendValidationMail(User user) {
    try {
      return new PasswordRecoveryAndEmailValidationCommand()
          .sendNewValidationToAccountEmail(user.getDocRef());
    } catch (SendValidationFailedException exc) {
      LOGGER.error("sendValidationMail - failed  for [{}]", user, exc);
      return false;
    }
  }

  private class UserClassFieldFilter implements Predicate<String> {

    @Override
    public boolean test(String field) {
      field = field.toLowerCase();
      return DEFAULT_LOGIN_FIELD.equals(field) || usersClass.getField(field).isPresent();
    }
  }

  private StreamEx<String> getSplitXWikiPreference(String prefName, String cfgParam,
      String defaultValue) {
    String prefValue = Strings.nullToEmpty(xwiki.get()
        .orElseThrow()
        .getXWikiPreference(prefName, cfgParam, defaultValue, context.getXWikiContext()))
        .trim();
    return StreamEx.of(Splitter.on(",").omitEmptyStrings().splitToStream(prefValue));
  }

  private String getMessage(String key) {
    return webUtils.getAdminMessageTool().get(key);
  }

}
