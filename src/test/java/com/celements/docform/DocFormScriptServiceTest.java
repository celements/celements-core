package com.celements.webform;

import static com.celements.common.test.CelementsTestUtils.*;
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

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentSaveException;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class DocFormScriptServiceTest extends AbstractComponentTest {

  private DocFormScriptService docFormService;
  private IModelAccessFacade modelAccessFacade;
  private XWikiRightService rightServiceMock;

  @Before
  public void prepareTest() throws Exception {
    modelAccessFacade = registerComponentMock(IModelAccessFacade.class);
    docFormService = (DocFormScriptService) Utils.getComponent(ScriptService.class, "docform");
    rightServiceMock = createMockAndAddToDefault(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightServiceMock).anyTimes();
    getContext().setRequest(createMockAndAddToDefault(XWikiRequest.class));
  }

  @Test
  public void test_checkRightsAndSaveXWikiDocCollection_empty() throws XWikiException {
    Map<String, Set<DocumentReference>> result = docFormService
        .checkRightsAndSaveXWikiDocCollection(
            Collections.<XWikiDocument>emptyList());
    assertEquals(2, result.size());
    assertEquals(0, result.get("successful").size());
    assertEquals(0, result.get("failed").size());
  }

  @Test
  public void test_checkRightsAndSaveXWikiDocCollection_saveDoc() throws XWikiException,
      DocumentSaveException {
    Collection<XWikiDocument> xdocs = new ArrayList<>();
    XWikiDocument doc = new XWikiDocument(new DocumentReference("w", "S", "D"));
    doc.setNew(false);
    doc.setTitle("some change happened");
    xdocs.add(doc);
    expect(modelAccessFacade.getOrCreateDocument(doc.getDocumentReference()))
        .andReturn(new XWikiDocument(doc.getDocumentReference()));
    modelAccessFacade.saveDocument(eq(doc), (String) anyObject());
    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(getContext().getUser()), eq("w:S.D"),
        same(getContext()))).andReturn(true);
    expect(getContext().getRequest().get(eq("createIfNotExists"))).andReturn("false").anyTimes();
    replayDefault();
    Map<String, Set<DocumentReference>> result = docFormService
        .checkRightsAndSaveXWikiDocCollection(xdocs);
    verifyDefault();
    assertEquals(2, result.size());
    assertEquals(1, result.get("successful").size());
    assertEquals(0, result.get("failed").size());
  }

  @Test
  public void test_checkRightsAndSaveXWikiDocCollection_noChange() throws XWikiException,
      DocumentSaveException {
    Collection<XWikiDocument> xdocs = new ArrayList<>();
    XWikiDocument doc = new XWikiDocument(new DocumentReference("w", "S", "D"));
    doc.setNew(false);
    xdocs.add(doc);
    expect(modelAccessFacade.getOrCreateDocument(doc.getDocumentReference()))
        .andReturn(new XWikiDocument(doc.getDocumentReference()));
    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(getContext().getUser()), eq("w:S.D"),
        same(getContext()))).andReturn(true);
    expect(getContext().getRequest().get(eq("createIfNotExists"))).andReturn("false").anyTimes();
    replayDefault();
    Map<String, Set<DocumentReference>> result = docFormService
        .checkRightsAndSaveXWikiDocCollection(xdocs);
    verifyDefault();
    assertEquals(2, result.size());
    assertEquals(0, result.get("successful").size());
    assertEquals(0, result.get("failed").size());
  }

  @Test
  public void test_checkRightsAndSaveXWikiDocCollection_newWithoutCreateIfNotExists()
      throws XWikiException {
    Collection<XWikiDocument> xdocs = new ArrayList<>();
    XWikiDocument doc = new XWikiDocument(new DocumentReference("w", "S", "D"));
    xdocs.add(doc);
    expect(getContext().getRequest().get(eq("createIfNotExists"))).andReturn("false").anyTimes();
    replayDefault();
    Map<String, Set<DocumentReference>> result = docFormService
        .checkRightsAndSaveXWikiDocCollection(xdocs);
    verifyDefault();
    assertEquals(2, result.size());
    assertEquals(0, result.get("successful").size());
    assertEquals(1, result.get("failed").size());
  }

  @Test
  public void test_checkRightsAndSaveXWikiDocCollection_saveDoc_multiple_notRightsOnAll()
      throws XWikiException {
    Collection<XWikiDocument> xdocs = new ArrayList<>();
    String docName1 = "HasRight";
    String docName2 = "NoRight";
    XWikiDocument doc1 = new XWikiDocument(new DocumentReference("w", "S", docName1));
    doc1.setNew(false);
    xdocs.add(doc1);
    XWikiDocument doc2 = new XWikiDocument(new DocumentReference("w", "S", docName2));
    xdocs.add(doc2);
    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(getContext().getUser()),
        eq("w:S." + docName1), same(getContext()))).andReturn(true);
    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(getContext().getUser()),
        eq("w:S." + docName2), same(getContext()))).andReturn(false);
    expect(getContext().getRequest().get(eq("createIfNotExists"))).andReturn("true").anyTimes();
    replayDefault();
    Map<String, Set<DocumentReference>> result = docFormService
        .checkRightsAndSaveXWikiDocCollection(xdocs);
    verifyDefault();
    assertEquals(2, result.size());
    assertEquals(0, result.get("successful").size());
    assertEquals(2, result.get("failed").size());
    DocumentReference[] failed = new DocumentReference[] { null, null };
    failed = result.get("failed").toArray(failed);
    assertTrue((docName1.equals(failed[0].getName()) && docName2.equals(failed[1].getName()))
        || (docName1.equals(failed[1].getName()) && docName2.equals(failed[0].getName())));
  }

  @Test
  public void test_checkRightsAndSaveXWikiDocCollection_saveDoc_multiple_rightsOnAll()
      throws XWikiException, DocumentSaveException {
    Collection<XWikiDocument> xdocs = new ArrayList<>();
    String docName1 = "HasRight1";
    String docName2 = "HasRight2";
    XWikiDocument doc1 = new XWikiDocument(new DocumentReference("w", "S", docName1));
    xdocs.add(doc1);
    XWikiDocument doc2 = new XWikiDocument(new DocumentReference("w", "S", docName2));
    xdocs.add(doc2);
    modelAccessFacade.saveDocument(eq(doc1), (String) anyObject());
    expectLastCall();
    modelAccessFacade.saveDocument(eq(doc2), (String) anyObject());
    expectLastCall();
    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(getContext().getUser()),
        eq("w:S." + docName1), same(getContext()))).andReturn(true);
    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(getContext().getUser()),
        eq("w:S." + docName2), same(getContext()))).andReturn(true);
    expect(getContext().getRequest().get(eq("createIfNotExists"))).andReturn("true").anyTimes();
    replayDefault();
    Map<String, Set<DocumentReference>> result = docFormService
        .checkRightsAndSaveXWikiDocCollection(xdocs);
    verifyDefault();
    assertEquals(2, result.size());
    assertEquals(2, result.get("successful").size());
    DocumentReference[] success = new DocumentReference[] { null, null };
    success = result.get("successful").toArray(success);
    assertTrue((docName1.equals(success[0].getName()) && docName2.equals(success[1].getName()))
        || (docName1.equals(success[1].getName()) && docName2.equals(success[0].getName())));
    assertEquals(0, result.get("failed").size());
  }

  @Test
  public void test_checkRightsAndSaveXWikiDocCollection_saveDoc_multiple_rightsOnAll_exception()
      throws XWikiException, DocumentSaveException {
    Collection<XWikiDocument> xdocs = new ArrayList<>();
    String docName1 = "HasRight1";
    String docName2 = "HasRight2";
    DocumentReference doc2Ref = new DocumentReference("w", "S", docName2);
    XWikiDocument doc1 = new XWikiDocument(new DocumentReference("w", "S", docName1));
    xdocs.add(doc1);
    XWikiDocument doc2 = new XWikiDocument(doc2Ref);
    xdocs.add(doc2);
    modelAccessFacade.saveDocument(eq(doc1), (String) anyObject());
    expectLastCall();
    modelAccessFacade.saveDocument(eq(doc2), (String) anyObject());
    expectLastCall().andThrow(new DocumentSaveException(doc2Ref));
    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(getContext().getUser()),
        eq("w:S." + docName1), same(getContext()))).andReturn(true);
    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(getContext().getUser()),
        eq("w:S." + docName2), same(getContext()))).andReturn(true);
    expect(getContext().getRequest().get(eq("createIfNotExists"))).andReturn("true").anyTimes();
    replayDefault();
    Map<String, Set<DocumentReference>> result = docFormService
        .checkRightsAndSaveXWikiDocCollection(xdocs);
    verifyDefault();
    assertEquals(2, result.size());
    assertEquals(1, result.get("successful").size());
    assertEquals(docName1, ((DocumentReference) result.get("successful").toArray()[0]).getName());
    assertEquals(1, result.get("failed").size());
    assertEquals(docName2, ((DocumentReference) result.get("failed").toArray()[0]).getName());
  }
}
