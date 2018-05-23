package com.celements.auth;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryException;

import com.celements.rights.access.IRightsAccessFacadeRole;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiUser;

@ComponentRole
public interface IAuthenticationServiceRole {

  public String getPasswordHash(String encoding, String str);

  public Map<String, String> activateAccount(String activationCode)
      throws AccountActivationFailedException;

  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins, Boolean noRedirect) throws XWikiException;

  /**
   * @deprecated since 3.0 instead use {@link IRightsAccessFacadeRole}
   */
  @Deprecated
  public boolean hasAccessLevel(String level, String userName, boolean isUser,
      DocumentReference docRef) throws XWikiException;

  /**
   * @return a unique alphanumeric validation key of length 24
   * @throws QueryException
   */
  @NotNull
  String getUniqueValidationKey() throws QueryException;

}
