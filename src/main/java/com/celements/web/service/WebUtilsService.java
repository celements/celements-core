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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

import com.celements.web.comparators.BaseObjectComparator;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.XWikiMessageTool;

@Component
public class WebUtilsService implements IWebUtilsService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(WebUtilsService.class);

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
    LOGGER.trace("isAdvancedAdmin: user [" + user + "] db [" + getContext().getDatabase()
        + "].");
    try {
      XWikiDocument userDoc = getContext().getWiki().getDocument(resolveDocumentReference(
          user), getContext());
      BaseObject userObj = userDoc.getXObject(resolveDocumentReference(
          "XWiki.XWikiUsers"));
      boolean isAdvancedAdmin = isAdminUser() && (user.startsWith("xwiki:")
          || ((userObj != null) && "Advanced".equals(userObj.getStringValue("usertype"
              ))));
      LOGGER.debug("isAdvancedAdmin: admin [" + isAdminUser() + "] global user ["
          + user.startsWith("xwiki:") + "] usertype [" + ((userObj != null
          ) ? userObj.getStringValue("usertype") : "null") + "] returning ["
          + isAdvancedAdmin + "] db [" + getContext().getDatabase() + "].");
      return isAdvancedAdmin;
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get user document for [" + user + "].", exp);
    }
    return false;
  }

  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField, boolean asc) {
    return getObjectsOrdered(doc, classRef, orderField, asc, null, false);
  }

  /**
   * Get a list of Objects for a Document sorted by one or two fields.
   * 
   * @param doc The Document where the Objects are attached.
   * @param classRef The reference to the class of the Objects to return
   * @param orderField1 Field to order the objects by. First priority.
   * @param asc1 Order first priority ascending or descending.
   * @param orderField2 Field to order the objects by. Second priority.
   * @param asc2 Order second priority ascending or descending.
   * @return List of objects ordered as specified
   */
  public List<BaseObject> getObjectsOrdered(XWikiDocument doc, DocumentReference classRef,
      String orderField1, boolean asc1, String orderField2, boolean asc2) {
    List<BaseObject> resultList = new ArrayList<BaseObject>();
    if(doc != null) {
      List<BaseObject> allObjects = doc.getXObjects(classRef);
      if(allObjects != null) {
        for (BaseObject obj : allObjects) {
          if(obj != null) {
            resultList.add(obj);
          }
        }
      }
      Collections.sort(resultList, new BaseObjectComparator(orderField1, asc1, 
          orderField2, asc2));
    }
    return resultList;
  }
}
