package com.celements.docform;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.docform.DocFormRequestKey.*;
import static com.celements.rights.access.EAccessLevel.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractComponentTest;
import com.celements.docform.IDocForm.ResponseState;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;
import com.xpn.xwiki.web.XWikiServletRequestStub;

public class DocFormScriptServiceTest extends AbstractComponentTest {

  private DocFormScriptService docFormService;
  private DocumentReference docRef;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IRightsAccessFacadeRole.class);
    docFormService = (DocFormScriptService) Utils.getComponent(ScriptService.class, "docform");
    docRef = new DocumentReference("db", "space", "doc");
    getContext().setRequest(new XWikiServletRequestStub());
  }

  @Test
  public void test_updateAndSaveDocFromMap_empty() throws Exception {
    Map<String, ?> requestMap = ImmutableMap.of();
    XWikiDocument doc = DocFormCommandTest.create(docRef);
    doc.setNew(false);

    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(docRef)).andReturn(doc);

    replayDefault();
    Map<String, Set<DocumentReference>> responseMap = docFormService
        .updateAndSaveDocFromMap(docRef, requestMap);
    verifyDefault();

    assertEquals(responseMap.toString(), ImmutableSet.of(docRef),
        responseMap.get(ResponseState.unchanged.name()));
  }

  @Test
  public void test_updateAndSaveDocFromMap() throws Exception {
    Map<String, ?> requestMap = ImmutableMap.of("title", "Title");
    XWikiDocument doc = DocFormCommandTest.create(docRef);
    doc.setNew(false);

    expect(getMock(IModelAccessFacade.class).exists(docRef)).andReturn(true);
    expect(getMock(IRightsAccessFacadeRole.class).hasAccessLevel(docRef, EDIT)).andReturn(true);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(docRef)).andReturn(doc);
    getMock(IModelAccessFacade.class).saveDocument(doc, "updateAndSaveDocFormRequest");

    replayDefault();
    Map<String, Set<DocumentReference>> responseMap = docFormService
        .updateAndSaveDocFromMap(docRef, requestMap);
    verifyDefault();

    assertEquals(responseMap.toString(), ImmutableSet.of(docRef),
        responseMap.get(ResponseState.successful.name()));
  }

  @Test
  public void test_updateAndSaveDocFromMap_noChange() throws Exception {
    Map<String, ?> requestMap = ImmutableMap.of("title", "Title");
    XWikiDocument doc = DocFormCommandTest.create(docRef);
    doc.setTitle("Title");
    doc.setNew(false);

    expect(getMock(IModelAccessFacade.class).exists(docRef)).andReturn(true);
    expect(getMock(IRightsAccessFacadeRole.class).hasAccessLevel(docRef, EDIT)).andReturn(true);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(docRef)).andReturn(doc);

    replayDefault();
    Map<String, Set<DocumentReference>> responseMap = docFormService
        .updateAndSaveDocFromMap(docRef, requestMap);
    verifyDefault();

    assertEquals(responseMap.toString(), ImmutableSet.of(docRef),
        responseMap.get(ResponseState.unchanged.name()));
  }

  @Test
  public void test_updateAndSaveDocFromMap_failed() throws Exception {
    Map<String, ?> requestMap = ImmutableMap.of("title", "Title");
    XWikiDocument doc = DocFormCommandTest.create(docRef);
    doc.setNew(false);

    expect(getMock(IModelAccessFacade.class).exists(docRef)).andReturn(true);
    expect(getMock(IRightsAccessFacadeRole.class).hasAccessLevel(docRef, EDIT)).andReturn(true);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(docRef)).andReturn(doc);
    getMock(IModelAccessFacade.class).saveDocument(doc, "updateAndSaveDocFormRequest");
    expectLastCall().andThrow(new DocumentSaveException(docRef));

    replayDefault();
    Map<String, Set<DocumentReference>> responseMap = docFormService
        .updateAndSaveDocFromMap(docRef, requestMap);
    verifyDefault();

    assertEquals(responseMap.toString(), ImmutableSet.of(docRef),
        responseMap.get(ResponseState.failed.name()));
  }

  @Test
  public void test_hasEditOnAllDocs_empty() throws Exception {
    replayDefault();
    assertTrue(docFormService.hasEditOnAllDocs(ImmutableList.of()));
    verifyDefault();
  }

  @Test
  public void test_hasEditOnAllDocs_notExists_noCreate() throws Exception {
    getContext().setRequest(createDefaultMock(XWikiRequest.class));
    DocFormRequestParam param = new DocFormRequestParam(
        createDocFieldKey("key", docRef, "field"), "");

    expect(getMock(IModelAccessFacade.class).exists(docRef)).andReturn(false);
    expect(getContext().getRequest().get("createIfNotExists")).andReturn("false");

    replayDefault();
    assertTrue(docFormService.hasEditOnAllDocs(ImmutableList.of(param)));
    verifyDefault();
  }

  @Test
  public void test_hasEditOnAllDocs_notExists() throws Exception {
    getContext().setRequest(createDefaultMock(XWikiRequest.class));
    DocFormRequestParam param = new DocFormRequestParam(
        createDocFieldKey("key", docRef, "field"), "");

    expect(getMock(IModelAccessFacade.class).exists(docRef)).andReturn(false);
    expect(getContext().getRequest().get("createIfNotExists")).andReturn("true");
    expect(getMock(IRightsAccessFacadeRole.class).hasAccessLevel(docRef, EDIT)).andReturn(true);

    replayDefault();
    assertTrue(docFormService.hasEditOnAllDocs(ImmutableList.of(param)));
    verifyDefault();
  }

  @Test
  public void test_hasEditOnAllDocs_noAccess() throws Exception {
    DocFormRequestParam param = new DocFormRequestParam(
        createDocFieldKey("key", docRef, "field"), "");

    expect(getMock(IModelAccessFacade.class).exists(docRef)).andReturn(true);
    expect(getMock(IRightsAccessFacadeRole.class).hasAccessLevel(docRef, EDIT)).andReturn(false);

    replayDefault();
    assertFalse(docFormService.hasEditOnAllDocs(ImmutableList.of(param)));
    verifyDefault();
  }

  @Test
  public void test_hasEditOnAllDocs_many_allTrue() throws Exception {
    List<DocFormRequestParam> params = ImmutableList.of(
        new DocFormRequestParam(createDocFieldKey("key",
            new DocumentReference("db", "space", "doc1"), "field1"), ""),
        new DocFormRequestParam(createDocFieldKey("key",
            new DocumentReference("db", "space", "doc2"), "field2"), ""),
        new DocFormRequestParam(createDocFieldKey("key",
            new DocumentReference("db", "space", "doc3"), "field3"), ""));

    params.stream().map(DocFormRequestParam::getDocRef).forEach(docRef -> {
      expect(getMock(IModelAccessFacade.class).exists(docRef)).andReturn(true);
      expect(getMock(IRightsAccessFacadeRole.class).hasAccessLevel(docRef, EDIT)).andReturn(true);
    });

    replayDefault();
    assertTrue(docFormService.hasEditOnAllDocs(params));
    verifyDefault();
  }

  @Test
  public void test_hasEditOnAllDocs_many_oneFalse() throws Exception {
    List<DocFormRequestParam> params = ImmutableList.of(
        new DocFormRequestParam(createDocFieldKey("key",
            new DocumentReference("db", "space", "doc1"), "field1"), ""),
        new DocFormRequestParam(createDocFieldKey("key",
            new DocumentReference("db", "space", "doc2"), "field2"), ""),
        new DocFormRequestParam(createDocFieldKey("key",
            new DocumentReference("db", "space", "doc3"), "field3"), ""));

    expect(getMock(IModelAccessFacade.class).exists(params.get(0).getDocRef())).andReturn(true);
    expect(getMock(IRightsAccessFacadeRole.class).hasAccessLevel(params.get(0).getDocRef(), EDIT))
        .andReturn(true);
    expect(getMock(IModelAccessFacade.class).exists(params.get(1).getDocRef())).andReturn(true);
    expect(getMock(IRightsAccessFacadeRole.class).hasAccessLevel(params.get(1).getDocRef(), EDIT))
        .andReturn(false);

    replayDefault();
    assertFalse(docFormService.hasEditOnAllDocs(params));
    verifyDefault();
  }

  @Test
  public void test_hasEditOnAllDocs_sameDoc() throws Exception {
    List<DocFormRequestParam> params = ImmutableList.of(
        new DocFormRequestParam(createDocFieldKey("key", docRef, "field1"), ""),
        new DocFormRequestParam(createDocFieldKey("key", docRef, "field2"), ""),
        new DocFormRequestParam(createDocFieldKey("key", docRef, "field3"), ""));

    expect(getMock(IModelAccessFacade.class).exists(docRef)).andReturn(true);
    expect(getMock(IRightsAccessFacadeRole.class).hasAccessLevel(docRef, EDIT)).andReturn(true);

    replayDefault();
    assertTrue(docFormService.hasEditOnAllDocs(params));
    verifyDefault();
  }

}
