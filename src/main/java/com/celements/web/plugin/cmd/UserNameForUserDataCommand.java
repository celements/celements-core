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

import org.mutabilitydetector.internal.com.google.common.base.Splitter;
import org.xwiki.query.QueryException;

import com.celements.web.UserService;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.Utils;

@Deprecated
public class UserNameForUserDataCommand {

  /**
   * @deprecated instead use {@link UserService#getUserForData(String, java.util.List)}
   */
  @Deprecated
  public String getUsernameForUserData(String login, String possibleLogins, XWikiContext context)
      throws XWikiException {
    try {
      Optional<XWikiUser> user = getUserService().getUserForData(login, Splitter.on(
          ",").omitEmptyStrings().splitToList(possibleLogins));
      if (user.isPresent()) {
        return user.get().getUser();
      }
      return "";
    } catch (QueryException exc) {
      throw new XWikiException(0, 0, "wrapper", exc);
    }
  }

  private UserService getUserService() {
    return Utils.getComponent(UserService.class);
  }

}
