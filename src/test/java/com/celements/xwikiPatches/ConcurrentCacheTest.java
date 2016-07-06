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

import org.junit.Before;
import org.junit.Test;
import org.xwiki.cache.CacheFactory;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiCacheStore;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

public class ConcurrentCacheTest extends AbstractComponentTest {

  private XWikiCacheStore theCacheStore;
  private String wikiName = "testWiki";
  public String testFullName = "TestSpace.TestDoc";
  private XWikiConfig configMock;
  public DocumentReference testDocRef;

  @Before
  public void setUp_ConcurrentCatchTest() throws Exception {
    getContext().setDatabase(wikiName);
    testDocRef = new DocumentReference(wikiName, "TestSpace", "TestDoc");
    configMock = createMockAndAddToDefault(XWikiConfig.class);
    expect(configMock.getProperty(eq("xwiki.store.hibernate.path"), eq(
        "/WEB-INF/hibernate.cfg.xml"))).andReturn("testhibernate.cfg.xml");
    expect(getWikiMock().Param(eq("xwiki.store.cache.capacity"))).andReturn(null).anyTimes();
    expect(getWikiMock().Param(eq("xwiki.store.cache.pageexistcapacity"))).andReturn(
        null).anyTimes();
    CacheFactory cacheFactory = Utils.getComponent(CacheFactory.class, "jbosscache");
    expect(getWikiMock().getCacheFactory()).andReturn(cacheFactory).anyTimes();
  }

  @Test
  public void test() throws Exception {
    expect(getWikiMock().getConfig()).andReturn(configMock);
    replayDefault();
    XWikiStoreInterface store = Utils.getComponent(XWikiStoreInterface.class);
    theCacheStore = new XWikiCacheStore(store, getContext());
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

  public class ResetCacheEntryCommand implements Runnable {

    @Override
    public void run() {
      String key = theCacheStore.getKey(wikiName, testFullName, "");
      if (theCacheStore.getCache() != null) {
        theCacheStore.getCache().remove(key);
      }
    }

  }

  public class LoadXWikiDocCommand implements Callable<Boolean> {

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
