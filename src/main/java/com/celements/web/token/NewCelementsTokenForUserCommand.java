/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.token;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.QueryException;

import com.celements.auth.IAuthenticationServiceRole;
import com.celements.auth.user.UserInstantiationException;
import com.celements.auth.user.UserService;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.model.util.References;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class NewCelementsTokenForUserCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(NewCelementsTokenForUserCommand.class);

  /**
   * @deprecated since 3.0 instead use
   *             {@link #getNewCelementsTokenForUser(DocumentReference, boolean)}
   */
  @Deprecated
  public String getNewCelementsTokenForUser(String accountName, Boolean guestPlus,
      XWikiContext context) throws XWikiException {
    return getNewCelementsTokenForUser(accountName, guestPlus, 1440, context);
  }

  /**
   * @deprecated since 3.0 instead use
   *             {@link #getNewCelementsTokenForUser(DocumentReference, boolean, int)}
   */
  @Deprecated
  public String getNewCelementsTokenForUser(String accountName, Boolean guestPlus, int minutesValid,
      XWikiContext context) throws XWikiException {
    DocumentReference userDocRef = getUserService().resolveUserDocRef(accountName);
    try {
      return getNewCelementsTokenForUser(userDocRef, guestPlus, minutesValid);
    } catch (UserInstantiationException exc) {
      LOGGER.info("getNewCelementsTokenForUser - failed for [{}]", userDocRef, exc);
      return null;
    } catch (QueryException | DocumentSaveException exc) {
      throw new XWikiException(0, 0, "wrapper", exc);
    }
  }

  /**
   * @param userDocRef
   * @param guestPlus
   *          if user is XWiki.XWikiGuest and guestPlus is true the account
   *          XWiki.XWikiGuestPlus will be used to get the token
   * @return the generated token
   * @throws UserInstantiationException
   *           if given user is invalid
   * @throws QueryException
   *           if unable to generate token
   * @throws DocumentSaveException
   *           if unable to save the user doc
   */
  public @NotNull String getNewCelementsTokenForUser(@NotNull DocumentReference userDocRef,
      boolean guestPlus) throws UserInstantiationException, QueryException, DocumentSaveException {
    return getNewCelementsTokenForUser(userDocRef, guestPlus, 1440);
  }

  /**
   * @param userDocRef
   * @param guestPlus
   *          if user is XWiki.XWikiGuest and guestPlus is true the account
   *          XWiki.XWikiGuestPlus will be used to get the token
   * @param minutesValid
   *          how long should the token be valid in minutes
   * @return the generated token
   * @throws UserInstantiationException
   *           if given user is invalid
   * @throws QueryException
   *           if unable to generate token
   * @throws DocumentSaveException
   *           if unable to save the user doc
   */
  public @NotNull String getNewCelementsTokenForUser(@NotNull DocumentReference userDocRef,
      boolean guestPlus, int minutesValid) throws UserInstantiationException, QueryException,
      DocumentSaveException {
    LOGGER.info("getNewCelementsTokenForUser - guestPlus [{}] for user [{}]", guestPlus,
        userDocRef);
    if (guestPlus && XWikiRightService.GUEST_USER_FULLNAME.equals(getModelUtils().serializeRefLocal(
        userDocRef))) {
      userDocRef = getXWikiGuestPlusDocRef(userDocRef.getWikiReference());
    }
    XWikiDocument userDoc = getUserService().getUser(userDocRef).getDocument();
    removeOutdatedTokens(userDoc);
    String validkey = createTokenObject(userDoc, minutesValid);
    getModelAccess().saveDocument(userDoc);
    LOGGER.debug("getNewCelementsTokenForUser - sucessfully created token for user [{}]",
        userDocRef);
    return validkey;
  }

  private DocumentReference getXWikiGuestPlusDocRef(WikiReference wikiRef) {
    return References.create(DocumentReference.class, "XWikiGuestPlus",
        getUserService().getUserSpaceRef(wikiRef));
  }

  synchronized boolean removeOutdatedTokens(XWikiDocument userDoc) {
    LOGGER.trace("removeOutdatedTokens - {}", userDoc.getDocumentReference());
    boolean changed = false;
    Date now = new Date();
    for (BaseObject obj : XWikiObjectFetcher.on(userDoc).filter(getTokenClassRef()).iter()) {
      Date validUntil = obj.getDateValue("validuntil");
      if ((validUntil == null) || validUntil.before(now)) {
        LOGGER.trace("removeOutdatedTokens - deleting [{}]", obj);
        changed |= userDoc.removeXObject(obj);
      }
    }
    return changed;
  }

  private String createTokenObject(XWikiDocument userDoc, int minutesValid) throws QueryException {
    // XXX doesn't guarantee a unique key regarding tokens
    String validkey = getAuthService().getUniqueValidationKey();
    BaseObject obj = XWikiObjectEditor.on(userDoc).filter(getTokenClassRef()).createFirst();
    obj.set("tokenvalue", validkey, getContext().getXWikiContext());
    Calendar myCal = Calendar.getInstance();
    myCal.add(Calendar.MINUTE, minutesValid);
    obj.setDateValue("validuntil", myCal.getTime());
    return validkey;
  }

  ClassReference getTokenClassRef() {
    return new ClassReference("Classes", "TokenClass");
  }

  /**
   * @deprecated instead use {@link IAuthenticationServiceRole#getUniqueValidationKey()}
   */
  @Deprecated
  public String getUniqueValidationKey(XWikiContext context) throws XWikiException {
    try {
      return getAuthService().getUniqueValidationKey();
    } catch (QueryException exc) {
      throw new XWikiException(0, 0, "wrapper", exc);
    }
  }

  /**
   * @param accountName
   * @param guestPlus.
   *          if user is XWiki.XWikiGuest and guestPlus is true the account
   *          XWiki.XWikiGuestPlus will be used to get the token.
   * @param context
   * @param minutesValid
   * @return token (or null if token can not be generated)
   * @throws XWikiException
   */
  public String getNewCelementsTokenForUserWithAuthentication(String accountName, Boolean guestPlus,
      int minutesValid, XWikiContext context) throws XWikiException {
    accountName = authenticateForRequest(accountName, context);
    return getNewCelementsTokenForUser(accountName, guestPlus, minutesValid, context);
  }

  /**
   * @param accountName
   * @param guestPlus.
   *          if user is XWiki.XWikiGuest and guestPlus is true the account
   *          XWiki.XWikiGuestPlus will be used to get the token.
   * @param context
   * @return token (or null if token can not be generated)
   * @throws XWikiException
   */
  public String getNewCelementsTokenForUserWithAuthentication(String accountName, Boolean guestPlus,
      XWikiContext context) throws XWikiException {
    accountName = authenticateForRequest(accountName, context);
    return getNewCelementsTokenForUser(accountName, guestPlus, context);
  }

  private String authenticateForRequest(String accountName, XWikiContext context)
      throws XWikiException {
    if (!"".equals(context.getRequest().getParameter("j_username")) && !"".equals(
        context.getRequest().getParameter("j_password"))) {
      LOGGER.info("getNewCelementsTokenForUser: trying to authenticate  "
          + context.getRequest().getParameter("j_username"));
      Principal principal = context.getWiki().getAuthService().authenticate(
          context.getRequest().getParameter("j_username"), context.getRequest().getParameter(
              "j_password"), context);
      if (principal != null) {
        LOGGER.info("getNewCelementsTokenForUser: successfully autenthicated "
            + principal.getName());
        context.setUser(principal.getName());
        accountName = principal.getName();
      }
    }
    return accountName;
  }

  IAuthenticationServiceRole getAuthService() {
    return Utils.getComponent(IAuthenticationServiceRole.class);
  }

  UserService getUserService() {
    return Utils.getComponent(UserService.class);
  }

  IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

  ModelContext getContext() {
    return Utils.getComponent(ModelContext.class);
  }

}
