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
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class NewCelementsTokenForUserCommand {

  QueryManager injected_queryManager;

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      NewCelementsTokenForUserCommand.class);

  /**
   * @param accountName
   * @param guestPlus.
   *          if user is XWiki.XWikiGuest and guestPlus is true the account
   *          XWiki.XWikiGuestPlus will be used to get the token.
   * @param context
   * @return token (or null if token can not be generated)
   * @throws XWikiException
   */
  public String getNewCelementsTokenForUser(String accountName, Boolean guestPlus,
      XWikiContext context) throws XWikiException {
    return getNewCelementsTokenForUser(accountName, guestPlus, 1440, context);
  }

  /**
   * @param accountName
   * @param guestPlus.
   *          if user is XWiki.XWikiGuest and guestPlus is true the account
   *          XWiki.XWikiGuestPlus will be used to get the token.
   * @param minutesValid
   *          how long should the token be valid in minutes.
   * @param context
   * @return token (or null if token can not be generated)
   * @throws XWikiException
   */
  public String getNewCelementsTokenForUser(String accountName, Boolean guestPlus, int minutesValid,
      XWikiContext context) throws XWikiException {
    LOGGER.debug("getNewCelementsTokenForUser: check for expired tokens.");
    LOGGER.debug("getNewCelementsTokenForUser: with guestPlus [" + guestPlus + "] for account ["
        + accountName + "].");
    String validkey = null;
    if (guestPlus && "XWiki.XWikiGuest".equals(accountName)) {
      accountName = "XWiki.XWikiGuestPlus";
    }
    XWikiDocument doc1 = context.getWiki().getDocument(accountName, context);
    if (context.getWiki().exists(accountName, context) && (doc1.getObject(
        "XWiki.XWikiUsers") != null)) {
      removeOutdatedTokens(doc1);
      validkey = getUniqueValidationKey(context);
      BaseObject obj = doc1.newObject("Classes.TokenClass", context);
      obj.set("tokenvalue", validkey, context);
      Calendar myCal = Calendar.getInstance();
      myCal.add(Calendar.MINUTE, minutesValid);
      obj.set("validuntil", myCal.getTime(), context);
      context.getWiki().saveDocument(doc1, context);
      LOGGER.debug("getNewCelementsTokenForUser: sucessfully created token for account ["
          + accountName + "].");
    }
    return validkey;
  }

  synchronized void removeOutdatedTokens(XWikiDocument userDoc) {
    String xwql = "select obj.number "
        + "from Document as doc, doc.object(Classes.TokenClass) as obj "
        + "where doc.fullName = :doc and obj.validuntil < :now order by obj.number desc";
    try {
      DocumentReference docRef = userDoc.getDocumentReference();
      Query query = getQueryManager().createQuery(xwql, Query.XWQL);
      query.bindValue("now", new Date());
      query.bindValue("doc", getWebUtilsService().getRefLocalSerializer().serialize(docRef));
      query.setWiki(docRef.getLastSpaceReference().getParent().getName());
      List<Object> tokenResults = query.execute();
      LOGGER.trace("userDoc: " + userDoc);
      LOGGER.trace("Tokens to delete: " + tokenResults.size() + " in wiki "
          + docRef.getLastSpaceReference().getParent().getName());
      DocumentReference tokenClassRef = getTokenClassDocRef(new WikiReference(
          userDoc.getDocumentReference().getLastSpaceReference().getParent()));
      for (Object retNr : tokenResults) {
        int nr = Integer.parseInt(retNr.toString());
        BaseObject obj = userDoc.getXObject(tokenClassRef, nr);
        LOGGER.trace("deleting token " + nr + " of ref '" + tokenClassRef + "' obj is " + obj);
        if (obj != null) {
          userDoc.removeXObject(obj);
        }
      }
    } catch (QueryException qe) {
      LOGGER.error("Exception querying for outdated tokens with xwql [" + xwql + "]", qe);
    }
  }

  DocumentReference getTokenClassDocRef(WikiReference wikiRef) {
    return new DocumentReference(wikiRef.getName(), "Classes", "TokenClass");
  }

  QueryManager getQueryManager() {
    if (injected_queryManager != null) {
      return injected_queryManager;
    }
    return Utils.getComponent(QueryManager.class);
  }

  IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  /**
   * @deprecated instead use {@link #getUniqueValidationKey()}
   */
  @Deprecated
  public String getUniqueValidationKey(XWikiContext context) throws XWikiException {
    try {
      return getUniqueValidationKey();
    } catch (QueryException exc) {
      throw new XWikiException(0, 0, "wrapper", exc);
    }
  }

  public String getUniqueValidationKey() throws QueryException {
    String xwql = "select usr.validkey from Document doc, doc.object(XWiki.XWikiUsers) usr "
        + "where usr.validkey <> ''";
    Set<String> existingKeys = ImmutableSet.copyOf(getQueryManager().createQuery(xwql,
        Query.XWQL).<String>execute());
    String validkey;
    do {
      validkey = RandomStringUtils.randomAlphanumeric(24);
    } while (existingKeys.contains(validkey));
    return validkey;
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

}
