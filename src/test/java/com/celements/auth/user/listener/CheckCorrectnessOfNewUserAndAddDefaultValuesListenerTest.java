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

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.XObjectFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class CheckCorrectnessOfNewUserAndAddDefaultValuesListenerTest
    extends AbstractComponentTest {

  private CheckCorrectnessOfNewUserAndAddDefaultValuesListener listener;
  private final DocumentReference userDocRef = new DocumentReference("xwikidb", "XWiki", "msladek");

  private static final String XWIKI_ADMIN_GROUP_FN = "XWiki.XWikiAdminGroup";

  @Before
  public void prepareTest() {
    listener = getBeanFactory().getBean(CheckCorrectnessOfNewUserAndAddDefaultValuesListener.class);
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

    List<BaseObject> rightsObs = XWikiObjectFetcher.on(userDoc).filter(getRightsClass()).list();
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

    replayDefault();
    listener.setDefaultValuesOnNewUser(userDoc);
    verifyDefault();

    assertEquals(getUserClass().getDocRef(), userDoc.getParentReference());
    assertEquals("XWiki.msladek", userDoc.getCreator());
    assertEquals("XWiki.msladek", userDoc.getAuthor());
    assertEquals("#includeForm(\"XWiki.XWikiUserSheet\")", userDoc.getContent());
    assertEquals(1, XWikiObjectFetcher.on(userDoc).filter(getUserClass()).count());
    assertEquals(24, XWikiObjectFetcher.on(userDoc).filter(getUserClass())
        .filterPresent(XWikiUsersClass.FIELD_PASSWORD).toString().length());
  }

  private static ClassDefinition getRightsClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiRightsClass.CLASS_DEF_HINT);
  }

  private static ClassDefinition getUserClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiUsersClass.CLASS_DEF_HINT);
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

  @SuppressWarnings("unchecked")
  private static FieldAccessor<BaseObject> getFieldAccessor() {
    return Utils.getComponent(FieldAccessor.class, XObjectFieldAccessor.NAME);
  }
}
