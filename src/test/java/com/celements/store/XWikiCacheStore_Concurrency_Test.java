/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.store;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.impl.AbstractQueryImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.cache.infinispan.internal.InfinispanCacheFactory;
import org.xwiki.cache.internal.DefaultCacheManager;
import org.xwiki.cache.internal.DefaultCacheManagerConfiguration;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.context.internal.DefaultExecution;
import org.xwiki.context.internal.DefaultExecutionContextManager;
import org.xwiki.logging.LoggerManager;
import org.xwiki.model.internal.DefaultModelConfiguration;
import org.xwiki.model.internal.reference.DefaultEntityReferenceProvider;
import org.xwiki.model.internal.reference.DefaultStringDocumentReferenceResolver;
import org.xwiki.model.internal.reference.DefaultStringEntityReferenceResolver;
import org.xwiki.model.internal.reference.DefaultStringEntityReferenceSerializer;
import org.xwiki.model.internal.reference.DefaultSymbolScheme;
import org.xwiki.model.internal.reference.ExplicitReferenceDocumentReferenceResolver;
import org.xwiki.model.internal.reference.ExplicitReferenceEntityReferenceResolver;
import org.xwiki.model.internal.reference.LocalStringEntityReferenceSerializer;
import org.xwiki.model.internal.reference.LocalUidStringEntityReferenceSerializer;
import org.xwiki.model.internal.reference.UidStringEntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.remote.RemoteObservationManagerContext;
import org.xwiki.query.QueryManager;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.internal.MockConfigurationSource;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.DefaultXWikiStubContextProvider;
import com.xpn.xwiki.internal.ReadOnlyXWikiContextProvider;
import com.xpn.xwiki.internal.XWikiCfgConfigurationSource;
import com.xpn.xwiki.internal.XWikiContextProvider;
import com.xpn.xwiki.internal.model.reference.CompactWikiStringEntityReferenceSerializer;
import com.xpn.xwiki.internal.model.reference.CurrentEntityReferenceProvider;
import com.xpn.xwiki.internal.model.reference.CurrentMixedEntityReferenceProvider;
import com.xpn.xwiki.internal.model.reference.CurrentMixedStringDocumentReferenceResolver;
import com.xpn.xwiki.internal.model.reference.CurrentReferenceDocumentReferenceResolver;
import com.xpn.xwiki.internal.model.reference.CurrentReferenceEntityReferenceResolver;
import com.xpn.xwiki.internal.model.reference.CurrentStringDocumentReferenceResolver;
import com.xpn.xwiki.internal.model.reference.CurrentStringEntityReferenceResolver;
import com.xpn.xwiki.internal.model.reference.XClassRelativeStringEntityReferenceResolver;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;
import com.xpn.xwiki.store.migration.DataMigrationManager;
import com.xpn.xwiki.util.AbstractXWikiRunnable;
import com.xpn.xwiki.web.Utils;

@ComponentList({
    XWikiContextProvider.class,
    DefaultXWikiStubContextProvider.class,
    ReadOnlyXWikiContextProvider.class,
    UidStringEntityReferenceSerializer.class,
    DefaultModelConfiguration.class,
    DefaultStringDocumentReferenceResolver.class,
    CurrentStringDocumentReferenceResolver.class,
    ExplicitReferenceDocumentReferenceResolver.class,
    CurrentReferenceDocumentReferenceResolver.class,
    CurrentMixedStringDocumentReferenceResolver.class,
    CurrentStringEntityReferenceResolver.class,
    DefaultStringEntityReferenceResolver.class,
    ExplicitReferenceEntityReferenceResolver.class,
    CurrentReferenceEntityReferenceResolver.class,
    XClassRelativeStringEntityReferenceResolver.class,
    DefaultStringEntityReferenceSerializer.class,
    CompactWikiStringEntityReferenceSerializer.class,
    LocalStringEntityReferenceSerializer.class,
    LocalUidStringEntityReferenceSerializer.class,
    CurrentEntityReferenceProvider.class,
    CurrentMixedEntityReferenceProvider.class,
    DefaultEntityReferenceProvider.class,
    DefaultSymbolScheme.class,
    XWikiHibernateStore.class,
    DefaultCacheManager.class,
    DefaultCacheManagerConfiguration.class,
    InfinispanCacheFactory.class,
    DefaultExecutionContextManager.class,
    DefaultExecution.class
})

