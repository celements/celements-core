package com.celements.store;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.easymock.IAnswer;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.impl.AbstractQueryImpl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.cache.CacheFactory;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.INavigationClassConfig;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.store.DocumentCacheStore.InvalidateState;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.store.XWikiCacheStore;
import com.xpn.xwiki.store.XWikiCacheStoreInterface;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;
import com.xpn.xwiki.util.AbstractXWikiRunnable;
import com.xpn.xwiki.web.Utils;

public class ConcurrentCacheTest extends AbstractComponentTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentCacheTest.class);

  private volatile XWikiCacheStore theCacheStore;
  private volatile DocumentCacheStore theCacheStoreFixed;
  private volatile Map<DocumentReference, List<BaseObject>> baseObjMap;
  private volatile Map<Integer, List<String[]>> propertiesMap;
  private volatile DocumentReference testDocRef;
  private static volatile Collection<Object> defaultMocks;
  private static volatile XWikiContext defaultContext;
  private final static AtomicBoolean fastFail = new AtomicBoolean();

  private final String wikiName = "testWiki";
  private final WikiReference wikiRef = new WikiReference(wikiName);
  private final String testFullName = "TestSpace.TestDoc";
  private XWikiConfig configMock;
  private SessionFactory sessionFactoryMock;
  private IPageTypeClassConfig pageTypeClassConfig;
  private INavigationClassConfig navClassConfig;
  private IWebUtilsService webUtilsService;

  /**
   * CAUTION: the doc load counting with AtomicIntegers leads to better memory visibility
   * and thus reduces likeliness of a race condition. Hence it may camouflage the race condition!
   * Nevertheless is it important to check that the cache is working at all.
   * Hence test it with and without counting.
   */
  private final boolean verifyDocLoads = false;
  private final AtomicInteger countDocLoads = new AtomicInteger();
  private final AtomicInteger expectedCountDocLoads = new AtomicInteger();
  private final AtomicInteger expectedCountDocLoadsFixed = new AtomicInteger();
  private final AtomicInteger failedToRemoveFromCacheCount = new AtomicInteger(0);
  private final AtomicInteger invalidatedLoadingCount = new AtomicInteger(0);
  private final AtomicInteger failedToInvalidatedLoadingCount = new AtomicInteger(0);
  private final AtomicInteger invalidatedMultipleCount = new AtomicInteger(0);

  @SuppressWarnings("deprecation")
  @Before
  public void setUp_ConcurrentCacheTest() throws Exception {
    pageTypeClassConfig = Utils.getComponent(IPageTypeClassConfig.class);
    navClassConfig = Utils.getComponent(INavigationClassConfig.class);
    webUtilsService = Utils.getComponent(IWebUtilsService.class);
    getContext().setDatabase(wikiName);
    sessionFactoryMock = createMockAndAddToDefault(SessionFactory.class);
    Utils.getComponent(HibernateSessionFactory.class).setSessionFactory(sessionFactoryMock);
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
    expect(getWikiMock().getPlugin(eq("monitor"), isA(XWikiContext.class))).andReturn(
        null).anyTimes();
    expect(getWikiMock().hasDynamicCustomMappings()).andReturn(false).anyTimes();
    expect(getWikiMock().isVirtualMode()).andReturn(false).anyTimes();
    expect(getWikiMock().Param(eq("xwiki.store.hibernate.useclasstables.read"), eq("1"))).andReturn(
        "0").anyTimes();
    expect(getWikiMock().getXClass(isA(DocumentReference.class), isA(
        XWikiContext.class))).andStubDelegateTo(new TestXWiki());
    createBaseObjects();
    createPropertiesMap();
  }

  @Test
  public void test_singleThreaded_sync_fixed() throws Exception {
    setupTestMocks();
    replayDefault();
    initStorePrepareMultiThreadMocks();
    preloadCache(theCacheStoreFixed);
    if (verifyDocLoads) {
      assertEquals(expectedCountDocLoadsFixed.get(), countDocLoads.get());
    }
    assertNotNull("Expecting document in cache.", theCacheStoreFixed.getDocFromCache(
        theCacheStoreFixed.getKeyWithLang(testDocRef, "")));
    verifyDefault();
  }

  @Test
  public void test_singleThreaded_sync() throws Exception {
    setupTestMocks();
    replayDefault();
    initStorePrepareMultiThreadMocks();
    preloadCache(theCacheStore);
    if (verifyDocLoads) {
      assertEquals(expectedCountDocLoads.get(), countDocLoads.get());
    }
    assertNotNull(theCacheStore.getCache().get(theCacheStore.getKey(wikiName, testFullName, "")));
    verifyDefault();
  }

  @Test
  public void test_singleThreaded_async_fixed() throws Exception {
    setupTestMocks();
    replayDefault();
    initStorePrepareMultiThreadMocks();
    preloadCache(theCacheStoreFixed);
    ScheduledExecutorService theExecutor = Executors.newScheduledThreadPool(1);
    Future<LoadDocCheckResult> testFuture = theExecutor.submit(
        (Callable<LoadDocCheckResult>) new LoadXWikiDocCommand(theCacheStoreFixed));
    theExecutor.shutdown();
    while (!theExecutor.isTerminated()) {
      theExecutor.awaitTermination(1, TimeUnit.SECONDS);
    }
    LoadDocCheckResult result = testFuture.get();
    assertTrue(Arrays.deepToString(result.getMessages().toArray()), result.isSuccessfull());
    if (verifyDocLoads) {
      assertEquals(expectedCountDocLoadsFixed.get(), countDocLoads.get());
    }
    verifyDefault();
  }

  @Test
  public void test_singleThreaded_async() throws Exception {
    setupTestMocks();
    replayDefault();
    initStorePrepareMultiThreadMocks();
    preloadCache(theCacheStore);
    ScheduledExecutorService theExecutor = Executors.newScheduledThreadPool(1);
    Future<LoadDocCheckResult> testFuture = theExecutor.submit(
        (Callable<LoadDocCheckResult>) new LoadXWikiDocCommand(theCacheStore));
    theExecutor.shutdown();
    while (!theExecutor.isTerminated()) {
      theExecutor.awaitTermination(1, TimeUnit.SECONDS);
    }
    LoadDocCheckResult result = testFuture.get();
    assertTrue(Arrays.deepToString(result.getMessages().toArray()), result.isSuccessfull());
    if (verifyDocLoads) {
      assertEquals(expectedCountDocLoads.get(), countDocLoads.get());
    }
    verifyDefault();
  }

  @Test
  public void test_multiRuns_singleThreaded_scenario1_fixed() throws Exception {
    int cores = 1;
    int executeRuns = 5000;
    setupTestMocks();
    replayDefault();
    initStorePrepareMultiThreadMocks();
    assertSuccessFullRuns(testScenario1(theCacheStoreFixed, cores, executeRuns));
    if (verifyDocLoads) {
      assertEquals(expectedCountDocLoadsFixed.get(), countDocLoads.get());
    }
    verifyDefault();
  }

  @Test
  public void test_multiRuns_singleThreaded_scenario1() throws Exception {
    int cores = 1;
    int executeRuns = 5000;
    setupTestMocks();
    replayDefault();
    initStorePrepareMultiThreadMocks();
    assertSuccessFullRuns(testScenario1(theCacheStore, cores, executeRuns));
    if (verifyDocLoads) {
      assertEquals(expectedCountDocLoads.get(), countDocLoads.get());
    }
    verifyDefault();
  }

  @Test
  public void test_multiThreaded_scenario1_fixed_DocumentCacheStore() throws Exception {
    int cores = Runtime.getRuntime().availableProcessors();
    assertTrue("This tests needs real multi core processors, but found " + cores, cores > 1);
    // tested on an intel quadcore 4770
    // without triggering any race condition! Tested with up to 10'000'000 executeRuns!
    // int executeRuns = 10000000;
    int executeRuns = 30000;
    setupTestMocks();
    replayDefault();
    initStorePrepareMultiThreadMocks();
    assertSuccessFullRuns(testScenario1(theCacheStoreFixed, cores, executeRuns));
    if (verifyDocLoads) {
      int countLoads = countDocLoads.get();
      int expectedLoads = expectedCountDocLoadsFixed.get();
      final String failingDetails = " expected loads '" + expectedLoads
          + "' must be lower equal to count loads '" + countLoads + "'\n diff '" + (countLoads
              - expectedLoads) + "'\n invalidatedLoadingCount '" + invalidatedLoadingCount.get()
          + "'\n invalidatedMultipleCount '" + invalidatedMultipleCount.get()
          + "'\n failedToInvalidatedLoadingCount '" + failedToInvalidatedLoadingCount.get()
          + "'\n failedToRemoveFromCacheCount '" + failedToRemoveFromCacheCount.get() + "'";
      assertTrue("invalidating during load leads to multiple loads for one invalidation, thus\n"
          + failingDetails, expectedLoads <= countLoads);
    }
    verifyDefault();
  }

  /**
   * IMPORTANT: This test shows, that XWikiCacheStore is broken. Increase executeRuns if the test
   * fails. Commented out from test executions since it doesn't pass reliably, see CELDEV-532
   */
  // @Test
  public void test_failing_multiThreaded_scenario1_XWikiCacheStore() throws Exception {
    int cores = Runtime.getRuntime().availableProcessors();
    assertTrue("This tests needs real multi core processors, but found " + cores, cores > 1);
    // on an intel quadcore 4770 first race condition generally happens already after a few thousand
    int executeRuns = 30000;
    setupTestMocks();
    replayDefault();
    initStorePrepareMultiThreadMocks();
    List<Future<LoadDocCheckResult>> testResults = testScenario1(theCacheStore, cores, executeRuns);
    boolean failed = false;
    try {
      assertSuccessFullRuns(testResults);
    } catch (AssertionError exp) {
      // expected
      failed = true;
    }
    if (!failed) {
      fail();
    }
    if (verifyDocLoads) {
      assertTrue("no synchronisation on loading must lead to concurrent loads."
          + " Thus more than the expected loads must have happend.",
          expectedCountDocLoads.get() <= countDocLoads.get());
    }
    verifyDefault();
  }

  private void preloadCache(XWikiCacheStoreInterface store) throws Exception {
    if (verifyDocLoads) {
      countDocLoads.set(0);
      expectedCountDocLoads.set(1);
      expectedCountDocLoadsFixed.set(1);
    }
    LoadXWikiDocCommand testLoadCommand = new LoadXWikiDocCommand(store);
    LoadDocCheckResult result = testLoadCommand.call();
    assertTrue(Arrays.deepToString(result.getMessages().toArray()), result.isSuccessfull());
  }

  private void setupTestMocks() {
    Session sessionMock = createMockAndAddToDefault(Session.class);
    expect(sessionFactoryMock.openSession()).andReturn(sessionMock).anyTimes();
    sessionMock.setFlushMode(eq(FlushMode.COMMIT));
    expectLastCall().atLeastOnce();
    sessionMock.setFlushMode(eq(FlushMode.MANUAL));
    expectLastCall().atLeastOnce();
    Transaction transactionMock = createMockAndAddToDefault(Transaction.class);
    expect(sessionMock.beginTransaction()).andReturn(transactionMock).anyTimes();
    transactionMock.rollback();
    expectLastCall().anyTimes();
    expect(sessionMock.close()).andReturn(null).anyTimes();
    XWikiDocument myDoc = new XWikiDocument(testDocRef);
    expectXWikiDocLoad(sessionMock, myDoc);
    expectLoadEmptyAttachmentList(sessionMock);
    expectBaseObjectLoad(sessionMock);
  }

  /**
   * Scenario 1
   * prepare executeRuns as follows
   * 1.1 first and every 3*cores run add a reset cache entry task
   * 1.2 load document 3*cores in parallels for core threads
   * 2. invoke all tasks once to the executor
   * !!CAUTION!!!
   * be careful NOT to add accidentally any memory visibility synchronization
   * e.g. by using CountDownLatch or similar
   * for more details see:
   * http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/package-summary.html#
   * MemoryVisibility
   */
  private List<Future<LoadDocCheckResult>> testScenario1(XWikiCacheStoreInterface store, int cores,
      int maxLoadTasks) throws Exception {
    fastFail.set(false);
    preloadCache(store);
    final int numTimesFromCache = cores * 3;
    final int oneRunRepeats = 200;
    int count = (maxLoadTasks / (oneRunRepeats * numTimesFromCache)) + 1;
    ScheduledExecutorService theExecutor = Executors.newScheduledThreadPool(cores);
    List<Future<LoadDocCheckResult>> futureList = new ArrayList<>(count * (numTimesFromCache + 1)
        * oneRunRepeats);
    try {
      do {
        count--;
        List<Callable<LoadDocCheckResult>> loadTasks = new ArrayList<>(oneRunRepeats
            * numTimesFromCache);
        for (int i = 0; i < oneRunRepeats; i++) {
          if (i > 0) {
            loadTasks.add(new RefreshCacheEntryCommand(store));
            loadTasks.add(new LoadXWikiDocCommand(store));
          }
          for (int j = 1; j <= numTimesFromCache; j++) {
            loadTasks.add(new LoadXWikiDocCommand(store));
          }
        }
        futureList.addAll(theExecutor.invokeAll(loadTasks));
        final Future<LoadDocCheckResult> lastFuture = futureList.get(futureList.size()
            - oneRunRepeats);
        while (!lastFuture.isDone() && !fastFail.get()) {
          Thread.sleep(50);
        }
      } while (!fastFail.get() && (count > 0));
    } finally {
      theExecutor.shutdown();
      while (!theExecutor.isTerminated()) {
        if (fastFail.get()) {
          theExecutor.shutdownNow();
        }
        theExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
      }
    }
    return futureList;
  }

  private void assertSuccessFullRuns(List<Future<LoadDocCheckResult>> futureList)
      throws InterruptedException, ExecutionException {
    int successfulRuns = 0;
    int failedRuns = 0;
    List<String> failMessgs = new ArrayList<>();
    for (Future<LoadDocCheckResult> testFuture : futureList) {
      LoadDocCheckResult result = testFuture.get();
      if (result.isSuccessfull()) {
        successfulRuns += 1;
      } else {
        failedRuns += 1;
        List<String> messages = result.getMessages();
        failMessgs.add("Run num: " + (successfulRuns + failedRuns) + "\n");
        failMessgs.addAll(messages);
      }
    }
    assertEquals("Found " + failedRuns + " failing runs: " + Arrays.deepToString(
        failMessgs.toArray()), futureList.size(), successfulRuns);
  }

  private void expectBaseObjectLoad(Session sessionMock) {
    String loadBaseObjectHql = "from BaseObject as bobject where bobject.name = :name order by "
        + "bobject.number";
    Query queryObj = new TestQuery<>(loadBaseObjectHql, new QueryList<BaseObject>() {

      @Override
      public List<BaseObject> list(String string, Map<String, Object> params)
          throws HibernateException {
        DocumentReference theDocRef = webUtilsService.resolveDocumentReference((String) params.get(
            "name"));
        List<BaseObject> attList = new ArrayList<>();
        for (BaseObject templBaseObject : baseObjMap.get(theDocRef)) {
          BaseObject bObj = createBaseObject(templBaseObject.getNumber(),
              templBaseObject.getXClassReference());
          bObj.setDocumentReference(theDocRef);
          attList.add(bObj);
        }
        return attList;
      }

    });
    expect(sessionMock.createQuery(eq(loadBaseObjectHql))).andReturn(queryObj).anyTimes();
    expectPropertiesLoad(sessionMock);
  }

  private void expectPropertiesLoad(Session sessionMock) {
    String loadPropHql = "select prop.name, prop.classType from BaseProperty as prop where "
        + "prop.id.id = :id";
    Query queryProp = new TestQuery<>(loadPropHql, new QueryList<String[]>() {

      @Override
      public List<String[]> list(String string, Map<String, Object> params)
          throws HibernateException {
        return propertiesMap.get(params.get("id"));
      }

    });
    expect(sessionMock.createQuery(eq(loadPropHql))).andReturn(queryProp).atLeastOnce();
    sessionMock.load(isA(PropertyInterface.class), isA(Serializable.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {

      @Override
      public Object answer() throws Throwable {
        BaseProperty property = (BaseProperty) getCurrentArguments()[0];
        Integer objId = property.getObject().getId();
        for (BaseObject templBaseObject : baseObjMap.get(testDocRef)) {
          if (objId.equals(templBaseObject.getId())) {
            for (Object theObj : templBaseObject.getFieldList()) {
              BaseProperty theField = (BaseProperty) theObj;
              if (theField.getName().equals(property.getName()) && theField.getClass().equals(
                  property.getClass())) {
                property.setValue(theField.getValue());
              }
            }
          }
        }
        return this;
      }
    }).atLeastOnce();
  }

  private void expectLoadEmptyAttachmentList(Session sessionMock) {
    String loadAttachmentHql = "from XWikiAttachment as attach where attach.docId=:docid";
    Query query = new TestQuery<>(loadAttachmentHql, new QueryList<XWikiAttachment>() {

      @Override
      public List<XWikiAttachment> list(String string, Map<String, Object> params)
          throws HibernateException {
        List<XWikiAttachment> attList = new ArrayList<>();
        return attList;
      }

    });
    expect(sessionMock.createQuery(eq(loadAttachmentHql))).andReturn(query).anyTimes();
  }

  private void expectXWikiDocLoad(Session sessionMock, XWikiDocument myDoc) {
    sessionMock.load(isA(XWikiDocument.class), eq(new Long(myDoc.getId())));
    expectLastCall().andAnswer(new IAnswer<Object>() {

      @Override
      public Object answer() throws Throwable {
        XWikiDocument theDoc = (XWikiDocument) getCurrentArguments()[0];
        if (testDocRef.equals(theDoc.getDocumentReference())) {
          if (verifyDocLoads) {
            countDocLoads.incrementAndGet();
          }
          theDoc.setContent("test Content");
          theDoc.setTitle("the test Title");
          theDoc.setAuthor("XWiki.testAuthor");
          theDoc.setCreationDate(new java.sql.Date(new Date().getTime() - 5000L));
          theDoc.setContentUpdateDate(new java.sql.Date(new Date().getTime() - 2000L));
          theDoc.setLanguage("");
          theDoc.setDefaultLanguage("en");
        }
        return this;
      }
    }).anyTimes();
  }

  private void createBaseObjects() {
    DocumentReference testDocRefClone = new DocumentReference(testDocRef.clone());
    BaseObject bObj1 = createBaseObject(0, navClassConfig.getMenuNameClassRef(wikiName));
    bObj1.setDocumentReference(testDocRefClone);
    addStringField(bObj1, INavigationClassConfig.MENU_NAME_LANG_FIELD, "de");
    addStringField(bObj1, INavigationClassConfig.MENU_NAME_FIELD, "Hause");
    BaseObject bObj2 = createBaseObject(1, navClassConfig.getMenuNameClassRef(wikiName));
    bObj2.setDocumentReference(testDocRefClone);
    addStringField(bObj2, INavigationClassConfig.MENU_NAME_LANG_FIELD, "en");
    addStringField(bObj2, INavigationClassConfig.MENU_NAME_FIELD, "Home");
    BaseObject bObj3 = createBaseObject(0, navClassConfig.getMenuItemClassRef(wikiRef));
    bObj3.setDocumentReference(testDocRefClone);
    addIntField(bObj3, INavigationClassConfig.MENU_POSITION_FIELD, 1);
    BaseObject bObj4 = createBaseObject(0, pageTypeClassConfig.getPageTypeClassRef(wikiRef));
    bObj4.setDocumentReference(testDocRefClone);
    addStringField(bObj4, IPageTypeClassConfig.PAGE_TYPE_FIELD, "Performance");
    List<BaseObject> attList = new Vector<>(Arrays.asList(bObj1, bObj2, bObj3, bObj4));
    baseObjMap = ImmutableMap.of(testDocRefClone, attList);
  }

  private void createPropertiesMap() {
    Map<Integer, List<String[]>> propertiesMap = new HashMap<>();
    for (BaseObject templBaseObject : baseObjMap.get(testDocRef)) {
      List<String[]> propList = new ArrayList<>();
      for (Object theObj : templBaseObject.getFieldList()) {
        PropertyInterface theField = (PropertyInterface) theObj;
        String[] row = new String[2];
        row[0] = theField.getName();
        row[1] = theField.getClass().getCanonicalName();
        propList.add(row);
      }
      if (propertiesMap.containsKey(templBaseObject.getId())) {
        throw new IllegalStateException();
      }
      propertiesMap.put(templBaseObject.getId(), ImmutableList.copyOf(propList));
    }
    this.propertiesMap = ImmutableMap.copyOf(propertiesMap);
  }

  private void initStorePrepareMultiThreadMocks() throws XWikiException {
    defaultContext = (XWikiContext) getContext().clone();
    XWikiStoreInterface store = Utils.getComponent(XWikiStoreInterface.class);
    theCacheStore = new XWikiCacheStore(store, defaultContext);
    theCacheStoreFixed = (DocumentCacheStore) Utils.getComponent(XWikiStoreInterface.class,
        DocumentCacheStore.COMPONENT_NAME);
    theCacheStoreFixed.initalize(); // ensure cache is initialized
    theCacheStoreFixed.getStore(); // ensure store is initialized
    defaultMocks = Collections.unmodifiableCollection(getDefaultMocks());
  }

  private class LoadDocCheckResult {

    private final List<String> messages = new Vector<>();

    public void addMessage(String message) {
      messages.add(message);
      fastFail.set(true);
    }

    public boolean isSuccessfull() {
      return (messages.size() == 0);
    }

    public List<String> getMessages() {
      return messages;
    }

  }

  private abstract class AbstractXWikiTestFuture extends AbstractXWikiRunnable implements
      Callable<LoadDocCheckResult> {

    private boolean hasNewContext;
    private final LoadDocCheckResult result = new LoadDocCheckResult();
    private final XWikiCacheStoreInterface store;

    protected AbstractXWikiTestFuture(XWikiCacheStoreInterface store) {
      this.store = store;
    }

    protected XWikiCacheStoreInterface getStore() {
      return this.store;
    }

    private ExecutionContext getExecutionContext() {
      return Utils.getComponent(Execution.class).getContext();
    }

    protected LoadDocCheckResult getResult() {
      return result;
    }

    @Override
    public LoadDocCheckResult call() throws Exception {
      try {
        try {
          hasNewContext = (getExecutionContext() == null);
          if (hasNewContext) {
            initExecutionContext();
            getExecutionContext().setProperty(EXECUTIONCONTEXT_KEY_MOCKS, defaultMocks);
            getExecutionContext().setProperty(XWikiContext.EXECUTIONCONTEXT_KEY,
                defaultContext.clone());
          }
          try {
            runInternal();
          } finally {
            if (hasNewContext) {
              // cleanup execution context
              cleanupExecutionContext();
            }
          }
        } catch (ExecutionContextException e) {
          LOGGER.error("Failed to initialize execution context", e);
        }
      } catch (Throwable exp) {
        // anything could happen in the test and we want to catch all failures
        getResult().addMessage("Exception: " + exp.getMessage() + "\n"
            + ExceptionUtils.getStackTrace(exp));
      }
      return getResult();
    }

  }

  private class RefreshCacheEntryCommand extends AbstractXWikiTestFuture {

    public RefreshCacheEntryCommand(XWikiCacheStoreInterface store) {
      super(store);
    }

    @Override
    public void runInternal() {
      if (!successfullRemoveFromCache()) {
        if (verifyDocLoads) {
          failedToRemoveFromCacheCount.incrementAndGet();
        }
      }
    }

    boolean successfullRemoveFromCache() {
      if (getStore() instanceof DocumentCacheStore) {
        return newStore();
      } else {
        return oldStore();
      }
    }

    private boolean newStore() {
      final InvalidateState invalidState = theCacheStoreFixed.removeDocFromCache(new XWikiDocument(
          testDocRef), true);
      if (verifyDocLoads) {
        switch (invalidState) {
          case LOADING_CANCELED:
            invalidatedLoadingCount.incrementAndGet();
            break;
          case REMOVED:
            expectedCountDocLoadsFixed.incrementAndGet();
            break;
          case LOADING_MULTI_CANCELED:
            invalidatedMultipleCount.incrementAndGet();
            break;
          default:
            failedToInvalidatedLoadingCount.incrementAndGet();
            break;
        }
      }
      return (invalidState.equals(InvalidateState.LOADING_CANCELED) || invalidState.equals(
          InvalidateState.REMOVED));
    }

    private boolean oldStore() {
      String key = theCacheStore.getKey(wikiName, testFullName, "");
      XWikiDocument oldCachedDoc = null;
      if (theCacheStore.getCache() != null) {
        oldCachedDoc = theCacheStore.getCache().get(key);
        theCacheStore.getCache().remove(key);
        if (verifyDocLoads) {
          expectedCountDocLoads.incrementAndGet();
        }
      }
      if (theCacheStore.getPageExistCache() != null) {
        theCacheStore.getPageExistCache().remove(key);
      }
      return oldCachedDoc != null;
    }

  }

  private class LoadXWikiDocCommand extends AbstractXWikiTestFuture {

    private XWikiDocument loadedXWikiDoc;

    public LoadXWikiDocCommand(XWikiCacheStoreInterface store) {
      super(store);
    }

    @Override
    protected void runInternal() {
      loadTestDocument();
      testLoadedDocument();
    }

    private void testLoadedDocument() {
      if (loadedXWikiDoc != null) {
        if (loadedXWikiDoc.isNew()) {
          getResult().addMessage("unexpected: isNew is true");
        }
        if (!loadedXWikiDoc.isMostRecent()) {
          getResult().addMessage("unexpected: isMostRecent is false");
        }
        for (BaseObject theTestObj : baseObjMap.get(testDocRef)) {
          Map<DocumentReference, List<BaseObject>> loadedObjs = loadedXWikiDoc.getXObjects();
          final List<BaseObject> xclassObjs = loadedObjs.get(theTestObj.getXClassReference());
          if (!xclassObjs.contains(theTestObj)) {
            getResult().addMessage("Object missing " + theTestObj);
          } else {
            BaseObject theLoadedObj = xclassObjs.get(xclassObjs.indexOf(theTestObj));
            if (theLoadedObj == theTestObj) {
              getResult().addMessage("Object is same " + theTestObj);
            } else {
              for (String theFieldName : theTestObj.getPropertyNames()) {
                BaseProperty theField = (BaseProperty) theLoadedObj.getField(theFieldName);
                BaseProperty theTestField = (BaseProperty) theTestObj.getField(theFieldName);
                if (theField == theTestField) {
                  getResult().addMessage("Field is same " + theField);
                } else if (!theTestField.getValue().equals(theField.getValue())) {
                  getResult().addMessage("Field value missmatch expected: " + theField
                      + "\n but found: " + theField.getValue());
                }
              }
            }
          }
        }
      } else {
        getResult().addMessage("Loaded document reference is null.");
      }
    }

    private void loadTestDocument() {
      try {
        XWikiDocument myDoc = new XWikiDocument(testDocRef);
        try {
          loadedXWikiDoc = getStore().loadXWikiDoc(myDoc, getContext());
        } catch (XWikiException exp) {
          throw new IllegalStateException(exp);
        }
      } catch (Exception exp) {
        throw new RuntimeException(exp);
      }
    }

  }

  private final void addIntField(BaseObject bObj, String fieldName, int value) {
    bObj.setIntValue(fieldName, value);
  }

  private final void addStringField(BaseObject bObj, String fieldName, String value) {
    bObj.setStringValue(fieldName, value);
  }

  private final BaseObject createBaseObject(int num, DocumentReference classRef) {
    BaseObject bObj = new BaseObject();
    bObj.setXClassReference(new DocumentReference(classRef.clone()));
    bObj.setNumber(num);
    return bObj;
  }

  private interface QueryList<T> {

    public List<T> list(String string, Map<String, Object> params) throws HibernateException;

  }

  private class TestQuery<T> extends AbstractQueryImpl {

    private Query theQueryMock;
    private QueryList<T> listStub;
    private Map<String, Object> params;

    public TestQuery(String queryStr, QueryList<T> listStub) {
      super(queryStr, FlushMode.AUTO, null, null);
      this.listStub = listStub;
      this.params = new HashMap<>();
      theQueryMock = createMock(Query.class);
      replay(theQueryMock);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Iterator iterate() throws HibernateException {
      return theQueryMock.iterate();
    }

    @Override
    public ScrollableResults scroll() throws HibernateException {
      return theQueryMock.scroll();
    }

    @Override
    public ScrollableResults scroll(ScrollMode scrollMode) throws HibernateException {
      return theQueryMock.scroll(scrollMode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> list() throws HibernateException {
      if (listStub != null) {
        return listStub.list(getQueryString(), params);
      }
      return theQueryMock.list();
    }

    @Override
    public Query setText(String named, String val) {
      this.params.put(named, val);
      return this;
    }

    @Override
    public Query setInteger(String named, int val) {
      this.params.put(named, new Integer(val));
      return this;
    }

    @Override
    public Query setLong(String named, long val) {
      this.params.put(named, new Long(val));
      return this;
    }

    @Override
    public int executeUpdate() throws HibernateException {
      return theQueryMock.executeUpdate();
    }

    @Override
    public Query setLockMode(String alias, LockMode lockMode) {
      return theQueryMock.setLockMode(alias, lockMode);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Map getLockModes() {
      throw new UnsupportedOperationException("getLockModes not supported");
    }
  }

  private class TestXWiki extends XWiki {

    @Override
    public BaseClass getXClass(DocumentReference documentReference, XWikiContext context)
        throws XWikiException {
      // Used to avoid recursive loading of documents if there are recursive usage of classes
      BaseClass bclass = context.getBaseClass(documentReference);
      if (bclass == null) {
        bclass = new BaseClass();
        bclass.setDocumentReference(documentReference);
        context.addBaseClass(bclass);
      }

      return bclass;
    }
  }

}
