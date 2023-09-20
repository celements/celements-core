package com.celements.mandatory;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.pagetype.classes.PageTypeClass;
import com.xpn.xwiki.XWikiConstant;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
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
  public void test_checkRightsObject_changes() {
    XWikiDocument xwikiWebPrefs = new XWikiDocument(
        new RefBuilder().wiki("testWiki").space(XWikiConstant.XWIKI_SPACE)
            .doc(XWikiConstant.WEB_PREF_DOC_NAME).build(DocumentReference.class));

    replayDefault();
    mandatoryWebPrefs.checkRightsObject(xwikiWebPrefs);
    verifyDefault();

  }

  public void test_checkRightsObject_nochanges() {

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

}
