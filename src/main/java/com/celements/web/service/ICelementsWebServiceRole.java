package com.celements.web.service;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.UserCreateException;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiUser;

@ComponentRole
public interface ICelementsWebServiceRole {

  public String getEmailAdressForUser(DocumentReference userDocRef);

  public int createUser(boolean validate) throws XWikiException;

  /**
   * @deprecated since 1.139 instead use createNewUser((Map<String, String>, String,
   *             boolean)
   */
  @Deprecated
  public int createUser(Map<String, String> userData, String possibleLogins, boolean validate)
      throws XWikiException;

  public XWikiUser createNewUser(Map<String, String> userData, String possibleLogins,
      boolean validate) throws UserCreateException;

  public Map<String, String> getUniqueNameValueRequestMap();

  public List<String> getSupportedAdminLanguages();

  public void setSupportedAdminLanguages(List<String> supportedAdminLangList);

  public boolean writeUTF8Response(String filename, String renderDocFullName);
}
