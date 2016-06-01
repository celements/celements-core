package com.celements.auth;

import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiUser;

@ComponentRole
public interface IAuthenticationServiceRole {

  public String getPasswordHash(String encoding, String str);

  public Map<String, String> activateAccount(String activationCode) throws XWikiException;

  public XWikiUser checkAuth(String logincredential, String password, String rememberme,
      String possibleLogins, Boolean noRedirect) throws XWikiException;

  public boolean hasAccessLevel(String level, String user, boolean isUser, DocumentReference docRef)
      throws XWikiException;

}
