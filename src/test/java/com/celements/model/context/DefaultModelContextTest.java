package com.celements.model.context;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.configuration.CelementsFromWikiConfigurationSource;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.util.ModelUtils;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiServletRequest;
import com.xpn.xwiki.web.XWikiServletRequestStub;

public class DefaultModelContextTest extends AbstractComponentTest {

  private DefaultModelContext modelContext;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(IModelAccessFacade.class);
    registerComponentMock(ConfigurationSource.class, CelementsFromWikiConfigurationSource.NAME);
    modelContext = (DefaultModelContext) Utils.getComponent(ModelContext.class);
    modelContext.defaultConfigSrc = createMockAndAddToDefault(ConfigurationSource.class);
  }

  @Test
  public void test_getWiki_setWiki() {
    WikiReference defaultWikiRef = new WikiReference("xwikidb");
    assertEquals(defaultWikiRef, modelContext.getWikiRef());
    WikiReference wikiRef = new WikiReference("db");
    assertEquals(defaultWikiRef, modelContext.setWikiRef(wikiRef));
    assertEquals(wikiRef, modelContext.getWikiRef());
  }

  @Test
  public void test_getDoc_setDoc() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    replayDefault();
    assertNull(modelContext.getDoc());
    assertNull(modelContext.setDoc(doc));
    assertEquals(doc, modelContext.getDoc());
    assertEquals(doc, modelContext.setDoc(null));
    assertNull(modelContext.getDoc());
    verifyDefault();
  }

  @Test
  public void test_getDefaultLanguage() {
    String lang = "xk";
    expect(modelContext.wikiConfigSrc.getProperty(eq(ModelContext.CFG_KEY_DEFAULT_LANG), eq(
        ModelContext.FALLBACK_DEFAULT_LANG))).andReturn(lang).once();
    replayDefault();
    assertEquals(lang, modelContext.getDefaultLanguage());
    verifyDefault();
  }

  @Test
  public void test_getDefaultLanguage_wiki() {
    final String lang = "xk";
    final WikiReference wikiRef = new WikiReference("wiki");
    expect(modelContext.wikiConfigSrc.getProperty(eq(ModelContext.CFG_KEY_DEFAULT_LANG), eq(
        ModelContext.FALLBACK_DEFAULT_LANG))).andAnswer(new IAnswer<String>() {

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
  public void test_getDefaultLanguage_space() throws Exception {
    final String lang = "xk";
    final SpaceReference spaceRef = new SpaceReference("space", new WikiReference("wiki"));
    final DocumentReference webPrefDocRef = getWebPrefDocRef(spaceRef);
    expect(modelContext.defaultConfigSrc.getProperty(eq(ModelContext.CFG_KEY_DEFAULT_LANG), eq(
        ModelContext.FALLBACK_DEFAULT_LANG))).andAnswer(new IAnswer<String>() {

          @Override
          public String answer() throws Throwable {
            assertEquals(spaceRef.getParent().getName(), getContext().getDatabase());
            assertEquals(webPrefDocRef, getContext().getDoc().getDocumentReference());
            return lang;
          }
        }).once();
    expect(getMock(IModelAccessFacade.class).getDocument(eq(webPrefDocRef))).andReturn(
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
    expect(modelContext.defaultConfigSrc.getProperty(eq(ModelContext.CFG_KEY_DEFAULT_LANG), eq(
        ModelContext.FALLBACK_DEFAULT_LANG))).andAnswer(new IAnswer<String>() {

          @Override
          public String answer() throws Throwable {
            assertEquals(docRef.getWikiReference().getName(), getContext().getDatabase());
            assertEquals(webPrefDocRef, getContext().getDoc().getDocumentReference());
            return lang;
          }
        }).once();
    expect(getMock(IModelAccessFacade.class).getDocument(eq(webPrefDocRef))).andReturn(
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

  @Test
  public void test_getRequest_noRequest() {
    assertFalse(modelContext.getRequest().isPresent());
    assertFalse(modelContext.getRequestParameter("asdf").isPresent());
  }

  @Test
  public void test_getRequest_withRequest() {
    getContext().setRequest(new XWikiServletRequestStub());
    assertTrue(modelContext.getRequest().isPresent());
    assertSame(getContext().getRequest(), modelContext.getRequest().get());
    assertFalse(modelContext.getRequestParameter("asdf").isPresent());
  }

  @Test
  public void test_getRequestParameter() {
    HttpServletRequest httpRequestMock = expectRequest();
    expect(httpRequestMock.getParameter("asdf")).andReturn("val").anyTimes();
    replayDefault();
    Optional<String> ret = modelContext.getRequestParameter("asdf");
    verifyDefault();
    assertTrue(ret.isPresent());
    assertEquals("val", ret.get());
  }

  private HttpServletRequest expectRequest() {
    HttpServletRequest httpRequestMock = createMockAndAddToDefault(HttpServletRequest.class);
    getContext().setRequest(new XWikiServletRequest(httpRequestMock));
    expect(httpRequestMock.getCharacterEncoding()).andReturn("").anyTimes();
    return httpRequestMock;
  }

  private void assertDefaultContext() {
    assertEquals("xwikidb", getContext().getDatabase());
    assertNull(getContext().getDoc());
  }

  private DocumentReference getWebPrefDocRef(EntityReference ref) {
    return Utils.getComponent(ModelUtils.class).resolveRef(ModelContext.WEB_PREF_DOC_NAME,
        DocumentReference.class, ref);
  }

}
