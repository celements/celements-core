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
package com.celements.web.plugin.cmd;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.EntityType;
import org.xwiki.model.internal.reference.DefaultStringEntityReferenceResolver;
import org.xwiki.model.internal.reference.DefaultStringEntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

public class UserNameForUserDataCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      UserNameForUserDataCommand.class);

  public String getUsernameForUserData(String login, String possibleLogins,
      XWikiContext context) throws XWikiException{
    String userDoc = "";
    
    if((login != null) && (login.trim().length() > 0)
        && (possibleLogins != null)){
      String[] fields = possibleLogins.split(",");
      
      String hql = ", BaseObject as obj, StringProperty as str where ";
      hql += "doc.space='XWiki' ";
      hql += "and obj.name=doc.fullName ";
      hql += "and obj.className='XWiki.XWikiUsers' ";
      hql += "and obj.id=str.id.id ";
      hql += "and (";
      for (int i = 0; i < fields.length; i++) {
        if(i > 0){ hql += "or "; }
        hql += "str.id.name='" + fields[i] + "' ";
      }
      hql += ") and lower(str.value)='" + login.toLowerCase().replace("'", "''") + "'";
      
      XWikiStoreInterface storage = context.getWiki().getStore();
      List<String> users = storage.searchDocumentsNames(hql, 0, 0, context);
      mLogger.info("searching users for " + login + " and found " + users.size());
      
      int usersFound = 0;
      for (String tmpUserDoc : users) {
        if(!tmpUserDoc.trim().equals("")) {
          usersFound++;
          userDoc = tmpUserDoc;
        }
      }
      if(usersFound > 1){
        mLogger.warn("Found more than one user for login '" + login
            + "' in possible fields '" + possibleLogins + "'");
        return null;
      }
      
      if((userDoc.trim().length() == 0)
          && (possibleLogins.matches("(.*,)?loginname(,.*)?"))){
        if(!login.startsWith("XWiki.")){
          login = "XWiki." + login;
        }
        if(context.getWiki().exists(login, context)){
          userDoc = login;
        } else {
          List<String> argsList = new ArrayList<String>();
          argsList.add(login.replaceAll("XWiki.", ""));
          List<XWikiDocument> docs = storage.searchDocuments("where lower(doc.name)=?", 
              argsList, context);
          if(docs.size() == 1) {
            userDoc = getRefSerializer().serialize(docs.get(0).getDocumentReference());
          } else if(docs.size() > 1) {
            return null;
          }
        }
      }
    }
    
    mLogger.info("Find login for '" + login + "' in fields: '"
        + possibleLogins + "' Result: '" + userDoc + "'");
    
    return userDoc;
  }
  
  private DefaultStringEntityReferenceSerializer getRefSerializer() {
    return (DefaultStringEntityReferenceSerializer) Utils.getComponent(
        EntityReferenceSerializer.class, "local");
  }
}
