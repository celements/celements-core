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
package com.celements.web.service;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.WikiReference;

import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.XWikiMessageTool;

@Component
public class WebUtilsService implements IWebUtilsService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(WebUtils.class);

  @Requirement
  Execution execution;

  @Requirement
  EntityReferenceResolver<String> referenceResolver;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public XWikiMessageTool getMessageTool(String adminLanguage) {
    if(adminLanguage != null) {
      if((getContext().getLanguage() != null) && getContext().getLanguage().equals(
          adminLanguage)) {
        return getContext().getMessageTool();
      } else {
        Locale locale = new Locale(adminLanguage);
        ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources",
            locale);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("ApplicationResources");
        }
        XWikiContext adminContext = (XWikiContext) getContext().clone();
        adminContext.putAll(getContext());
        adminContext.setLanguage(adminLanguage);
        return new XWikiMessageTool(bundle, adminContext);
      }
    } else {
      return null;
    }
  }

  public XWikiMessageTool getAdminMessageTool() {
    return getMessageTool(getAdminLanguage());
  }
  
  public String getAdminLanguage() {
    return getAdminLanguage(getContext().getUser());
  }

  public String getAdminLanguage(String userFullName) {
    String adminLanguage = null;
    try {
      DocumentReference userDocRef = resolveDocumentReference(userFullName);
      DocumentReference xwikiUsersClassRef = new DocumentReference(
          getContext().getDatabase(), "XWiki", "XWikiUsers");
      xwikiUsersClassRef.setWikiReference(userDocRef.getWikiReference());
      BaseObject userObj = getContext().getWiki().getDocument(userDocRef, getContext()
          ).getXObject(xwikiUsersClassRef);
      if (userObj != null) {
        adminLanguage = userObj.getStringValue("admin_language");
      }
    } catch (XWikiException e) {
      LOGGER.error("failed to get UserObject for " + getContext().getUser());
    }
    if ((adminLanguage == null) || ("".equals(adminLanguage))) {
      adminLanguage = getContext().getWiki().getWebPreference("admin_language",
          getContext().getLanguage(), getContext());
    }
    return adminLanguage;
  }

  public DocumentReference resolveDocumentReference(String fullName) {
    DocumentReference eventRef = new DocumentReference(referenceResolver.resolve(
        fullName, EntityType.DOCUMENT));
    if (!fullName.contains(":")) {
      eventRef.setWikiReference(new WikiReference(getContext().getDatabase()));
    }
    LOGGER.debug("getDocRefFromFullName: for [" + fullName + "] got reference ["
        + eventRef + "].");
    return eventRef;
  }

  public String getDefaultLanguage() {
    return getContext().getWiki().getWebPreference("default_language", getContext());
  }

  public boolean isAdminUser() {
    try {
      if ((getContext().getXWikiUser() != null)
          && (getContext().getWiki().getRightService() != null)
          && (getContext().getDoc() != null)) {
        return (getContext().getWiki().getRightService().hasAdminRights(getContext())
          || getContext().getXWikiUser().isUserInGroup("XWiki.XWikiAdminGroup",
              getContext()));
      } else {
        return false;
      }
    } catch (XWikiException e) {
      LOGGER.error("Cannot determin if user has Admin Rights therefore guess"
        + " no (false).", e);
      return false;
    }
  }

  public boolean isAdvancedAdmin() {
    String user = getContext().getUser();
    try {
      XWikiDocument userDoc = getContext().getWiki().getDocument(resolveDocumentReference(
          user), getContext());
      BaseObject userObj = userDoc.getXObject(new DocumentReference(getContext(
          ).getOriginalDatabase(), "XWiki", "XWikiUsers"));
      return (isAdminUser() && (user.startsWith("xwiki:")
          || ((userObj != null) && "Advanced".equals(userObj.getStringValue("usertype"
              )))));
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get user document for [" + user + "].", exp);
    }
    return false;
  }

}
