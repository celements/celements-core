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
import com.celements.auth.user.UserService;
import com.celements.model.util.ModelUtils;
import com.celements.sajson.Builder;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class RemoteUserValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteUserValidator.class);

  public String isValidUserJSON(String username, String password, String memberOfGroup,
      List<String> returnGroups, XWikiContext context) {
    String resultJSON = getErrorJSON("access_denied");
    if (validationAllowed(context)) {
      try {
        Optional<User> user = getUserService().getUserForData(username);
        if (user.isPresent() && authenticate(user.get(), password, context)) {
          LOGGER.debug("Authentication successful, now checking group");
          if (user.get().asXWikiUser().isUserInGroup(memberOfGroup, context)) {
            LOGGER.debug("Group matched requirement, now checking if account is active.");
            if (checkActive(user.get(), context)) {
              LOGGER.debug("GRANTING access to " + user.get() + "!");
              resultJSON = getResultJSON(user.get(), username, returnGroups, context);
            } else {
              LOGGER.warn("DENYING access: account '" + username + "' is inactive.");
              resultJSON = getErrorJSON("useraccount_inactive");
            }
          } else {
            LOGGER.warn("DENYING access: user is not in group '" + memberOfGroup + "'.");
            resultJSON = getErrorJSON("user_not_in_group");
          }
        } else {
          LOGGER.warn("DENYING access: authentication failed");
          resultJSON = getErrorJSON("wrong_username_password");
        }
      } catch (XWikiException exc) {
        LOGGER.error("DENYING access: Exception while authenticating.", exc);
      }
    }
    return resultJSON;
  }

  private boolean authenticate(User user, String password, XWikiContext context)
      throws XWikiException {
    Principal principal = context.getWiki().getAuthService().authenticate(
        getModelUtils().serializeRefLocal(user.getDocRef()), password, context);
    return (principal != null) && user.getDocRef().equals(getModelUtils().resolveRef(
        principal.getName()));
  }

  String getErrorJSON(String errorMsg) {
    Builder jsonBuilder = new Builder();
    jsonBuilder.openDictionary();
    jsonBuilder.addStringProperty("access", "false");
    jsonBuilder.addStringProperty("error", errorMsg);
    jsonBuilder.closeDictionary();
    return jsonBuilder.getJSON();
  }

  String getResultJSON(User user, String username, List<String> returnGroups,
      XWikiContext context) {
    Builder jsonBuilder = new Builder();
    jsonBuilder.openDictionary();
    jsonBuilder.addStringProperty("access", "true");
    jsonBuilder.addStringProperty("username", username);
    jsonBuilder.openProperty("group_membership");
    jsonBuilder.openDictionary();
    if (returnGroups != null) {
      for (String group : returnGroups) {
        String groupName = group.substring(group.lastIndexOf(".") + 1);
        groupName = context.getMessageTool().get("cel_groupname_" + groupName);
        jsonBuilder.addStringProperty(groupName, isGroupMember(user, group, context));
      }
    }
    jsonBuilder.closeDictionary();
    jsonBuilder.closeDictionary();
    return jsonBuilder.getJSON();
  }

  String isGroupMember(User user, String group, XWikiContext context) {
    boolean inGroup = false;
    if ((user != null) && !Strings.nullToEmpty(group).isEmpty() && !group.contains(":")
        && !group.equals("XWiki.XWikiAllGroup")) {
      try {
        inGroup = user.asXWikiUser().isUserInGroup(group, context);
      } catch (XWikiException xwe) {
        LOGGER.error("Could not check group '{}' for user '{}'", group, user, xwe);
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

  // Copy & Paste and customise from bugged method in XWiki class.
  private boolean checkActive(User user, XWikiContext context) {
    boolean active;
    // These users are necessarly active
    String accountName = getModelUtils().serializeRefLocal(user.getDocRef());
    if (accountName.equals(XWikiRightService.GUEST_USER_FULLNAME) || (accountName.equals(
        XWikiRightService.SUPERADMIN_USER_FULLNAME))) {
      active = true;
    } else {
      String checkactivefield = context.getWiki().getXWikiPreference("auth_active_check", context);
      if (checkactivefield.equals("1")) {
        active = user.isActive();
      } else {
        LOGGER.warn("XWikiPreferences field auth_active_check != 1 which means all users"
            + "are always handled as active");
        active = true;
      }
    }
    return active;
  }

  private UserService getUserService() {
    return Utils.getComponent(UserService.class);
  }

  private ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
