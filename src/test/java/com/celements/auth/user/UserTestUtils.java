package com.celements.auth.user;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class UserTestUtils {

  public static XWikiDocument createAndExpectUserDoc(DocumentReference userDocRef)
      throws DocumentNotExistsException {
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    addUserObj(userDoc);
    expect(getMock(IModelAccessFacade.class).getDocument(userDocRef)).andReturn(userDoc).anyTimes();
    return userDoc;
  }

  public static BaseObject addUserObj(XWikiDocument userDoc) {
    BaseObject userObj = new BaseObject();
    userObj.setDocumentReference(userDoc.getDocumentReference());
    userObj.setXClassReference(getUserClass().getClassReference().getDocRef(
        userDoc.getDocumentReference().getWikiReference()));
    userDoc.addXObject(userObj);
    return userObj;
  }

  public static ClassDefinition getUserClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiUsersClass.CLASS_DEF_HINT);
  }

}