public class XWikiCacheStore_Concurrency_Test
{

    private static final Logger LOGGER = LoggerFactory.getLogger(XWikiCacheStore_Concurrency_Test.class);

    @Rule
    public final MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    private volatile XWikiCacheStore theCacheStore;

    private volatile Map<DocumentReference, List<BaseObject>> baseObjMap;

    private volatile DocumentReference testDocRef;

    private final static AtomicBoolean fastFail = new AtomicBoolean();

    private static final String CONCURRENCY_TEST_SESSION_MOCK = "CONCURRENCY_TEST_SESSION_MOCK";

    private static final String wikiName = "testWiki";

    private static final WikiReference wikiRef = new WikiReference(wikiName);

    private final String testFullName = "TestSpace.TestDoc";

    private volatile SessionFactory sessionFactoryMock;

    /**
     * CAUTION: the doc load counting with AtomicIntegers leads to better memory visibility and thus reduces likeliness
     * of a race condition. Hence it may camouflage the race condition! Nevertheless is it important to check that the
     * cache is working at all. Hence test it with and without counting.
     */
    private final boolean verifyDocLoads = true;

    private final AtomicInteger countDocLoads = new AtomicInteger();

    private final AtomicInteger expectedCountDocLoads = new AtomicInteger();

    private final AtomicInteger failedToRemoveFromCacheCount = new AtomicInteger(0);

    private final DocumentReference testClass1DocRef = new DocumentReference(wikiName, "TestClasses",
        "Class1");

    private final DocumentReference testClass2DocRef = new DocumentReference(wikiName, "TestClasses", "Class2");

    private final DocumentReference testClass3DocRef = new DocumentReference(wikiName, "TestClasses", "Class3");

    private DocumentReferenceResolver<String> docRefResolver;

    private final AtomicLong docId = new AtomicLong();

    private volatile Map<Long, List<String[]>> propertiesMap;

    private final static XWiki xwiki = new XWikiCacheStore_Concurrency_Test.TestXWiki();

    @Before
    public void setUp_ConcurrentCacheTest() throws Exception
    {
        Utils.setComponentManager(componentManager);
        ExecutionContextManager ecm = componentManager.getInstance(ExecutionContextManager.class);
        ExecutionContext ec = new ExecutionContext();
        initXWikiContext(ec);
        ecm.initialize(ec);

        // Make sure a default ConfigurationSource is available
        componentManager.registerMemoryConfigurationSource();

        // Make sure a "xwikicfg" ConfigurationSource is available
        componentManager.registerComponent(
            MockConfigurationSource.getDescriptor(XWikiCfgConfigurationSource.ROLEHINT), new MockConfigurationSource());

        componentManager.registerMockComponent(RemoteObservationManagerContext.class);
        componentManager.registerMockComponent(ObservationManager.class);
        componentManager.registerMockComponent(LoggerManager.class);
        componentManager.registerMockComponent(QueryManager.class);
        componentManager.registerMockComponent(DataMigrationManager.class, "hibernate");
        HibernateSessionFactory hibSessionFactory =
            componentManager.registerMockComponent(HibernateSessionFactory.class);

        docRefResolver = componentManager.getInstance(DocumentReferenceResolver.TYPE_STRING, "current");
        sessionFactoryMock =
            mock(SessionFactory.class, withSettings().extraInterfaces(SessionFactoryImplementor.class));
        when(hibSessionFactory.getSessionFactory()).thenReturn(sessionFactoryMock);
        when(sessionFactoryMock.openSession()).thenAnswer(new Answer<Session>()
        {
            @Override
            public Session answer(InvocationOnMock invocation) throws Throwable
            {
                Session session = setupTestMocks();
                if (session == null) {
                    LOGGER.error("session is null");
                }
                return session;
            }
        });
        ConnectionProvider connProvMock = mock(ConnectionProvider.class);
        when(((SessionFactoryImplementor) sessionFactoryMock).getConnectionProvider()).thenReturn(connProvMock);
        Connection connMock = mock(Connection.class);
        when(connProvMock.getConnection()).thenReturn(connMock);
        when(connMock.getMetaData()).thenReturn(null);
        Configuration hibConfig = new Configuration();
        hibConfig.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        when(hibSessionFactory.getConfiguration()).thenReturn(hibConfig);

        XWikiStoreInterface store = componentManager.getInstance(XWikiStoreInterface.class, "hibernate");
        theCacheStore = new XWikiCacheStore(store, getContext());
        xwiki.setStore(theCacheStore);

        testDocRef = new DocumentReference(wikiName, "TestSpace", "TestDoc");
        XWikiDocument myDoc = new XWikiDocument(testDocRef);
        docId.set(myDoc.getId());
        createBaseObjects();
        createPropertiesMap();
    }

