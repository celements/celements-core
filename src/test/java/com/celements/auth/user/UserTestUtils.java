package com.celements.auth.user;

import com.celements.model.classes.ClassDefinition;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class UserTestUtils {

  public static BaseObject addUserObj(XWikiDocument userDoc) {
    BaseObject userObj = new BaseObject();
    userObj.setDocumentReference(userDoc.getDocumentReference());
    userObj.setXClassReference(getUserClass().getClassReference().getDocRef(
        userDoc.getDocumentReference().getWikiReference()));
    userObj.setIntValue(XWikiUsersClass.FIELD_SUSPENDED.getName(), 0);
    userDoc.addXObject(userObj);
    return userObj;
  }

  public static ClassDefinition getUserClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiUsersClass.CLASS_DEF_HINT);
  }

}
