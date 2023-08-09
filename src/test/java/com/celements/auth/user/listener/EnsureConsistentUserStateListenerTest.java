package com.celements.auth.user.listener;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.auth.user.UserPageType;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.XObjectFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class EnsureConsistentUserStateListenerTest
    extends AbstractComponentTest {

  private EnsureConsistentUserStateListener listener;
  private final DocumentReference userDocRef = new DocumentReference("xwikidb", "XWiki", "msladek");

  private static final String XWIKI_ADMIN_GROUP_FN = "XWiki.XWikiAdminGroup";

  @Before
  public void prepareTest() throws Exception {
    expectClass(getUserClass(), userDocRef.getWikiReference());
    listener = getBeanFactory().getBean(EnsureConsistentUserStateListener.class);
  }

  @Test
  public void test_setRightsOnUser() throws Exception {
    List<EAccessLevel> levels = Arrays.asList(EAccessLevel.VIEW, EAccessLevel.EDIT,
        EAccessLevel.DELETE);
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    expectClassWithNewObj(getRightsClass(), userDocRef.getWikiReference());

    replayDefault();
    listener.setRightsOnUser(userDoc, levels);
    verifyDefault();

    List<BaseObject> rightsObs = XWikiObjectFetcher.on(userDoc)
        .filter(XWikiRightsClass.CLASS_REF)
        .list();
    assertEquals(2, rightsObs.size());
    assertEquals("XWiki.msladek", getValue(rightsObs.get(0), XWikiRightsClass.FIELD_USERS).get(
        0).getUser());
    assertEquals(levels, getValue(rightsObs.get(0), XWikiRightsClass.FIELD_LEVELS));
    assertTrue(getValue(rightsObs.get(0), XWikiRightsClass.FIELD_ALLOW));
    assertEquals(XWIKI_ADMIN_GROUP_FN, getValue(rightsObs.get(1),
        XWikiRightsClass.FIELD_GROUPS).get(0));
    assertEquals(levels, getValue(rightsObs.get(1), XWikiRightsClass.FIELD_LEVELS));
    assertTrue(getValue(rightsObs.get(1), XWikiRightsClass.FIELD_ALLOW));
  }

  @Test
  public void test_setDefaultValuesOnNewUser() throws Exception {
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    BaseObject userObj = new BaseObject();
    userObj.setDocumentReference(userDoc.getDocumentReference());
    userObj.setXClassReference(XWikiUsersClass.CLASS_REF.getDocRef(
        userDoc.getDocumentReference().getWikiReference()));
    userDoc.addXObject(userObj);

    replayDefault();
    listener.setDefaultValuesOnNewUser(userDoc);
    verifyDefault();

    assertEquals(getUserClass().getDocRef(), userDoc.getParentReference());
    assertEquals("XWiki.msladek", userDoc.getCreator());
    assertEquals("XWiki.msladek", userDoc.getAuthor());
    assertEquals(24, XWikiObjectFetcher.on(userDoc)
        .fetchField(XWikiUsersClass.FIELD_PASSWORD)
        .findFirst()
        .get()
        .length());
  }

  @Test
  public void test_addPageTypeOnUser() throws XWikiException {
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    expectClassWithNewObj(getPageTypeClass(), userDoc.getWikiRef());

    replayDefault();
    listener.addPageTypeOnUser(userDoc);
    verifyDefault();

    assertEquals(UserPageType.PAGETYPE_NAME, XWikiObjectFetcher.on(userDoc)
        .filter(PageTypeClass.CLASS_REF)
        .fetchField(PageTypeClass.FIELD_PAGE_TYPE)
        .findFirst()
        .orElse(null));
  }

  private static ClassDefinition getRightsClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiRightsClass.CLASS_DEF_HINT);
  }

  private static ClassDefinition getUserClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiUsersClass.CLASS_DEF_HINT);
  }

  private static ClassDefinition getPageTypeClass() {
    return Utils.getComponent(ClassDefinition.class, PageTypeClass.CLASS_DEF_HINT);
  }

  private static BaseClass expectClassWithNewObj(ClassDefinition classDef, WikiReference wikiRef)
      throws XWikiException {
    BaseClass bClass = expectNewBaseObject(classDef.getDocRef(wikiRef));
    for (ClassField<?> field : classDef.getFields()) {
      expect(bClass.get(field.getName())).andReturn(field.getXField()).anyTimes();
    }
    return bClass;
  }

  private static <T> T getValue(BaseObject obj, ClassField<T> field) {
    return getFieldAccessor().get(obj, field).get();
  }

  private static BaseClass expectClass(ClassDefinition classDef, WikiReference wikiRef)
      throws XWikiException {
    BaseClass bClass = createBaseClassMock(classDef.getDocRef(wikiRef));
    for (ClassField<?> field : classDef.getFields()) {
      expect(bClass.get(field.getName())).andReturn(field.getXField()).anyTimes();
    }
    return bClass;
  }

  @SuppressWarnings("unchecked")
  private static FieldAccessor<BaseObject> getFieldAccessor() {
    return Utils.getComponent(FieldAccessor.class, XObjectFieldAccessor.NAME);
  }
}