    @Test
    public void test_singleThreaded_sync() throws Exception
    {
        preloadCache();
        if (verifyDocLoads) {
            assertEquals(expectedCountDocLoads.get(), countDocLoads.get());
        }
        String cacheKey = theCacheStore.getKey(wikiName, testFullName, "");
        assertEquals("8:testWiki9:TestSpace7:TestDoc", cacheKey);
        assertNotNull(theCacheStore.getCache().get(cacheKey));
    }

    @Test
    public void test_singleThreaded_async() throws Exception
    {
        preloadCache();
        ScheduledExecutorService theExecutor = Executors.newScheduledThreadPool(1);
        Future<LoadDocCheckResult> testFuture = theExecutor.submit(
            (Callable<LoadDocCheckResult>) new LoadXWikiDocCommand(false));
        theExecutor.shutdown();
        while (!theExecutor.isTerminated()) {
            theExecutor.awaitTermination(1, TimeUnit.SECONDS);
        }
        LoadDocCheckResult result = testFuture.get();
        assertTrue(Arrays.deepToString(result.getMessages().toArray()), result.isSuccessfull());
        if (verifyDocLoads) {
            assertEquals(expectedCountDocLoads.get(), countDocLoads.get());
        }
    }

    @Test
    public void test_multiRuns_singleThreaded_scenario1() throws Exception
    {
        int cores = 1;
        int executeRuns = 5000;
        assertSuccessfulRuns(testScenario1(cores, executeRuns));
        if (verifyDocLoads) {
            assertEquals(expectedCountDocLoads.get(), countDocLoads.get());
        }
    }

    @Test
    public void test_multiThreaded_scenario1() throws Exception
    {
        int cores = Runtime.getRuntime().availableProcessors();
        assertTrue("This tests needs real multi core processors, but found " + cores, cores > 1);
        // tested on an intel quadcore 4770
        // without triggering any race condition! Tested with up to 10'000'000 executeRuns!
        // int executeRuns = 10000000;
        int executeRuns = 100000;
        assertSuccessfulRuns(testScenario1(cores, executeRuns));
        if (verifyDocLoads) {
            int countLoads = countDocLoads.get();
            int expectedLoads = expectedCountDocLoads.get();
            final String failingDetails = " expected loads '" + expectedLoads
                + "' must be lower equal to count loads '" + countLoads + "'\n diff '" + (countLoads
                    - expectedLoads)
                + "'\n failedToRemoveFromCacheCount '" + failedToRemoveFromCacheCount.get() + "'";
            assertFalse("invalidating during load leads to multiple loads for one invalidation, thus\n"
                + failingDetails, expectedLoads <= countLoads);
        }
    }

