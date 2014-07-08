package com.celements.web.service;

import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiException;

@ComponentRole
public interface ICelementsWebServiceRole {
  
  public String getEmailAdressForUser(DocumentReference userDocRef);
  
  public int createUser(boolean validate) throws XWikiException;
  
  public int createUser(Map<String, String> userData, String possibleLogins,
      boolean validate) throws XWikiException;
  
  public Map<String, String> getUniqueNameValueRequestMap();
}
