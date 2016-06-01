package com.celements.query;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.XWikiHibernateBaseStore.HibernateCallback;
import com.xpn.xwiki.store.XWikiHibernateStore;
import com.xpn.xwiki.web.Utils;

public class QueryExecutionServiceTest extends AbstractBridgedComponentTestCase {

  private QueryExecutionService queryExecService;

  private XWikiHibernateStore storeMock;

  @Before
  public void setUp_CelementsWebScriptServiceTest() throws Exception {
    queryExecService = (QueryExecutionService) Utils.getComponent(IQueryExecutionServiceRole.class);
    storeMock = createMockAndAddToDefault(XWikiHibernateStore.class);
    expect(getWikiMock().getHibernateStore()).andReturn(storeMock).anyTimes();
  }

  @Test
  public void testExecuteWriteHQL() throws Exception {
    String hql = "someHQL";
    Map<String, Object> binds = new HashMap<String, Object>();
    binds.put("key", "someVal");
    int ret = 5;
    Capture<HibernateCallback<Integer>> hibCallbackCapture = new Capture<HibernateCallback<Integer>>();

    expect(storeMock.executeWrite(same(getContext()), eq(true), capture(
        hibCallbackCapture))).andReturn(ret).once();

    assertEquals("xwikidb", getContext().getDatabase());
    replayDefault();
    assertEquals(ret, queryExecService.executeWriteHQL(hql, binds));
    verifyDefault();
    assertEquals("xwikidb", getContext().getDatabase());
    ExecuteWriteCallback callback = (ExecuteWriteCallback) hibCallbackCapture.getValue();
    assertEquals(hql, callback.getHQL());
    assertNotSame(binds, callback.getBinds());
    assertEquals(binds, callback.getBinds());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteWriteHQL_otherWiki() throws Exception {
    String hql = "someHQL";
    Map<String, Object> binds = new HashMap<String, Object>();
    binds.put("key", "someVal");
    WikiReference wikiRef = new WikiReference("otherdb");
    int ret = 5;
    Capture<XWikiContext> contextCapture = new ClonedContextCapture();

    expect(storeMock.executeWrite(capture(contextCapture), eq(true), anyObject(
        HibernateCallback.class))).andReturn(ret).once();

    assertEquals("xwikidb", getContext().getDatabase());
    replayDefault();
    assertEquals(ret, queryExecService.executeWriteHQL(hql, binds, wikiRef));
    verifyDefault();
    assertEquals("xwikidb", getContext().getDatabase());
    XWikiContext clonedContext = contextCapture.getValue();
    assertEquals(wikiRef.getName(), clonedContext.get("wiki"));
    clonedContext.setDatabase("xwikidb");
    assertEquals(getContext(), contextCapture.getValue());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testExecuteWriteHQL_XWE() throws Exception {
    String hql = "someHQL";
    Map<String, Object> binds = new HashMap<String, Object>();
    WikiReference wikiRef = new WikiReference("otherdb");
    Throwable cause = new XWikiException();

    expect(storeMock.executeWrite(same(getContext()), eq(true), anyObject(
        HibernateCallback.class))).andThrow(cause).once();

    assertEquals("xwikidb", getContext().getDatabase());
    replayDefault();
    try {
      queryExecService.executeWriteHQL(hql, binds, wikiRef);
    } catch (XWikiException xwe) {
      assertSame(cause, xwe);
    }
    verifyDefault();
    assertEquals("xwikidb", getContext().getDatabase());
  }

  private class ClonedContextCapture extends Capture<XWikiContext> {

    private static final long serialVersionUID = 1L;

    @Override
    public void setValue(XWikiContext context) {
      super.setValue((XWikiContext) context.clone());
    }

  }

}
