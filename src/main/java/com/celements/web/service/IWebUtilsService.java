package com.celements.web.service;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.web.XWikiMessageTool;

@ComponentRole
public interface IWebUtilsService {

  public XWikiMessageTool getMessageTool(String adminLanguage);

  public XWikiMessageTool getAdminMessageTool();

  public String getAdminLanguage();

  public String getAdminLanguage(String userFullName);

}
