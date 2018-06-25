package com.celements.web.service;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.auth.user.UserCreateException;
import com.celements.auth.user.UserService;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiUser;

@ComponentRole
public interface ICelementsWebServiceRole {

  /**
   * @deprecated since 3.0 instead use {@link UserService#getEmailForUser(DocumentReference)}
   */
  @Deprecated
  public String getEmailAdressForUser(DocumentReference userDocRef);

  /**
   * @deprecated since 3.0 instead use {@link UserService#createNewUser(Map, boolean)}
   */
  @Deprecated
  public int createUser(boolean validate) throws XWikiException;

  /**
   * @deprecated since 1.139 instead use {@link UserService#createNewUser(Map, boolean)}
   */
  @Deprecated
  public int createUser(Map<String, String> userData, String possibleLogins, boolean validate)
      throws XWikiException;

  /**
   * @deprecated since 3.0 instead use {@link UserService#createNewUser(Map, boolean)}
   */
  @Deprecated
  public XWikiUser createNewUser(Map<String, String> userData, String possibleLogins,
      boolean validate) throws UserCreateException;

  public Map<String, String> getUniqueNameValueRequestMap();

  public List<String> getSupportedAdminLanguages();

  public void setSupportedAdminLanguages(List<String> supportedAdminLangList);

  public boolean writeUTF8Response(String filename, String renderDocFullName);

  public void sendRedirect(String urlStr);

  public String encodeUrlToUtf8(String urlStr);
}