    /**
     * IMPORTANT: This test shows, that XWikiCacheStore is broken!!!
     */
    @Test
    public void test_failing_multiThreaded_scenario1_XWikiCacheStore() throws Exception
    {
        int cores = Runtime.getRuntime().availableProcessors();
        assertTrue("This tests needs real multi core processors, but found " + cores, cores > 1);
        // on an intel quadcore 4770 first race condition generally happens already after a few thousand
        int executeRuns = 30000;
        List<Future<LoadDocCheckResult>> testResults = testScenario1(cores, executeRuns);
        boolean failed = false;
        try {
            assertSuccessfulRuns(testResults);
        } catch (AssertionError exp) {
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
    }

    private void preloadCache() throws Exception
    {
        if (verifyDocLoads) {
            countDocLoads.set(0);
            expectedCountDocLoads.set(1);
        }
        LoadXWikiDocCommand testLoadCommand = new LoadXWikiDocCommand(false);
        LoadDocCheckResult result = testLoadCommand.call();
        assertTrue(Arrays.deepToString(result.getMessages().toArray()), result.isSuccessfull());
    }

    private Session setupTestMocks()
    {
        ExecutionContext execContext = getExecutionContext();
        Session sessionMock = (Session) execContext.getProperty(CONCURRENCY_TEST_SESSION_MOCK);
        if (sessionMock == null) {
            sessionMock = mock(Session.class);
            Connection connMock = mock(Connection.class);
            when(sessionMock.connection()).thenReturn(connMock);
            Transaction transactionMock = mock(Transaction.class);
            when(sessionMock.beginTransaction()).thenReturn(transactionMock);
            when(sessionMock.close()).thenReturn(null);
            expectXWikiDocLoad(sessionMock);
            expectLoadEmptyAttachmentList(sessionMock);
            expectBaseObjectLoad(sessionMock);
            execContext.setProperty(CONCURRENCY_TEST_SESSION_MOCK, sessionMock);
        } else {
            LOGGER.warn("session reuse");
        }
        return sessionMock;
    }

    /**
     * Scenario 1 prepare executeRuns as follows 1.1 first and every 3*cores run add a reset cache entry task 1.2 load
     * document 3*cores in parallels for core threads 2. invoke all tasks once to the executor !!CAUTION!!! be careful
     * NOT to add accidentally any memory visibility synchronization e.g. by using CountDownLatch or similar for more
     * details see: http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/package-summary.html# MemoryVisibility
     */
    private List<Future<LoadDocCheckResult>> testScenario1(int cores, int maxLoadTasks) throws Exception
    {
        fastFail.set(false);
        preloadCache();
        final int numTimesFromCache = cores * 12;
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
                        loadTasks.add(new RefreshCacheEntryCommand());
                    }
                    for (int j = 1; j <= numTimesFromCache; j++) {
                        loadTasks.add(new LoadXWikiDocCommand(true));
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

    private void assertSuccessfulRuns(List<Future<LoadDocCheckResult>> futureList)
        throws InterruptedException, ExecutionException
    {
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

    private void expectBaseObjectLoad(Session sessionMock)
    {
        String loadBaseObjectHql = "from BaseObject as bobject where bobject.name = :name order by "
            + "bobject.number";
        Query queryObj = new TestQuery<BaseObject>(loadBaseObjectHql, new QueryList<BaseObject>()
        {

            @Override
            public List<BaseObject> list(String string, Map<String, Object> params)
                throws HibernateException
            {
                DocumentReference theDocRef = docRefResolver.resolve((String) params.get("name"));
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
        when(sessionMock.createQuery(eq(loadBaseObjectHql))).thenReturn(queryObj);
        expectPropertiesLoad(sessionMock);
    }

    private void expectPropertiesLoad(Session sessionMock)
    {
        String loadPropHql = "select prop.name, prop.classType from BaseProperty as prop where "
            + "prop.id.id = :id";
        Query queryProp = new TestQuery<String[]>(loadPropHql, new QueryList<String[]>()
        {

            @Override
            public List<String[]> list(String string, Map<String, Object> params)
                throws HibernateException
            {
                return propertiesMap.get(params.get("id"));
            }

        });
        when(sessionMock.createQuery(eq(loadPropHql))).thenReturn(queryProp);
        doAnswer(new Answer<Void>()
        {

            @SuppressWarnings("rawtypes")
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                BaseProperty property = (BaseProperty) invocation.getArguments()[0];
                Long objId = property.getObject().getId();
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
                return null;
            }
        }).when(sessionMock).load(isA(PropertyInterface.class), isA(Serializable.class));
    }

    private void expectLoadEmptyAttachmentList(Session sessionMock)
    {
        String loadAttachmentHql = "from XWikiAttachment as attach where attach.docId=:docid";
        Query query = new TestQuery<XWikiAttachment>(loadAttachmentHql,
            new QueryList<XWikiAttachment>()
            {

                @Override
                public List<XWikiAttachment> list(String string, Map<String, Object> params)
                    throws HibernateException
                {
                    return Collections.emptyList();
                }

            });
        when(sessionMock.createQuery(eq(loadAttachmentHql))).thenReturn(query);
    }

    private void expectXWikiDocLoad(Session sessionMock)
    {
        doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                XWikiDocument theDoc = (XWikiDocument) invocation.getArguments()[0];
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
                return null;
            }
        }).when(sessionMock).load(isA(XWikiDocument.class), eq(docId.get()));
    }

    private void createBaseObjects()
    {
        DocumentReference testDocRefClone = new DocumentReference(testDocRef);
        BaseObject bObj1 = createBaseObject(0, testClass2DocRef);
        bObj1.setDocumentReference(testDocRefClone);
        addStringField(bObj1, "MENU_NAME_LANG_FIELD", "de");
        addStringField(bObj1, "MENU_NAME_FIELD", "Hause");
        BaseObject bObj2 = createBaseObject(1, testClass2DocRef);
        bObj2.setDocumentReference(testDocRefClone);
        addStringField(bObj2, "MENU_NAME_LANG_FIELD", "en");
        addStringField(bObj2, "MENU_NAME_FIELD", "Home");
        BaseObject bObj3 = createBaseObject(0, testClass3DocRef);
        bObj3.setDocumentReference(testDocRefClone);
        addIntField(bObj3, "MENU_POSITION_FIELD", 1);
        BaseObject bObj4 = createBaseObject(0, testClass1DocRef);
        bObj4.setDocumentReference(testDocRefClone);
        addStringField(bObj4, "TestField", "Performance");
        List<BaseObject> attList = ImmutableList.of(bObj1, bObj2, bObj3, bObj4);
        baseObjMap = ImmutableMap.of(testDocRefClone, attList);
    }

    private void createPropertiesMap()
    {
        Map<Long, List<String[]>> propertiesMap = new HashMap<>();
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

    private class RefreshCacheEntryCommand implements Callable<LoadDocCheckResult>
    {

        @Override
        public LoadDocCheckResult call() throws Exception
        {
            if (!successfullRemoveFromCache()) {
                if (verifyDocLoads) {
                    failedToRemoveFromCacheCount.incrementAndGet();
                }
            }
            return new LoadXWikiDocCommand(false).call();
        }

        boolean successfullRemoveFromCache()
        {
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

    private class LoadDocCheckResult
    {

        private final List<String> messages = new Vector<String>();

        public void addMessage(String message)
        {
            messages.add(message);
            fastFail.set(true);
        }

        public boolean isSuccessfull()
        {
            return (messages.size() == 0);
        }

        public List<String> getMessages()
        {
            return messages;
        }

    }

    private class LoadXWikiDocCommand extends AbstractXWikiRunnable implements
        Callable<LoadDocCheckResult>
    {

        private XWikiDocument loadedXWikiDoc;

        private final LoadDocCheckResult result = new LoadDocCheckResult();

        private boolean fromCache;

        public LoadXWikiDocCommand(boolean fromCache)
        {
            this.fromCache = fromCache;
        }

        @Override
        public LoadDocCheckResult call() throws Exception
        {
            return callInternal();
        }

        @Override
        protected void declareProperties(ExecutionContext executionContext)
        {
            initXWikiContext(executionContext);
        }

        protected LoadDocCheckResult callInternal()
        {
            try {
                try {
                    initExecutionContext();
                    try {
                        runInternal();
                        testLoadedDocument();
                    } finally {
                        // cleanup execution context
                        cleanupExecutionContext();
                    }
                } catch (ExecutionContextException e) {
                    LOGGER.error("Failed to initialize execution context", e);
                }
            } catch (

            Throwable exp) {
                // anything could happen in the test and we want to catch all failures
                result.addMessage("Exception: " + exp.getMessage() + "\n" + ExceptionUtils.getStackTrace(
                    exp));
            }
            return result;
        }

        private void testLoadedDocument()
        {
            if (loadedXWikiDoc != null) {
                if (loadedXWikiDoc.isNew()) {
                    result.addMessage("unexpected: isNew is true");
                }
                if (!loadedXWikiDoc.isMostRecent()) {
                    result.addMessage("unexpected: isMostRecent is false");
                }
                for (BaseObject theTestObj : baseObjMap.get(testDocRef)) {
                    Map<DocumentReference, List<BaseObject>> loadedObjs = loadedXWikiDoc.getXObjects();
                    final List<BaseObject> xclassObjs = loadedObjs.get(theTestObj.getXClassReference());
                    if (!xclassObjs.contains(theTestObj)) {
                        result.addMessage("Object missing " + theTestObj);
                    } else {
                        BaseObject theLoadedObj = xclassObjs.get(xclassObjs.indexOf(theTestObj));
                        if (theLoadedObj == theTestObj) {
                            result.addMessage("Object is same " + theTestObj);
                        } else {
                            for (String theFieldName : theTestObj.getPropertyNames()) {
                                BaseProperty theField = (BaseProperty) theLoadedObj.getField(theFieldName);
                                BaseProperty theTestField = (BaseProperty) theTestObj.getField(theFieldName);
                                if (theField == theTestField) {
                                    result.addMessage("Field is same " + theField);
                                } else if (!theTestField.getValue().equals(theField.getValue())) {
                                    result.addMessage("Field value missmatch expected: " + theField + "\n but found: "
                                        + theField.getValue());
                                }
                            }
                        }
                    }
                }
            } else {
                result.addMessage("Loaded document reference is null.");
            }
        }

        private boolean isInCache(XWikiDocument myDoc)
        {
            String key = theCacheStore.getKey(myDoc, getContext());
            return (theCacheStore.getCache().get(key) != null);
        }

        @Override
        public void runInternal()
        {
            try {
                XWikiDocument myDoc = new XWikiDocument(testDocRef);
                try {
                    while (fromCache && !isInCache(myDoc)) {
                        Thread.sleep(1);
                    }
                    loadedXWikiDoc = theCacheStore.loadXWikiDoc(myDoc, getContext());
                } catch (XWikiException exp) {
                    throw new IllegalStateException(exp);
                }
            } catch (Exception exp) {
                throw new RuntimeException(exp);
            }
        }

    }

    private final void addIntField(BaseObject bObj, String fieldName, int value)
    {
        bObj.setIntValue(fieldName, value);
    }

    private final void addStringField(BaseObject bObj, String fieldName, String value)
    {
        bObj.setStringValue(fieldName, value);
    }

    private final BaseObject createBaseObject(int num, DocumentReference classRef)
    {
        BaseObject bObj = new BaseObject();
        bObj.setXClassReference(new DocumentReference(classRef));
        bObj.setNumber(num);
        return bObj;
    }

    private interface QueryList<T>
    {

        public List<T> list(String string, Map<String, Object> params) throws HibernateException;

    }

    private class TestQuery<T> extends AbstractQueryImpl
    {

        private Query theQueryMock;

        private QueryList<T> listStub;

        private Map<String, Object> params;

        public TestQuery(String queryStr, QueryList<T> listStub)
        {
            super(queryStr, FlushMode.AUTO, null, null);
            this.listStub = listStub;
            this.params = new HashMap<String, Object>();
            theQueryMock = mock(Query.class);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Iterator iterate() throws HibernateException
        {
            return theQueryMock.iterate();
        }

        @Override
        public ScrollableResults scroll() throws HibernateException
        {
            return theQueryMock.scroll();
        }

        @Override
        public ScrollableResults scroll(ScrollMode scrollMode) throws HibernateException
        {
            return theQueryMock.scroll(scrollMode);
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<T> list() throws HibernateException
        {
            if (listStub != null) {
                return listStub.list(getQueryString(), params);
            }
            return theQueryMock.list();
        }

        @Override
        public Query setText(String named, String val)
        {
            this.params.put(named, val);
            return this;
        }

        @Override
        public Query setInteger(String named, int val)
        {
            this.params.put(named, new Integer(val));
            return this;
        }

        @Override
        public Query setLong(String named, long val)
        {
            this.params.put(named, new Long(val));
            return this;
        }

        @Override
        public int executeUpdate() throws HibernateException
        {
            return theQueryMock.executeUpdate();
        }

        @Override
        public Query setLockMode(String alias, LockMode lockMode)
        {
            return theQueryMock.setLockMode(alias, lockMode);
        }

        @Override
        public Query setLockOptions(LockOptions lockOptions)
        {
            throw new UnsupportedOperationException("setLockOptions not supported");
        }

        @Override
        public LockOptions getLockOptions()
        {
            throw new UnsupportedOperationException("getLockOptions not supported");
        }
    }

    private static ExecutionContext getExecutionContext()
    {
        return Utils.getComponent(Execution.class).getContext();
    }

    private static XWikiContext getContext()
    {
        return (XWikiContext) getExecutionContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
    }

    private static void initXWikiContext(ExecutionContext executionContext)
    {
        XWikiContext xwikContext = new XWikiContext();
        xwikContext.setWikiId(wikiName);
        xwikContext.setWikiReference(wikiRef);
        XWiki spyXWiki = spy(xwiki);
        xwikContext.setWiki(spyXWiki);
        xwikContext.declareInExecutionContext(executionContext);
    }

    private static class TestXWiki extends XWiki
    {

        @Override
        public BaseClass getXClass(DocumentReference documentReference, XWikiContext context)
            throws XWikiException
        {
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
