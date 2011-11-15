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

import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class NewCelementsTokenForUserCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      NewCelementsTokenForUserCommand.class);

  /**
   * 
   * @param accountName
   * @param guestPlus. if user is XWiki.XWikiGuest and guestPlus is true the account
   * XWiki.XWikiGuestPlus will be used to get the token.
   * @param context
   * @return token (or null if token can not be generated)
   * @throws XWikiException
   */
  public String getNewCelementsTokenForUser(String accountName,
      Boolean guestPlus, XWikiContext context) throws XWikiException {
    mLogger.debug("getNewCelementsTokenForUser: with guestPlus [" + guestPlus
        + "] for account [" + accountName + "].");
    String validkey = null;
    if (guestPlus && "XWiki.XWikiGuest".equals(accountName)) {
      accountName = "XWiki.XWikiGuestPlus";
    }
    XWikiDocument doc1 = context.getWiki().getDocument(accountName, context);
    if (context.getWiki().exists(accountName, context)
        && (doc1.getObject("XWiki.XWikiUsers") != null)) {
      validkey = getUniqueValidationKey(context);
      BaseObject obj = doc1.newObject("Classes.TokenClass", context);
      obj.set("tokenvalue", validkey, context);
      Calendar myCal = Calendar.getInstance();
      myCal.add(Calendar.DAY_OF_YEAR, 1);
      obj.set("validuntil", myCal.getTime(), context);
      context.getWiki().saveDocument(doc1, context);
      mLogger.debug("getNewCelementsTokenForUser: sucessfully created token for account ["
          + accountName + "].");
    }
    return validkey;
  }

  public String getUniqueValidationKey(XWikiContext context)
      throws XWikiException {
    XWikiStoreInterface storage = context.getWiki().getStore();
    
    String hql = "select str.value from BaseObject as obj, StringProperty as str ";
    hql += "where obj.className='XWiki.XWikiUsers' ";
    hql += "and obj.id=str.id.id ";
    hql += "and str.id.name='validkey' ";
    hql += "and str.value<>''";
    List<String> existingKeys = storage.search(hql, 0, 0, context);
    
    String validkey = "";
    while(validkey.equals("") || existingKeys.contains(validkey)){
      validkey = context.getWiki().generateRandomString(24);
    }
    
    return validkey;
  }

}
