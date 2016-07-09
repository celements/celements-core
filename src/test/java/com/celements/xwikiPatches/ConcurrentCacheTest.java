package com.celements.xwikiPatches;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.xwiki.cache.CacheFactory;
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
import com.xpn.xwiki.web.Utils;

public class ConcurrentCacheTest extends AbstractComponentTest {

  private XWikiCacheStore theCacheStore;
  private final String wikiName = "testWiki";
  private final WikiReference wikiRef = new WikiReference(wikiName);
  private String testFullName = "TestSpace.TestDoc";
  private XWikiConfig configMock;
  private DocumentReference testDocRef;
  private SessionFactory sessionFactoryMock;
  private IPageTypeClassConfig pageTypeClassConfig;
  private INavigationClassConfig navClassConfig;
  private IWebUtilsService webUtilsService;
  private ConcurrentMap<DocumentReference, List<BaseObject>> baseObjMap = new ConcurrentHashMap<>();

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
    expect(getWikiMock().getPlugin(eq("monitor"), same(getContext()))).andReturn(null).anyTimes();
    expect(getWikiMock().hasDynamicCustomMappings()).andReturn(false).anyTimes();
    expect(getWikiMock().isVirtualMode()).andReturn(false).anyTimes();
    expect(getWikiMock().Param(eq("xwiki.store.hibernate.useclasstables.read"), eq("1"))).andReturn(
        "0").anyTimes();
    expect(getWikiMock().getXClass(isA(DocumentReference.class), isA(
        XWikiContext.class))).andStubDelegateTo(new TestXWiki());
    createBaseObjects();
  }

  @Test
  public void test_singleThreaded() throws Exception {
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
    sessionMock.load(isA(XWikiDocument.class), eq(new Long(myDoc.getId())));
    expectLastCall().andAnswer(new IAnswer<Object>() {

      @Override
      public Object answer() throws Throwable {
        XWikiDocument theDoc = (XWikiDocument) getCurrentArguments()[0];
        theDoc.setContent("test Content");
        theDoc.setTitle("the test Title");
        theDoc.setAuthor("XWiki.testAuthor");
        theDoc.setCreationDate(new java.sql.Date(new Date().getTime() - 5000L));
        theDoc.setContentUpdateDate(new java.sql.Date(new Date().getTime() - 2000L));
        return this;
      }
    }).once();
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
    expect(sessionMock.createQuery(eq(loadAttachmentHql))).andReturn(query);

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
    expect(sessionMock.createQuery(eq(loadBaseObjectHql))).andReturn(queryObj);
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

    replayDefault();
    initStore();
    LoadXWikiDocCommand testLoadCommand = new LoadXWikiDocCommand();
    Boolean result = testLoadCommand.call();
    assertTrue(result);
    verifyDefault();
  }

  public void createBaseObjects() {
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

  @Test
  public void test_multiThreaded() throws Exception {
    fail("not yet implemented");
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

    @Override
    public Boolean call() throws Exception {
      XWikiDocument myDoc = new XWikiDocument(testDocRef);
      XWikiDocument loadedXWikiDoc = theCacheStore.loadXWikiDoc(myDoc, getContext());
      assertNotNull(loadedXWikiDoc);
      boolean testResult = true;
      for (BaseObject theTestObj : baseObjMap.get(testDocRef)) {
        Map<DocumentReference, List<BaseObject>> loadedObjs = loadedXWikiDoc.getXObjects();
        testResult &= loadedObjs.get(theTestObj.getXClassReference()).contains(theTestObj);
      }
      return testResult;
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
