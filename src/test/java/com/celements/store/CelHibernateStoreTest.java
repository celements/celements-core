package com.celements.store;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.store.TestHibernateQuery.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.easymock.Capture;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ImmutableDocumentReference;
import org.xwiki.model.reference.ImmutableReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

public class CelHibernateStoreTest extends AbstractComponentTest {

  private SessionFactory sessionFactoryMock;

  @Before
  public void prepareTest() throws Exception {
    sessionFactoryMock = createMockAndAddToDefault(SessionFactory.class);
    expect(getWikiMock().getConfig()).andReturn(new XWikiConfig()).anyTimes();
    expect(getWikiMock().getPlugin("monitor", getContext())).andReturn(null).anyTimes();
    expect(getWikiMock().hasDynamicCustomMappings()).andReturn(false).anyTimes();
    expect(getWikiMock().isVirtualMode()).andReturn(false).anyTimes();
    expect(getWikiMock().hasVersioning(getContext())).andReturn(false).anyTimes();
    expect(getWikiMock().hasBacklinks(getContext())).andReturn(false).anyTimes();
    expect(getWikiMock().Param(eq("xwiki.store.hibernate.useclasstables.read"), eq("1"))).andReturn(
        "0").anyTimes();

  }

  @Test
  public void test_loadXWikiDoc_mutability() throws Exception {
    DocumentReference docRef = new ImmutableDocumentReference("xwikidb", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    DocRefCapture docCapture = new DocRefCapture();
    Session sessionMock = createSessionMock(doc);
    expectLoadAttachments(sessionMock, Collections.<XWikiAttachment>emptyList());
    expectLoadObjects(sessionMock, Collections.<BaseObject>emptyList());
    sessionMock.load(capture(docCapture), eq(new Long(doc.getId())));
    expectLastCall().once();

    replayDefault();
    XWikiDocument ret = getStore(sessionMock).loadXWikiDoc(doc, getContext());
    verifyDefault();

    assertSame(doc, ret);
    assertSame(doc, docCapture.getValue());
    assertEquals(docRef, docCapture.docRef);
    assertFalse("XWikiHibernateStore requires mutable docRef",
        docCapture.docRef instanceof ImmutableReference);
    assertTrue("after execution docRef has to be immutable",
        doc.getDocumentReference() instanceof ImmutableReference);
  }

  @Test
  public void test_saveXWikiDoc_mutability() throws Exception {
    DocumentReference docRef = new ImmutableDocumentReference("xwikidb", "space", "doc");
    XWikiDocument doc = new XWikiDocument(docRef);
    DocRefCapture docCapture = new DocRefCapture();
    Session sessionMock = createSessionMock(doc);
    expectSaveDocExists(sessionMock, false);
    expect(sessionMock.save(capture(docCapture))).andReturn(null).once();

    replayDefault();
    getStore(sessionMock).saveXWikiDoc(doc, getContext());
    verifyDefault();

    assertSame(doc, docCapture.getValue());
    assertEquals(docRef, docCapture.docRef);
    assertFalse("XWikiHibernateStore requires mutable docRef",
        docCapture.docRef instanceof ImmutableReference);
    assertTrue("after execution docRef has to be immutable",
        doc.getDocumentReference() instanceof ImmutableReference);
  }

  private CelHibernateStore getStore(Session session) {
    CelHibernateStore store = (CelHibernateStore) Utils.getComponent(XWikiStoreInterface.class);
    store.setSessionFactory(sessionFactoryMock);
    store.setSession(session, getContext());
    return store;
  }

  private Session createSessionMock(XWikiDocument doc) {
    Session sessionMock = createMockAndAddToDefault(Session.class);
    sessionMock.setFlushMode(anyObject(FlushMode.class));
    expectLastCall().anyTimes();
    return sessionMock;
  }

  private class DocRefCapture extends Capture<XWikiDocument> {

    private static final long serialVersionUID = 1L;

    DocumentReference docRef;

    @Override
    public void setValue(XWikiDocument doc) {
      super.setValue(doc);
      docRef = doc.getDocumentReference();
    }

  }

}
