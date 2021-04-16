package com.celements.scheduler.job;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.scheduler.XWikiServletRequestStub;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.XWikiURLFactoryServiceImpl;

public class AbstractJobTest extends AbstractComponentTest {

  private TestJob testJob;
  private XWikiStoreInterface mockStore;
  private DocumentReference testDocRef;
  private IModelAccessFacade modelAccess;

  @Before
  public void setUp_AbstractJobTest() throws Exception {
    mockStore = registerComponentMock(XWikiStoreInterface.class);
    modelAccess = registerComponentMock(IModelAccessFacade.class);
    testDocRef = new DocumentReference(getContext().getDatabase(), "TestSpace", "testDoc");
    getContext().setDoc(new XWikiDocument(testDocRef));
    getContext().setRequest(new XWikiServletRequestStub());
    getContext().setURL(new URL("http", "testhost", 8015, "testfile"));
    expect(getWikiMock().getStore()).andReturn(mockStore).anyTimes();
    expect(getWikiMock().getURLFactoryService()).andReturn(createUrlFactoryService());
    expect(getWikiMock().getWebAppPath(isA(XWikiContext.class))).andReturn("/").anyTimes();
    expect(getWikiMock().getServerURL(eq(getContext().getDatabase()), isA(
        XWikiContext.class))).andReturn(new URL("http://www.myTestURL.ch/")).anyTimes();
    testJob = new TestJob();
  }

  @Test
  public void testInitExecutionContext() throws Exception {
    assertNull(getContext().get("vcontext"));
    Capture<XWikiContext> scontextCapture = newCapture();
    expectStoreCleanup(scontextCapture);
    expect(getWikiMock().Param(eq("xwiki.url.protocol"), (String) isNull())).andReturn(
        "http").atLeastOnce();
    expect(modelAccess.getDocument(eq(testDocRef))).andReturn(new XWikiDocument(testDocRef));
    replayDefault();
    testJob.initExecutionContext(getContext());
    verifyDefault();
    assertNotNull(getContext().get("vcontext"));
  }

  @Test
  public void testCreateJobContext_notSame() throws Exception {
    Capture<XWikiContext> scontextCapture = newCapture();
    expectStoreCleanup(scontextCapture);
    expect(getWikiMock().Param(eq("xwiki.url.protocol"), (String) isNull())).andReturn(
        "http").atLeastOnce();
    expect(modelAccess.getDocument(eq(testDocRef))).andReturn(new XWikiDocument(testDocRef));
    replayDefault();
    XWikiContext newContext = testJob.createJobContext(getContext());
    testJob.setupServerUrlAndFactory(newContext, getContext());
    assertNotSame(newContext, getContext());
    XWikiContext testContext = scontextCapture.getValue();
    assertSame(newContext, testContext);
    verifyDefault();
  }

  @Test
  public void testCreateJobContext_port() throws Exception {
    expectStoreCleanup(newCapture());
    expect(getWikiMock().Param(eq("xwiki.url.protocol"), (String) isNull())).andReturn(
        "http").atLeastOnce();
    expect(modelAccess.getDocument(eq(testDocRef))).andReturn(new XWikiDocument(testDocRef));
    expect(getWikiMock().getServletPath(eq(getContext().getDatabase()), isA(
        XWikiContext.class))).andReturn("").atLeastOnce();
    expect(getWikiMock().showViewAction(isA(XWikiContext.class))).andReturn(false).atLeastOnce();
    expect(getWikiMock().skipDefaultSpaceInURLs(isA(XWikiContext.class))).andReturn(
        true).atLeastOnce();
    expect(getWikiMock().getDefaultSpace(isA(XWikiContext.class))).andReturn(
        "Content").atLeastOnce();
    expect(getWikiMock().useDefaultAction(isA(XWikiContext.class))).andReturn(false).atLeastOnce();
    expect(getWikiMock().getDefaultPage(isA(XWikiContext.class))).andReturn(
        "WebHome").atLeastOnce();
    replayDefault();
    XWikiContext newContext = testJob.createJobContext(getContext());
    testJob.setupServerUrlAndFactory(newContext, getContext());
    assertNotSame(newContext, getContext());
    URL testURL = newContext.getURLFactory().createAttachmentURL("filename", "testSpace",
        "testDocument", "download", "", newContext);
    assertEquals(-1, testURL.getPort());
    verifyDefault();
  }

  // ***************
  // * HELPER
  // ***************

  private XWikiURLFactoryServiceImpl createUrlFactoryService() {
    XWiki urlWikiMock = createMock(XWiki.class);
    expect(urlWikiMock.Param(isA(String.class))).andReturn(null).anyTimes();
    replay(urlWikiMock);
    XWikiURLFactoryServiceImpl factoryService = new XWikiURLFactoryServiceImpl(urlWikiMock);
    return factoryService;
  }

  private void expectStoreCleanup(Capture<XWikiContext> scontextCapture) {
    mockStore.cleanUp(capture(scontextCapture));
    expectLastCall().once();
  }

  private class TestJob extends AbstractJob {

    @Override
    protected Logger getLogger() {
      return LoggerFactory.getLogger(TestJob.class);
    }

    @Override
    protected void executeJob(JobExecutionContext jobContext) throws JobExecutionException {}

  }

}
