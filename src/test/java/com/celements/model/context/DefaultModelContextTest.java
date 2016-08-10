package com.celements.model.context;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.util.IModelUtils;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class DefaultModelContextTest extends AbstractComponentTest {

  private DefaultModelContext modelContext;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    registerComponentMock(IModelAccessFacade.class);
    modelContext = (DefaultModelContext) Utils.getComponent(IModelContext.class);
  }

  @Test
  public void test_getWiki_setWiki() {
    WikiReference defaultWikiRef = new WikiReference("xwikidb");
    assertEquals(defaultWikiRef, modelContext.getWiki());
    WikiReference wikiRef = new WikiReference("db");
    assertEquals(defaultWikiRef, modelContext.setWiki(wikiRef));
    assertEquals(wikiRef, modelContext.getWiki());
  }

  @Test
  public void test_getDoc_setDoc() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(docRef))).andReturn(
        new XWikiDocument(docRef)).once();
    replayDefault();
    assertNull(modelContext.getDoc());
    assertNull(modelContext.setDoc(docRef));
    assertEquals(docRef, modelContext.getDoc());
    assertEquals(docRef, modelContext.setDoc(null));
    assertNull(modelContext.getDoc());
    verifyDefault();
  }

  @Test
  public void test_getDefaultLanguage() {
    assertDefaultContext();
    replayDefault();
    assertEquals(IModelContext.FALLBACK_DEFAULT_LANG, modelContext.getDefaultLanguage());
    verifyDefault();
    assertDefaultContext();
  }

  @Test
  public void test_getDefaultLanguage_cfg() {
    String lang = "xk";
    getConfigurationSource().setProperty(IModelContext.CFG_KEY_DEFAULT_LANG, lang);
    replayDefault();
    assertEquals(lang, modelContext.getDefaultLanguage());
    verifyDefault();
  }

  @Test
  public void test_getDefaultLanguage_wiki() {
    final String lang = "xk";
    final WikiReference wikiRef = new WikiReference("wiki");
    modelContext.cfgSrc = createMockAndAddToDefault(ConfigurationSource.class);
    expect(modelContext.cfgSrc.getProperty(eq(IModelContext.CFG_KEY_DEFAULT_LANG), eq(
        IModelContext.FALLBACK_DEFAULT_LANG))).andAnswer(new IAnswer<String>() {

          @Override
          public String answer() throws Throwable {
            assertEquals(wikiRef.getName(), getContext().getDatabase());
            assertNull(getContext().getDoc());
            return lang;
          }
        }).once();

    assertDefaultContext();
    replayDefault();
    assertEquals(lang, modelContext.getDefaultLanguage(wikiRef));
    verifyDefault();
    assertDefaultContext();
  }

  @Test
  public void test_getDefaultLanguage_space() {
    final String lang = "xk";
    final SpaceReference spaceRef = new SpaceReference("space", new WikiReference("wiki"));
    final DocumentReference webPrefDocRef = getWebPrefDocRef(spaceRef);
    modelContext.cfgSrc = createMockAndAddToDefault(ConfigurationSource.class);
    expect(modelContext.cfgSrc.getProperty(eq(IModelContext.CFG_KEY_DEFAULT_LANG), eq(
        IModelContext.FALLBACK_DEFAULT_LANG))).andAnswer(new IAnswer<String>() {

          @Override
          public String answer() throws Throwable {
            assertEquals(spaceRef.getParent().getName(), getContext().getDatabase());
            assertEquals(webPrefDocRef, getContext().getDoc().getDocumentReference());
            return lang;
          }
        }).once();
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(webPrefDocRef))).andReturn(
        new XWikiDocument(webPrefDocRef)).once();

    assertDefaultContext();
    replayDefault();
    assertEquals(lang, modelContext.getDefaultLanguage(spaceRef));
    verifyDefault();
    assertDefaultContext();
  }

  @Test
  public void test_getDefaultLanguage_doc() throws Exception {
    final String lang = "xk";
    final DocumentReference docRef = new DocumentReference("doc", new SpaceReference("space",
        new WikiReference("wiki")));
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getMock(IModelAccessFacade.class).getDocument(eq(docRef))).andReturn(doc).once();
    final DocumentReference webPrefDocRef = getWebPrefDocRef(docRef.getLastSpaceReference());
    modelContext.cfgSrc = createMockAndAddToDefault(ConfigurationSource.class);
    expect(modelContext.cfgSrc.getProperty(eq(IModelContext.CFG_KEY_DEFAULT_LANG), eq(
        IModelContext.FALLBACK_DEFAULT_LANG))).andAnswer(new IAnswer<String>() {

          @Override
          public String answer() throws Throwable {
            assertEquals(docRef.getWikiReference().getName(), getContext().getDatabase());
            assertEquals(webPrefDocRef, getContext().getDoc().getDocumentReference());
            return lang;
          }
        }).once();
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(webPrefDocRef))).andReturn(
        new XWikiDocument(webPrefDocRef)).once();

    assertDefaultContext();
    replayDefault();
    assertEquals(lang, modelContext.getDefaultLanguage(docRef));
    verifyDefault();
    assertDefaultContext();
  }

  @Test
  public void test_getDefaultLanguage_doc_onDoc() throws Exception {
    final String lang = "xk";
    final DocumentReference docRef = new DocumentReference("doc", new SpaceReference("space",
        new WikiReference("wiki")));
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setDefaultLanguage(lang);
    expect(getMock(IModelAccessFacade.class).getDocument(eq(docRef))).andReturn(doc).once();

    assertDefaultContext();
    replayDefault();
    assertEquals(lang, modelContext.getDefaultLanguage(docRef));
    verifyDefault();
    assertDefaultContext();
  }

  private void assertDefaultContext() {
    assertEquals("xwikidb", getContext().getDatabase());
    assertNull(getContext().getDoc());
  }

  private DocumentReference getWebPrefDocRef(EntityReference ref) {
    return Utils.getComponent(IModelUtils.class).resolveRef(IModelContext.WEB_PREF_DOC_NAME,
        DocumentReference.class, ref);
  }

}
