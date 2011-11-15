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
package com.celements.cells;

import static org.junit.Assert.*;

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.cmd.PageDependentDocumentReferenceCommand;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;

public class CellsScriptServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private CellsScriptService cellsScriptService;
  private PageDependentDocumentReferenceCommand mockPageDepDocRefCmd;
  private DocumentReference cellDocRef;
  private DocumentReference currentDocRef;
  private Document currentDoc;
  private Execution executionMock;
  private XWikiDocument currentXDoc;

  @Before
  public void setUp_CellsScriptServiceTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    cellDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myCell");
    currentDocRef = new DocumentReference(context.getDatabase(), "Content", "myDoc");
    currentXDoc = new XWikiDocument(currentDocRef);
    context.setDoc(currentXDoc);
    currentDoc = createMock(Document.class);
    cellsScriptService = new CellsScriptService();
    executionMock = createMock(Execution.class);
    cellsScriptService.execution = executionMock;
    ExecutionContext execContext = new ExecutionContext();
    execContext.setProperty("xwikicontext", context);
    expect(executionMock.getContext()).andReturn(execContext).anyTimes();
    mockPageDepDocRefCmd = createMock(PageDependentDocumentReferenceCommand.class);
    cellsScriptService.inject_pageDepDocRefCmd(mockPageDepDocRefCmd);
  }

  @Test
  public void testGetPageDepDocRefCmd_default() {
    cellsScriptService.inject_pageDepDocRefCmd(null);
    assertNotSame(cellsScriptService.getPageDepDocRefCmd(),
        cellsScriptService.getPageDepDocRefCmd());
    assertEquals(PageDependentDocumentReferenceCommand.class,
        cellsScriptService.getPageDepDocRefCmd().getClass());
  }

  @Test
  public void testGetPageDepDocRefCmd_inject() {
    cellsScriptService.inject_pageDepDocRefCmd(mockPageDepDocRefCmd);
    assertSame(mockPageDepDocRefCmd, cellsScriptService.getPageDepDocRefCmd());
  }

  @Test
  public void testGetPageDependentDocRef_DocumentReference() {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myExpectedDoc");
    expect(mockPageDepDocRefCmd.getDocumentReference(same(currentXDoc), same(cellDocRef),
        same(context))).andReturn(expectedDocRef).once();
    replayAll();
    assertEquals(expectedDocRef, cellsScriptService.getPageDependentDocRef(cellDocRef));
    verifyAll();
  }

  @Test
  public void testGetPageDependentDocRef_DocumentReferenceDocumentReference(
      ) throws Exception {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myExpectedDoc");
    expect(xwiki.getDocument(eq(currentDocRef), same(context))).andReturn(currentXDoc
      ).once();
    expect(mockPageDepDocRefCmd.getDocumentReference(same(currentXDoc), same(cellDocRef),
        same(context))).andReturn(expectedDocRef).once();
    replayAll();
    assertEquals(expectedDocRef, cellsScriptService.getPageDependentDocRef(currentDocRef,
        cellDocRef));
    verifyAll();
  }

  @Test
  public void testGetPageDependentDocRef_DocumentReferenceDocumentReference_Exception(
      ) throws Exception {
    expect(xwiki.getDocument(eq(currentDocRef), same(context))).andThrow(
        new XWikiException()).once();
    replayAll();
    assertEquals(currentDocRef, cellsScriptService.getPageDependentDocRef(currentDocRef,
        cellDocRef));
    verifyAll();
  }

  @Test
  public void testGetPageDependentTranslatedDocument() throws XWikiException {
    XWikiDocument expectedXDoc = createMock(XWikiDocument.class);
    expect(mockPageDepDocRefCmd.getTranslatedDocument(same(currentXDoc), same(cellDocRef),
        same(context))).andReturn(expectedXDoc).once();
    expect(currentDoc.getDocumentReference()).andReturn(currentDocRef).anyTimes();
    expect(xwiki.getDocument(eq(currentDocRef), same(context))).andReturn(currentXDoc
      ).once();
    Document expectedDoc = new Document(expectedXDoc, context);
    expect(expectedXDoc.newDocument(same(context))).andReturn(expectedDoc).once();
    replayAll(expectedXDoc);
    assertSame(expectedDoc, cellsScriptService.getPageDependentTranslatedDocument(
        currentDoc, cellDocRef));
    verifyAll(expectedXDoc);
  }

  @Test
  public void testGetPageDependentTranslatedDocument_Exception() throws XWikiException {
    try {
      expect(mockPageDepDocRefCmd.getTranslatedDocument(same(currentXDoc),
          same(cellDocRef), same(context))).andThrow(new XWikiException()).once();
      expect(currentDoc.getDocumentReference()).andReturn(currentDocRef).anyTimes();
      expect(xwiki.getDocument(eq(currentDocRef), same(context))).andReturn(currentXDoc
        ).once();
      replayAll();
      assertSame(currentDoc, cellsScriptService.getPageDependentTranslatedDocument(
          currentDoc, cellDocRef));
      verifyAll();
    } catch (XWikiException exp) {
      fail("Expecting to catch XWikiException and return currentDoc.");
    }
  }

  
  private void replayAll(Object ... mocks) {
    replay(xwiki, mockPageDepDocRefCmd, executionMock, currentDoc);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, mockPageDepDocRefCmd, executionMock, currentDoc);
    verify(mocks);
  }

}
