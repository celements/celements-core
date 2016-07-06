package com.celements.xwikiPatches;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.cache.CacheFactory;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryExecutor;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiCacheStore;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;
import com.xpn.xwiki.web.Utils;

public class ConcurrentCacheTest extends AbstractComponentTest {

  private XWikiCacheStore theCacheStore;
  private String wikiName = "testWiki";
  private String testFullName = "TestSpace.TestDoc";
  private XWikiConfig configMock;
  private DocumentReference testDocRef;
  private SessionFactory sessionFactoryMock;
  private Configuration hibConfigMock;
  private QueryExecutor hibQueryExecMock;

  @Before
  public void setUp_ConcurrentCatchTest() throws Exception {
    getContext().setDatabase(wikiName);
    sessionFactoryMock = createMockAndAddToDefault(SessionFactory.class);
    hibConfigMock = createMockAndAddToDefault(Configuration.class);
    Utils.getComponent(HibernateSessionFactory.class).setSessionFactory(sessionFactoryMock);
    hibQueryExecMock = registerComponentMock(QueryExecutor.class, "hql");
    testDocRef = new DocumentReference(wikiName, "TestSpace", "TestDoc");
    configMock = createMockAndAddToDefault(XWikiConfig.class);
    expect(getWikiMock().getConfig()).andReturn(configMock).anyTimes();
    expect(configMock.getProperty(eq("xwiki.store.hibernate.path"), eq(
        "/WEB-INF/hibernate.cfg.xml"))).andReturn("testhibernate.cfg.xml");
    expect(getWikiMock().Param(eq("xwiki.store.cache.capacity"))).andReturn(null).anyTimes();
    expect(getWikiMock().Param(eq("xwiki.store.cache.pageexistcapacity"))).andReturn(
        null).anyTimes();
    CacheFactory cacheFactory = Utils.getComponent(CacheFactory.class, "jbosscache");
    expect(getWikiMock().getCacheFactory()).andReturn(cacheFactory).anyTimes();
    expect(getWikiMock().getPlugin(eq("monitor"), same(getContext()))).andReturn(null).anyTimes();
    expect(getWikiMock().hasDynamicCustomMappings()).andReturn(false).anyTimes();
    expect(getWikiMock().isVirtualMode()).andReturn(false).anyTimes();
  }

  @Test
  public void test_singleThreaded() throws Exception {
    Session sessionMock = createMockAndAddToDefault(Session.class);
    expect(sessionFactoryMock.openSession()).andReturn(sessionMock).once();
    sessionMock.setFlushMode(eq(FlushMode.COMMIT));
    expectLastCall().once();
    sessionMock.setFlushMode(eq(FlushMode.MANUAL));
    expectLastCall().once();
    Transaction transactionMock = createMockAndAddToDefault(Transaction.class);
    expect(sessionMock.beginTransaction()).andReturn(transactionMock).once();
    expect(sessionMock.close()).andReturn(null).once();
    // expected(sessionMock.load(capture(docCapture), id))
    // TODO
    replayDefault();
    initStore();
    LoadXWikiDocCommand testLoadCommand = new LoadXWikiDocCommand();
    Boolean result = testLoadCommand.call();
    assertTrue(result);
    verifyDefault();
  }

  @Test
  public void test_multiThreaded() throws Exception {
    replayDefault();
    initStore();
    int cores = Runtime.getRuntime().availableProcessors();
    assertTrue("This tests needs real multi core processors, but found " + cores, cores > 1);
    ScheduledExecutorService theExecutor = Executors.newScheduledThreadPool(cores);
    ArrayList<ScheduledFuture<Boolean>> futureList = new ArrayList<>(100);
    for (int i = 1; i < 100; i++) {
      ScheduledFuture<Boolean> testFuture = theExecutor.schedule(new LoadXWikiDocCommand(), 90,
          TimeUnit.MILLISECONDS);
      futureList.add(testFuture);
    }
    theExecutor.scheduleAtFixedRate(new ResetCacheEntryCommand(), 100, 100, TimeUnit.MILLISECONDS);
    try {
      theExecutor.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException exp) {
      exp.printStackTrace();
    }
    for (ScheduledFuture<Boolean> testFuture : futureList) {
      assertTrue(testFuture.isDone());
      assertTrue(testFuture.get());
    }
    theExecutor.shutdown();
    verifyDefault();
  }

  void initStore() throws XWikiException {
    XWikiStoreInterface store = Utils.getComponent(XWikiStoreInterface.class);
    theCacheStore = new XWikiCacheStore(store, getContext());
  }

  private class ResetCacheEntryCommand implements Runnable {

    @Override
    public void run() {
      String key = theCacheStore.getKey(wikiName, testFullName, "");
      if (theCacheStore.getCache() != null) {
        theCacheStore.getCache().remove(key);
      }
    }

  }

  private class LoadXWikiDocCommand implements Callable<Boolean> {

    private XWikiContext getContext() {
      Execution execution = Utils.getComponent(Execution.class);
      ExecutionContext execContext = execution.getContext();
      // TODO create ExecutionContext if not exists
      return (XWikiContext) execContext.getProperty("xwikicontext");
    }

    @Override
    public Boolean call() throws Exception {
      XWikiDocument myDoc = new XWikiDocument(testDocRef);
      XWikiDocument loadedXWikiDoc = theCacheStore.loadXWikiDoc(myDoc, getContext());
      assertNotNull(loadedXWikiDoc);
      // TODO check objects
      return false;
    }

  }

}
