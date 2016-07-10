package com.celements.xwikiPatches;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import com.celements.web.service.IWebUtilsService;
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
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;
import com.xpn.xwiki.util.AbstractXWikiRunnable;
import com.xpn.xwiki.web.Utils;

public class ConcurrentCacheTest extends AbstractComponentTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentCacheTest.class);

  private volatile XWikiCacheStore theCacheStore;
  private volatile ConcurrentMap<DocumentReference, List<BaseObject>> baseObjMap = new ConcurrentHashMap<>();
  private volatile DocumentReference testDocRef;
  private static volatile Collection<Object> defaultMocks;
  private static volatile XWikiContext defaultContext;

  private final String wikiName = "testWiki";
  private final WikiReference wikiRef = new WikiReference(wikiName);
  private String testFullName = "TestSpace.TestDoc";
  private XWikiConfig configMock;
  private SessionFactory sessionFactoryMock;
  private IPageTypeClassConfig pageTypeClassConfig;
  private INavigationClassConfig navClassConfig;
  private IWebUtilsService webUtilsService;

  @SuppressWarnings("deprecation")
  @Before
  public void setUp_ConcurrentCatchTest() throws Exception {
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
  }

  @Test
  public void test_singleThreaded_sync() throws Exception {
    Session sessionMock = createMockAndAddToDefault(Session.class);
    expect(sessionFactoryMock.openSession()).andReturn(sessionMock).once();
    sessionMock.setFlushMode(eq(FlushMode.COMMIT));
    expectLastCall().atLeastOnce();
    sessionMock.setFlushMode(eq(FlushMode.MANUAL));
    expectLastCall().atLeastOnce();
    Transaction transactionMock = createMockAndAddToDefault(Transaction.class);
    expect(sessionMock.beginTransaction()).andReturn(transactionMock).once();
    transactionMock.rollback();
    expectLastCall().once();
    expect(sessionMock.close()).andReturn(null).once();
    XWikiDocument myDoc = new XWikiDocument(testDocRef);
    expectXWikiDocLoad(sessionMock, myDoc);
    expectLoadEmptyAttachmentList(sessionMock);
    expectBaseObjectLoad(sessionMock);
    replayDefault();
    initStorePrepareMultiThreadMocks();
    LoadXWikiDocCommand testLoadCommand = new LoadXWikiDocCommand();
    List<Object> result = testLoadCommand.call();
    assertTrue((Boolean) result.get(0));
    verifyDefault();
  }

  @Test
  public void test_singleThreaded_async() throws Exception {
    Session sessionMock = createMockAndAddToDefault(Session.class);
    expect(sessionFactoryMock.openSession()).andReturn(sessionMock).once();
    sessionMock.setFlushMode(eq(FlushMode.COMMIT));
    expectLastCall().atLeastOnce();
    sessionMock.setFlushMode(eq(FlushMode.MANUAL));
    expectLastCall().atLeastOnce();
    Transaction transactionMock = createMockAndAddToDefault(Transaction.class);
    expect(sessionMock.beginTransaction()).andReturn(transactionMock).once();
    transactionMock.rollback();
    expectLastCall().once();
    expect(sessionMock.close()).andReturn(null).once();
    XWikiDocument myDoc = new XWikiDocument(testDocRef);
    expectXWikiDocLoad(sessionMock, myDoc);
    expectLoadEmptyAttachmentList(sessionMock);
    expectBaseObjectLoad(sessionMock);
    replayDefault();
    initStorePrepareMultiThreadMocks();
    ScheduledExecutorService theExecutor = Executors.newScheduledThreadPool(1);
    Future<List<Object>> testFuture = theExecutor.submit(
        (Callable<List<Object>>) new LoadXWikiDocCommand());
    theExecutor.shutdown();
    while (!theExecutor.isTerminated()) {
      Thread.sleep(500L);
    }
    List<Object> result = testFuture.get();
    @SuppressWarnings("unchecked")
    List<String> messages = (List<String>) result.get(1);
    assertTrue(Arrays.deepToString(messages.toArray()), (Boolean) result.get(0));
    verifyDefault();
  }

  @Test
  public void test_multiRuns_singleThreaded() throws Exception {
    executeMultiRunsTest(1, 50000);
  }

  @Test
  public void test_multiThreaded() throws Exception {
    int cores = Runtime.getRuntime().availableProcessors();
    assertTrue("This tests needs real multi core processors, but found " + cores, cores > 1);
    executeMultiRunsTest(cores, 100000);
  }

  private void executeMultiRunsTest(int cores, int executeRuns) throws Exception {
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
    replayDefault();
    initStorePrepareMultiThreadMocks();
    ScheduledExecutorService theExecutor = Executors.newScheduledThreadPool(cores);
    List<Future<List<Object>>> futureList = new ArrayList<>(executeRuns * (cores + 1));
    for (int i = 1; i < executeRuns; i++) {
      futureList.add(theExecutor.submit((Callable<List<Object>>) new LoadXWikiDocCommand()));
      CountDownLatch doneSignal = new CountDownLatch(cores);
      CountDownLatch startSignal = new CountDownLatch(cores);
      for (int j = 1; j <= cores; j++) {
        Future<List<Object>> testFuture = theExecutor.schedule(
            (Callable<List<Object>>) new LoadXWikiDocCommand(startSignal, doneSignal), 100,
            TimeUnit.MILLISECONDS);
        futureList.add(testFuture);
      }
      doneSignal.await();
      theExecutor.submit(new ResetCacheEntryCommand());
    }
    CountDownLatch startSignal2 = new CountDownLatch(cores);
    CountDownLatch doneSignal2 = new CountDownLatch(executeRuns);
    List<Future<List<Object>>> futureList2 = new ArrayList<>(executeRuns);
    futureList2.add(theExecutor.submit((Callable<List<Object>>) new LoadXWikiDocCommand(null,
        doneSignal2)));
    for (int i = 1; i < executeRuns; i++) {
      Future<List<Object>> testFuture = theExecutor.schedule(
          (Callable<List<Object>>) new LoadXWikiDocCommand(startSignal2, doneSignal2), 500,
          TimeUnit.MILLISECONDS);
      futureList2.add(testFuture);
    }
    doneSignal2.await();
    theExecutor.submit(new ResetCacheEntryCommand());
    CountDownLatch startSignal3 = new CountDownLatch(cores);
    CountDownLatch doneSignal3 = new CountDownLatch(executeRuns);
    List<Future<List<Object>>> futureList3 = new ArrayList<>(executeRuns);
    futureList3.add(theExecutor.submit((Callable<List<Object>>) new LoadXWikiDocCommand(null,
        doneSignal3)));
    for (int i = 1; i < executeRuns; i++) {
      Future<List<Object>> testFuture = theExecutor.schedule(
          (Callable<List<Object>>) new LoadXWikiDocCommand(startSignal3, doneSignal3), 1000,
          TimeUnit.MILLISECONDS);
      futureList3.add(testFuture);
    }
    doneSignal3.await();
    theExecutor.shutdown();
    while (!theExecutor.isTerminated()) {
      Thread.sleep(500L);
    }
    assertSuccessFullRuns(futureList);
    assertSuccessFullRuns(futureList2);
    assertSuccessFullRuns(futureList3);
    verifyDefault();
  }

  void assertSuccessFullRuns(List<Future<List<Object>>> futureList) throws InterruptedException,
      ExecutionException {
    int successfulRuns = 0;
    List<String> failMessgs = new ArrayList<>();
    for (Future<List<Object>> testFuture : futureList) {
      List<Object> result = testFuture.get();
      if ((Boolean) result.get(0)) {
        successfulRuns += 1;
      } else {
        @SuppressWarnings("unchecked")
        List<String> messages = (List<String>) result.get(1);
        failMessgs.addAll(messages);
      }
    }
    assertEquals("Found failing runs: " + Arrays.deepToString(failMessgs.toArray()),
        futureList.size(), successfulRuns);
  }

  private void expectBaseObjectLoad(Session sessionMock) {
    String loadBaseObjectHql = "from BaseObject as bobject where bobject.name = :name order by "
        + "bobject.number";
    Query queryObj = new TestQuery<BaseObject>(loadBaseObjectHql, new QueryList<BaseObject>() {

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
    Query queryProp = new TestQuery<String[]>(loadPropHql, new QueryList<String[]>() {

      @Override
      public List<String[]> list(String string, Map<String, Object> params)
          throws HibernateException {
        Integer objId = (Integer) params.get("id");
        List<String[]> propList = new ArrayList<>();
        for (BaseObject templBaseObject : baseObjMap.get(testDocRef)) {
          if (objId.equals(templBaseObject.getId())) {
            for (Object theObj : templBaseObject.getFieldList()) {
              PropertyInterface theField = (PropertyInterface) theObj;
              String[] row = new String[2];
              row[0] = theField.getName();
              row[1] = theField.getClass().getCanonicalName();
              propList.add(row);
            }
          }
        }
        return propList;
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
    Query query = new TestQuery<XWikiAttachment>(loadAttachmentHql,
        new QueryList<XWikiAttachment>() {

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
        if (testDocRef.equals(theDoc)) {
          theDoc.setContent("test Content");
          theDoc.setTitle("the test Title");
          theDoc.setAuthor("XWiki.testAuthor");
          theDoc.setCreationDate(new java.sql.Date(new Date().getTime() - 5000L));
          theDoc.setContentUpdateDate(new java.sql.Date(new Date().getTime() - 2000L));
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
    baseObjMap.put(testDocRefClone, attList);
  }

  private void initStorePrepareMultiThreadMocks() throws XWikiException {
    XWikiStoreInterface store = Utils.getComponent(XWikiStoreInterface.class);
    defaultContext = (XWikiContext) getContext().clone();
    theCacheStore = new XWikiCacheStore(store, defaultContext);
    defaultMocks = Collections.unmodifiableCollection(getDefaultMocks());
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

  private class LoadXWikiDocCommand extends AbstractXWikiRunnable implements
      Callable<List<Object>> {

    private XWikiDocument loadedXWikiDoc;
    private boolean hasNewContext;
    private final List<String> messages = new Vector<String>();
    private final CountDownLatch startSignal;
    private final CountDownLatch doneSignal;
    private boolean startDone = false;

    public LoadXWikiDocCommand() {
      startSignal = null;
      doneSignal = null;
    }

    public LoadXWikiDocCommand(CountDownLatch startSignal, CountDownLatch doneSignal) {
      this.startSignal = startSignal;
      this.doneSignal = doneSignal;
    }

    private ExecutionContext getExecutionContext() {
      return Utils.getComponent(Execution.class).getContext();
    }

    @Override
    public List<Object> call() throws Exception {
      boolean result = false;
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
            result = testLoadedDocument();
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
        messages.add("Exception: " + exp.getMessage() + "\\n" + ExceptionUtils.getStackTrace(exp));
      } finally {
        if ((startSignal != null) && !startDone) {
          startSignal.countDown();
        }
        if (doneSignal != null) {
          doneSignal.countDown();
        }
      }
      return Arrays.asList(result, messages);
    }

    private boolean testLoadedDocument() {
      boolean testResult = loadedXWikiDoc != null;
      if (testResult) {
        for (BaseObject theTestObj : baseObjMap.get(testDocRef)) {
          Map<DocumentReference, List<BaseObject>> loadedObjs = loadedXWikiDoc.getXObjects();
          if (!loadedObjs.get(theTestObj.getXClassReference()).contains(theTestObj)) {
            messages.add("Object missing " + theTestObj);
            testResult = false;
          }
        }
      }
      return testResult;
    }

    @Override
    public void runInternal() {
      try {
        if (startSignal != null) {
          startSignal.countDown();
          startSignal.await();
          startDone = true;
        }
        XWikiDocument myDoc = new XWikiDocument(testDocRef);
        try {
          loadedXWikiDoc = theCacheStore.loadXWikiDoc(myDoc, getContext());
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
      this.params = new HashMap<String, Object>();
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
      // Used to avoid recursive loading of documents if there are recursives usage of classes
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
