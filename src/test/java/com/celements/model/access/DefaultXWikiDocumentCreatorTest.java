package com.celements.model.access;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.configuration.CelementsFromWikiConfigurationSource;
import com.celements.model.context.ModelContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class DefaultXWikiDocumentCreatorTest extends AbstractComponentTest {

  private DefaultXWikiDocumentCreator docCreator;
  private DocumentReference docRef;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(ConfigurationSource.class, "all", getConfigurationSource());
    registerComponentMock(ConfigurationSource.class, CelementsFromWikiConfigurationSource.NAME,
        getConfigurationSource());
    docCreator = (DefaultXWikiDocumentCreator) Utils.getComponent(XWikiDocumentCreator.class);
    docRef = new DocumentReference("db", "space", "doc");
  }

  @Test
  public void test_create() throws Exception {
    String lang = "de";
    getConfigurationSource().setProperty(ModelContext.CFG_KEY_DEFAULT_LANG, lang);
    Date beforeCreationDate = new Date(System.currentTimeMillis() - 1000); // doc drops ms
    expectSpacePreferences(docRef.getLastSpaceReference());
    replayDefault();
    String userName = "XWiki.TestUser";
    getContext().setUser(userName);
    XWikiDocument ret = docCreator.create(docRef);
    verifyDefault();
    assertEquals(docRef, ret.getDocumentReference());
    assertTrue(ret.isNew());
    assertFalse(ret.isFromCache());
    assertEquals(lang, ret.getDefaultLanguage());
    assertEquals("", ret.getLanguage());
    assertTrue(beforeCreationDate.before(ret.getCreationDate()));
    assertTrue(beforeCreationDate.before(ret.getContentUpdateDate()));
    assertTrue(beforeCreationDate.before(ret.getDate()));
    assertEquals(userName, ret.getCreator());
    assertEquals(userName, ret.getAuthor());
    assertEquals(0, ret.getTranslation());
    assertEquals("", ret.getContent());
    assertTrue(ret.isMetaDataDirty());
  }

  private void expectSpacePreferences(SpaceReference spaceRef) throws XWikiException {
    DocumentReference webPrefDocRef = new DocumentReference(ModelContext.WEB_PREF_DOC_NAME,
        spaceRef);
    expect(getWikiMock().exists(eq(webPrefDocRef), same(getContext()))).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(webPrefDocRef), same(getContext()))).andReturn(
        new XWikiDocument(webPrefDocRef)).once();
  }

  @Test
  public void test_create_null() throws Exception {
    try {
      docCreator.create(null);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

}
