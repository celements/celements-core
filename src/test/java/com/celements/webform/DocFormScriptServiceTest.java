package com.celements.webform;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class DocFormScriptServiceTest extends AbstractBridgedComponentTestCase  {
  private XWikiContext context;
  private XWiki xwiki;
  private DocFormScriptService docFormService;

  @Before
  public void setUp_DocFormScriptServiceTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    docFormService = (DocFormScriptService) Utils.getComponent(ScriptService.class, 
        "docform");
  }

  @Test
  public void testSaveXWikiDocCollection_empty() throws XWikiException {
    Map<String, Set<DocumentReference>> result = docFormService.saveXWikiDocCollection(
        Collections.<XWikiDocument>emptyList());
    assertEquals(2, result.size());
    assertEquals(0, result.get("successful").size());
    assertEquals(0, result.get("failed").size());
  }

  @Test
  public void testSaveXWikiDocCollection_saveDoc() throws XWikiException {
    Collection<XWikiDocument> xdocs = new ArrayList<XWikiDocument>();
    XWikiDocument doc = new XWikiDocument(new DocumentReference("w", "S", "D"));
    doc.setNew(false);
    xdocs.add(doc);
    xwiki.saveDocument(eq(doc), (String)anyObject(), eq(false), same(getContext()));
    expectLastCall();
    XWikiRightService rightService = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightService).anyTimes();
    expect(rightService.hasAccessLevel(eq("edit"), eq(getContext().getUser()), 
        eq("w:S.D"), same(getContext()))).andReturn(true);
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    expect(request.get(eq("createIfNotExists"))).andReturn("false").anyTimes();
    context.setRequest(request);
    replayDefault();
    Map<String, Set<DocumentReference>> result = docFormService.saveXWikiDocCollection(
        xdocs);
    verifyDefault();
    assertEquals(2, result.size());
    assertEquals(1, result.get("successful").size());
    assertEquals(0, result.get("failed").size());
  }

  @Test
  public void testSaveXWikiDocCollection_saveDoc_multiple_notRightsOnAll(
      ) throws XWikiException {
    Collection<XWikiDocument> xdocs = new ArrayList<XWikiDocument>();
    String docName1 = "HasRight";
    String docName2 = "NoRight";
    XWikiDocument doc1 = new XWikiDocument(new DocumentReference("w", "S", docName1));
    xdocs.add(doc1);
    XWikiDocument doc2 = new XWikiDocument(new DocumentReference("w", "S", docName2));
    xdocs.add(doc2);
    XWikiRightService rightService = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightService).anyTimes();
    expect(rightService.hasAccessLevel(eq("edit"), eq(getContext().getUser()), 
        eq("w:S." + docName1), same(getContext()))).andReturn(true);
    expect(rightService.hasAccessLevel(eq("edit"), eq(getContext().getUser()), 
        eq("w:S." + docName2), same(getContext()))).andReturn(false);
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    expect(request.get(eq("createIfNotExists"))).andReturn("true").anyTimes();
    context.setRequest(request);
    replayDefault();
    Map<String, Set<DocumentReference>> result = docFormService.saveXWikiDocCollection(
        xdocs);
    verifyDefault();
    assertEquals(2, result.size());
    assertEquals(0, result.get("successful").size());
    assertEquals(2, result.get("failed").size());
    DocumentReference[] failed = new DocumentReference[] { null, null };
    failed = result.get("failed").toArray(failed);
    assertTrue((docName1.equals(failed[0].getName()) 
        && docName2.equals(failed[1].getName())) 
        || (docName1.equals(failed[1].getName()) 
        && docName2.equals(failed[0].getName())));
  }

  @Test
  public void testSaveXWikiDocCollection_saveDoc_multiple_rightsOnAll(
      ) throws XWikiException {
    Collection<XWikiDocument> xdocs = new ArrayList<XWikiDocument>();
    String docName1 = "HasRight1";
    String docName2 = "HasRight2";
    XWikiDocument doc1 = new XWikiDocument(new DocumentReference("w", "S", docName1));
    xdocs.add(doc1);
    XWikiDocument doc2 = new XWikiDocument(new DocumentReference("w", "S", docName2));
    xdocs.add(doc2);
    xwiki.saveDocument(eq(doc1), (String)anyObject(), eq(false), 
        same(getContext()));
    expectLastCall();
    xwiki.saveDocument(eq(doc2), (String)anyObject(), eq(false), 
        same(getContext()));
    expectLastCall();
    XWikiRightService rightService = createMockAndAddToDefault(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightService).anyTimes();
    expect(rightService.hasAccessLevel(eq("edit"), eq(getContext().getUser()), 
        eq("w:S." + docName1), same(getContext()))).andReturn(true);
    expect(rightService.hasAccessLevel(eq("edit"), eq(getContext().getUser()), 
        eq("w:S." + docName2), same(getContext()))).andReturn(true);
    XWikiRequest request = createMockAndAddToDefault(XWikiRequest.class);
    expect(request.get(eq("createIfNotExists"))).andReturn("true").anyTimes();
    context.setRequest(request);
    replayDefault();
    Map<String, Set<DocumentReference>> result = docFormService.saveXWikiDocCollection(
        xdocs);
    verifyDefault();
    assertEquals(2, result.size());
    assertEquals(2, result.get("successful").size());
    DocumentReference[] success = new DocumentReference[] { null, null };
    success = result.get("successful").toArray(success);
    assertTrue((docName1.equals(success[0].getName()) 
        && docName2.equals(success[1].getName())) 
        || (docName1.equals(success[1].getName()) 
        && docName2.equals(success[0].getName())));
    assertEquals(0, result.get("failed").size());
  }
}
