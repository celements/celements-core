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
import com.celements.model.context.ModelContext;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.XObjectFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.classes.oldcore.XWikiGlobalRightsClass;
import com.xpn.xwiki.XWikiConstant;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class XWikiWebPreferencesTest extends AbstractComponentTest {

  private XWikiWebPreferences mandatoryWebPrefs;

  @Before
  public void prepareTest() {
    mandatoryWebPrefs = getBeanFactory().getBean(XWikiWebPreferences.class);

  }

  @Test
  public void test_checkSpaceLayout_changes() throws Exception {
    DocumentReference docRef = new RefBuilder().wiki(getXContext().getDatabase()).space("Content")
        .doc(ModelContext.WEB_PREF_DOC_NAME).build(DocumentReference.class);
    XWikiDocument webPrefDoc = new XWikiDocument(docRef);
    expectClassWithNewObj(getPageTypeClass(), docRef.getWikiReference());

    replayDefault();
    assertTrue(mandatoryWebPrefs.checkSpaceLayout(webPrefDoc));
    assertTrue(XWikiObjectFetcher.on(webPrefDoc).filter(PageTypeClass.CLASS_REF).filter(
        PageTypeClass.PAGE_LAYOUT, "SimpleLayout").exists());
    verifyDefault();
  }

  @Test
  public void test_checkSpaceLayout_noChanges() throws Exception {
    DocumentReference docRef = new RefBuilder().wiki(getXContext().getDatabase()).space("Content")
        .doc(ModelContext.WEB_PREF_DOC_NAME).build(DocumentReference.class);
    XWikiDocument webPrefDoc = new XWikiDocument(docRef);
    expectClassWithNewObj(getPageTypeClass(), docRef.getWikiReference());

    replayDefault();
    XWikiObjectEditor editor = XWikiObjectEditor.on(webPrefDoc).filter(PageTypeClass.CLASS_REF);
    editor.createFirstIfNotExists();
    editor.editField(PageTypeClass.PAGE_LAYOUT).first("SimpleLayout");
    assertFalse(mandatoryWebPrefs.checkSpaceLayout(webPrefDoc));
    verifyDefault();
  }

  @Test
  public void test_checkGlobalRightsObject_changes() throws Exception {
    DocumentReference docRef = new RefBuilder().wiki("testWiki").space(XWikiConstant.XWIKI_SPACE)
        .doc(XWikiConstant.WEB_PREF_DOC_NAME).build(DocumentReference.class);
    XWikiDocument webPrefDoc = new XWikiDocument(docRef);
    expectClassWithNewObj(getGlobalRightsClass(), webPrefDoc.getWikiRef());

    replayDefault();
    assertTrue(mandatoryWebPrefs.checkGlobalRightsObject(webPrefDoc));
    verifyDefault();

    List<BaseObject> rightsObj = XWikiObjectFetcher.on(webPrefDoc)
        .filter(XWikiGlobalRightsClass.CLASS_REF)
        .list();
    assertEquals(1, rightsObj.size());
    assertEquals("XWikiAdminGroup", getValue(rightsObj.get(0),
        XWikiGlobalRightsClass.FIELD_GROUPS).get(0));
    assertEquals(XWikiWebPreferences.ACCESS_RIGHTS,
        getValue(rightsObj.get(0), XWikiGlobalRightsClass.FIELD_LEVELS));
    assertTrue(getValue(rightsObj.get(0), XWikiGlobalRightsClass.FIELD_ALLOW));
  }

  @Test
  public void test_checkGlobalRightsObject_nochanges() throws Exception {
    DocumentReference docRef = new RefBuilder().wiki("testWiki").space(XWikiConstant.XWIKI_SPACE)
        .doc(XWikiConstant.WEB_PREF_DOC_NAME).build(DocumentReference.class);
    XWikiDocument webPrefDoc = new XWikiDocument(docRef);
    List<EAccessLevel> rights = List.of(EAccessLevel.VIEW, EAccessLevel.EDIT, EAccessLevel.COMMENT,
        EAccessLevel.DELETE, EAccessLevel.UNDELETE, EAccessLevel.REGISTER);
    expectClassWithNewObj(getGlobalRightsClass(), webPrefDoc.getWikiRef());

    replayDefault();
    XWikiObjectEditor admGrpObjEditor = XWikiObjectEditor.on(webPrefDoc)
        .filter(XWikiGlobalRightsClass.CLASS_REF);
    admGrpObjEditor.filter(XWikiGlobalRightsClass.FIELD_GROUPS, List.of("XWikiAdminGroup"));
    admGrpObjEditor.filter(XWikiGlobalRightsClass.FIELD_LEVELS, rights);
    admGrpObjEditor.filter(XWikiGlobalRightsClass.FIELD_ALLOW, true);
    admGrpObjEditor.createFirstIfNotExists();
    assertFalse(mandatoryWebPrefs.checkGlobalRightsObject(webPrefDoc));
    verifyDefault();

    List<BaseObject> rightsObj = XWikiObjectFetcher.on(webPrefDoc)
        .filter(XWikiGlobalRightsClass.CLASS_REF)
        .list();
    assertEquals(1, rightsObj.size());
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

  private static ClassDefinition getGlobalRightsClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiGlobalRightsClass.CLASS_DEF_HINT);
  }

  private static <T> T getValue(BaseObject obj, ClassField<T> field) {
    return getFieldAccessor().get(obj, field).get();
  }

  @SuppressWarnings("unchecked")
  private static FieldAccessor<BaseObject> getFieldAccessor() {
    return Utils.getComponent(FieldAccessor.class, XObjectFieldAccessor.NAME);
  }

}
