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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.classes.PasswordClass;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.LDAP.XWikiLDAPAuthServiceImpl;

public class TokenLDAPAuthServiceImpl extends XWikiLDAPAuthServiceImpl {

  private static final Log mLogger = LogFactory.getFactory().getInstance(
      TokenLDAPAuthServiceImpl.class);

  @Override
  public XWikiUser checkAuth(XWikiContext context) throws XWikiException {
    if ((context.getResponse() != null)
        && !"".equals(context.getWiki().Param("celements.auth.P3P", ""))) {
      context.getResponse().addHeader("P3P", "CP=\"" + context.getWiki().Param(
          "celements.auth.P3P") + "\"");
    }
    if (context.getRequest() != null) {
      String token = context.getRequest().getParameter("token");
      String username = context.getRequest().getParameter("username");
      boolean hasToken = (token != null) && !"".equals(token);
      if (hasToken && (username != null) && !"".equals(username)) {
        mLogger.info("trying to authenticate user [" + username + "] with token ["
            + hasToken + "].");
          XWikiUser tokenUser = checkAuthByToken(username, token, context);
          if (tokenUser != null) {
            return tokenUser;
          }
      }
      mLogger.info("checkAuth for token skipped or failed. user [" + username
          + "] with token [" + hasToken + "].");
    }
    return super.checkAuth(context);
  }

  public XWikiUser checkAuthByToken(String loginname, String userToken,
      XWikiContext context) throws XWikiException {
    if ((loginname != null) && !"".equals(loginname)) {
      String usernameByToken = getUsernameForToken(userToken, context);
      if((usernameByToken != null) && !usernameByToken.equals("")
          && (usernameByToken.equals("XWiki." + loginname) 
          || usernameByToken.equals("xwiki:XWiki." + loginname))){
        mLogger.info("checkAuthByToken: user " + usernameByToken
            + " identified by userToken.");
        context.setUser(usernameByToken);
        return context.getXWikiUser();
      } else {
        mLogger.warn("checkAuthByToken: username could not be identified by token");
      }
    }
    return null;
  }

  String getUsernameForToken(String userToken, XWikiContext context
      ) throws XWikiException{

    String hashedCode = encryptString("hash:SHA-512:", userToken);
    String userDoc = "";
    
    if((userToken != null) && (userToken.trim().length() > 0)){
      
      String hql = ", BaseObject as obj, Classes.TokenClass as token where ";
      hql += "doc.space='XWiki' ";
      hql += "and obj.name=doc.fullName ";
      hql += "and token.tokenvalue=? ";
      hql += "and token.validuntil>=? ";
      hql += "and obj.id=token.id ";
      
      List<Object> parameterList = new Vector<Object>();
      parameterList.add(hashedCode);
      parameterList.add(new Date());
      
      XWikiStoreInterface storage = context.getWiki().getStore();
      List<String> users = storage.searchDocumentsNames(hql, 0, 0, parameterList, context);
      mLogger.info("searching token and found " + users.size() + " with parameters " + 
          Arrays.deepToString(parameterList.toArray()));
      if(users == null || users.size() == 0) {
        String db = context.getDatabase();
        context.setDatabase("xwiki");
        users = storage.searchDocumentsNames(hql, 0, 0, parameterList, context);
        context.setDatabase(db);
      }
      int usersFound = 0;
      for (String tmpUserDoc : users) {
        if(!tmpUserDoc.trim().equals("")) {
          usersFound++;
          userDoc = tmpUserDoc;
        }
      }
      if(usersFound > 1){
        mLogger.warn("Found more than one user for token '" + userToken + "'");
        return null;
      }
    } else {
      mLogger.warn("No valid token given");
    }    
    return userDoc;
  }

  String encryptString(String encoding, String str) {
    return new PasswordClass().getEquivalentPassword(encoding, str);
  }
  
}
