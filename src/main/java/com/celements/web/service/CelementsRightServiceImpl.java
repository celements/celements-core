package com.celements.web.service;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiRightNotFoundException;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;

public class CelementsRightServiceImpl extends XWikiRightServiceImpl {
  @Override
  public boolean checkRight(String userOrGroupName, XWikiDocument doc, String accessLevel, boolean user,
      boolean allow, boolean global, XWikiContext context) throws XWikiRightNotFoundException, XWikiException {
    if("yep".equals(context.getRequest().get("allowed"))) {
      return super.checkRight(userOrGroupName, doc, accessLevel, user, allow, global, context);
    }
    return false;
  }
}
