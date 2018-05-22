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

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.auth.user.User;
import com.celements.auth.user.UserInstantiationException;
import com.celements.auth.user.UserService;
import com.celements.sajson.Builder;
import com.celements.web.classcollections.OldCoreClasses;
import com.celements.web.plugin.CelementsWebPlugin;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class RemoteUserValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteUserValidator.class);

  private CelementsWebPlugin injectedCelementsWeb;

  public String isValidUserJSON(String username, String password, String memberOfGroup,
      List<String> returnGroups, XWikiContext context) {
    String resultJSON = getResultJSON(null, false, "access_denied", null, context);
    if (validationAllowed(context)) {
      Principal principal = null;
      try {
        Optional<User> user = getUserService().getUserForData(username);
        if (user.isPresent()) {
          principal = context.getWiki().getAuthService().authenticate(
              user.get().asXWikiUser().getUser(), password, context);
        }
      } catch (XWikiException exc) {
        LOGGER.error("DENYING access: Exception while authenticating.", exc);
      }
      if (principal != null) {
        LOGGER.debug("Authentication successful, now checking group");
        if (context.getWiki().getUser(principal.getName(), context).isUserInGroup(memberOfGroup)) {
          LOGGER.debug("Group matched requirement, now checking if account is active.");
          if (checkActive(principal.getName(), context)) {
            LOGGER.debug("GRANTING access to " + principal.getName() + "!");
            resultJSON = getResultJSON(username, true, null, returnGroups, context);
          } else {
            LOGGER.warn("DENYING access: account '" + username + "' is inactive.");
            resultJSON = getResultJSON(null, false, "useraccount_inactive", null, context);
          }
        } else {
          LOGGER.warn("DENYING access: user is not in group '" + memberOfGroup + "'.");
          resultJSON = getResultJSON(null, false, "user_not_in_group", null, context);
        }
      } else {
        LOGGER.warn("DENYING access: authentication failed");
        resultJSON = getResultJSON(null, false, "wrong_username_password", null, context);
      }
    }
    return resultJSON;
  }

  private CelementsWebPlugin getCelementsweb(XWikiContext context) {
    if (injectedCelementsWeb != null) {
      return injectedCelementsWeb;
    }
    return (CelementsWebPlugin) context.getWiki().getPlugin("celementsweb", context);
  }

  // For Testing
  void injectCelementsWeb(CelementsWebPlugin celementsweb) {
    injectedCelementsWeb = celementsweb;
  }

  // Access can only be true if there is no error message. When there is an error, access
  // is always false.
  String getResultJSON(String username, boolean access, String errorMsg, List<String> returnGroups,
      XWikiContext context) {
    Builder jsonBuilder = new Builder();
    jsonBuilder.openDictionary();
    if (hasValue(errorMsg)) {
      jsonBuilder.addStringProperty("access", "false");
      jsonBuilder.addStringProperty("error", errorMsg);
    } else if (hasValue(username)) {
      jsonBuilder.addStringProperty("access", access + "");
      jsonBuilder.addStringProperty("username", username);
      jsonBuilder.openProperty("group_membership");
      jsonBuilder.openDictionary();
      if (returnGroups != null) {
        for (String group : returnGroups) {
          String groupName = group.substring(group.lastIndexOf(".") + 1);
          groupName = context.getMessageTool().get("cel_groupname_" + groupName);
          jsonBuilder.addStringProperty(groupName, isGroupMember(username, group, context));
        }
      }
      jsonBuilder.closeDictionary();
    }
    jsonBuilder.closeDictionary();
    return jsonBuilder.getJSON();
  }

  String isGroupMember(String username, String group, XWikiContext context) {
    boolean inGroup = false;
    String userDocName = null;
    if ((group != null) && !group.contains(":") && !group.equals("XWiki.XWikiAllGroup")) {
      try {
        userDocName = getCelementsweb(context).getUsernameForUserData(username,
            context.getWiki().getXWikiPreference(OldCoreClasses.XWIKI_PREFERENCES_CELLOGIN_PROPERTY,
                "loginname", context), context);
      } catch (XWikiException e) {
        LOGGER.error("Could not get user for username '" + username + "'.");
      }
      if ((userDocName != null) && (userDocName.trim().length() > 0)) {
        inGroup = context.getWiki().getUser(userDocName, context).isUserInGroup(group);
      }
    }
    return String.valueOf(inGroup);
  }

  boolean validationAllowed(XWikiContext context) {
    String requestHost = context.getRequest().getHttpServletRequest().getRemoteHost();
    if ((requestHost != null) && (requestHost.trim().length() > 0)) {
      BaseObject config = context.getDoc().getObject("Classes.RemoteUserValidationClass", "host",
          requestHost, false);
      if (config != null) {
        String serverSecret = config.getStringValue("secret");
        String requestSecret = context.getRequest().get("secret");
        if ((serverSecret != null) && (serverSecret.trim().length() > 0)
            && serverSecret.trim().equals(requestSecret.trim())) {
          LOGGER.debug("ALLOWING validation for host " + requestHost + " with secret "
              + requestSecret);
          return true;
        } else {
          LOGGER.warn("DENYING validation: Server secret '" + requestSecret + "' does "
              + "not match expectation!");
        }
      } else {
        LOGGER.warn("DENYING validation: No configuration object found for host '" + requestHost
            + "'.");
      }
    } else {
      LOGGER.warn("DENYING validation: Received no requester host!");
    }
    if (context.getUser().startsWith("xwiki:")) {
      LOGGER.warn("ALLOWING validation for SUPERADMIN.");
      return true;
    }
    return false;
  }

  boolean hasValue(String value) {
    return (value != null) && (value.trim().length() > 0);
  }

  // Copy & Paste and customise from bugged method in XWiki class.
  private boolean checkActive(String accountName, XWikiContext context) {
    boolean active;
    // These users are necessarly active
    if (accountName.equals(XWikiRightService.GUEST_USER_FULLNAME) || (accountName.equals(
        XWikiRightService.SUPERADMIN_USER_FULLNAME))) {
      active = true;
    } else {
      try {
        User user = getUserService().getUser(getUserService().completeUserDocRef(accountName));
        String checkactivefield = context.getWiki().getXWikiPreference("auth_active_check",
            context);
        if (checkactivefield.equals("1")) {
          active = user.isActive();
        } else {
          LOGGER.warn("XWikiPreferences field auth_active_check != 1 which means all users"
              + "are always handled as active");
          active = true;
        }
      } catch (UserInstantiationException exc) {
        LOGGER.warn("checkActive - failed '{}'", exc);
        active = false;
      }
    }
    return active;
  }

  private UserService getUserService() {
    return Utils.getComponent(UserService.class);
  }

}
