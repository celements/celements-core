package com.celements.web.service;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.auth.user.User;
import com.celements.auth.user.UserCreateException;
import com.celements.auth.user.UserService;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiUser;

@ComponentRole
public interface ICelementsWebServiceRole {

  /**
   * @deprecated since 3.0 instead use {@link UserService#getUser(DocumentReference)} and
   *             {@link User#getEmail()}
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

  /**
   * @deprecated since 3.1, only intended for internal usage
   */
  @Deprecated
  public String encodeUrlToUtf8(String urlStr);
}
