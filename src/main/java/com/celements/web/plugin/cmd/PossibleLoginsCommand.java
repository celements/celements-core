package com.celements.web.plugin.cmd;

import com.celements.auth.user.UserService;
import com.google.common.base.Joiner;
import com.xpn.xwiki.web.Utils;

@Deprecated
public class PossibleLoginsCommand {

  /**
   * @deprecated instead use {@link UserService#getPossibleLoginFields()}
   */
  @Deprecated
  public String getPossibleLogins() {
    return Joiner.on(',').join(getUserService().getPossibleLoginFields());
  }

  private UserService getUserService() {
    return Utils.getComponent(UserService.class);
  }

}
