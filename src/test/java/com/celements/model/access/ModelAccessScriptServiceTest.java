package com.celements.model.access;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.rights.AccessLevel;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class ModelAccessScriptServiceTest extends AbstractBridgedComponentTestCase {

  private ModelAccessScriptService modelAccess;
  private DocumentReference docRef;
  private XWikiDocument doc;

  @Before
  public void setUp_DefaultModelAccessFacadeTest() {
    modelAccess = (ModelAccessScriptService) Utils.getComponent(ScriptService.class, 
        ModelAccessScriptService.NAME);
    modelAccess.modelAccess = createMockAndAddToDefault(IModelAccessFacade.class);
    modelAccess.webUtils = createMockAndAddToDefault(IWebUtilsService.class);
    docRef = new DocumentReference("db", "space", "doc");
    doc = new XWikiDocument(docRef);
    XWikiRightService rightServiceMock = createMockAndAddToDefault(
        XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightServiceMock).anyTimes();
    expect(rightServiceMock.hasProgrammingRights(same(getContext()))).andReturn(true
        ).anyTimes();
  }

  @Test
  public void test_getDocument() throws Exception {
    expect(modelAccess.webUtils.hasAccessLevel(eq(docRef), eq(AccessLevel.VIEW))
        ).andReturn(true).once();
    expect(modelAccess.modelAccess.getDocument(eq(docRef))).andReturn(doc).once();
    Document apiDoc = new Document(doc, getContext());
    expect(modelAccess.modelAccess.getApiDocument(same(doc))).andReturn(apiDoc).once();
    replayDefault();
    Document ret = modelAccess.getDocument(docRef);
    verifyDefault();
    assertSame(ret, apiDoc);
  }

  @Test
  public void test_getDocument_loadException() throws Exception {
    expect(modelAccess.webUtils.hasAccessLevel(eq(docRef), eq(AccessLevel.VIEW))
        ).andReturn(true).once();
    expect(modelAccess.modelAccess.getDocument(eq(docRef))).andThrow(
        new DocumentLoadException(docRef)).once();
    replayDefault();
    Document ret = modelAccess.getDocument(docRef);
    verifyDefault();
    assertNull(ret);
  }

  @Test
  public void test_getDocument_notViewRights() throws Exception {
    expect(modelAccess.webUtils.hasAccessLevel(eq(docRef), eq(AccessLevel.VIEW))
        ).andReturn(false).once();
    replayDefault();
    Document ret = modelAccess.getDocument(docRef);
    verifyDefault();
    assertNull(ret);
  }

  @Test
  public void test_getDocument_null() throws Exception {
    expect(modelAccess.webUtils.hasAccessLevel(isNull(DocumentReference.class), 
        eq(AccessLevel.VIEW))).andReturn(false).once();
    replayDefault();
    Document ret = modelAccess.getDocument(null);
    verifyDefault();
    assertNull(ret);
  }

  @Test
  public void test_exists() {
    expect(modelAccess.modelAccess.exists(eq(docRef))).andReturn(true).once();
    replayDefault();
    boolean ret = modelAccess.exists(docRef);
    verifyDefault();
    assertTrue(ret);
  }

  @Test
  public void test_exists_null() {
    expect(modelAccess.modelAccess.exists(isNull(DocumentReference.class))
        ).andReturn(false).once();
    replayDefault();
    boolean ret = modelAccess.exists(null);
    verifyDefault();
    assertFalse(ret);
  }

}
