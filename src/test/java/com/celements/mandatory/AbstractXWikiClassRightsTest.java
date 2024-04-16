package com.celements.mandatory;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

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
import com.celements.model.reference.RefBuilder;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.xpn.xwiki.XWikiConstant;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class AbstractXWikiClassRightsTest extends AbstractComponentTest {

  private TestXWikiClassRights xwikiClassRights;
  private WikiReference wiki = new WikiReference("testwiki");

  @Before
  public void setup_AbstractXWikiClassRightsTest() throws Exception {
    xwikiClassRights = getBeanFactory().getBean(TestXWikiClassRights.class);
  }

  @Test
  public void test_checkRightsObject_changes() throws XWikiException {
    DocumentReference docRef = new RefBuilder().wiki("testwiki").space(XWikiConstant.XWIKI_SPACE)
        .doc("TestXWikiClass").build(DocumentReference.class);
    XWikiDocument classDoc = new XWikiDocument(docRef);
    expectClassWithNewObj(getRightsClass(), classDoc.getWikiRef());

    replayDefault();
    assertTrue(xwikiClassRights.checkRightsObject(classDoc));
    verifyDefault();

    List<BaseObject> rightsObj = XWikiObjectFetcher.on(classDoc)
        .filter(XWikiRightsClass.CLASS_REF)
        .list();
    assertEquals(1, rightsObj.size());
    assertEquals("XWikiAllGroup", getValue(rightsObj.get(0), XWikiRightsClass.FIELD_GROUPS).get(0));
    assertEquals(List.of(EAccessLevel.VIEW),
        getValue(rightsObj.get(0), XWikiRightsClass.FIELD_LEVELS));
    assertTrue(getValue(rightsObj.get(0), XWikiRightsClass.FIELD_ALLOW));
  }

  @Test
  public void test_checkRightsObject_noChanges() {
    // TODO implement test

  }

  private static <T> T getValue(BaseObject obj, ClassField<T> field) {
    return getFieldAccessor().get(obj, field).get();
  }

  @SuppressWarnings("unchecked")
  private static FieldAccessor<BaseObject> getFieldAccessor() {
    return Utils.getComponent(FieldAccessor.class, XObjectFieldAccessor.NAME);
  }

  private static BaseClass expectClassWithNewObj(ClassDefinition classDef, WikiReference wikiRef)
      throws XWikiException {
    BaseClass bClass = expectNewBaseObject(classDef.getDocRef(wikiRef));
    for (ClassField<?> field : classDef.getFields()) {
      expect(bClass.get(field.getName())).andReturn(field.getXField()).anyTimes();
    }
    return bClass;
  }

  private static ClassDefinition getRightsClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiRightsClass.CLASS_DEF_HINT);
  }

}
