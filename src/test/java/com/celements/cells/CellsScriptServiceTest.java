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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.cells.cmd.PageDependentDocumentReferenceCommand;
import com.celements.common.test.AbstractComponentTest;
import com.celements.pagelayout.LayoutServiceRole;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class CellsScriptServiceTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private CellsScriptService cellsScriptService;
  private PageDependentDocumentReferenceCommand mockPageDepDocRefCmd;
  private DocumentReference cellDocRef;
  private DocumentReference currentDocRef;
  private Document currentDoc;
  private XWikiDocument currentXDoc;
  private LayoutServiceRole layoutService;

  @Before
  public void setUp_CellsScriptServiceTest() throws Exception {
    registerComponentMock(LayoutServiceRole.class);
    context = getXContext();
    xwiki = getMock(XWiki.class);
    layoutService = getMock(LayoutServiceRole.class);
    cellDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myCell");
    currentDocRef = new DocumentReference(context.getDatabase(), "Content", "myDoc");
    currentXDoc = new XWikiDocument(currentDocRef);
    context.setDoc(currentXDoc);
    currentDoc = createDefaultMock(Document.class);
    cellsScriptService = Utils.getComponent(CellsScriptService.class);
    mockPageDepDocRefCmd = createDefaultMock(PageDependentDocumentReferenceCommand.class);
    cellsScriptService.inject_pageDepDocRefCmd(mockPageDepDocRefCmd);
  }

  @Test
  public void testGetPageDepDocRefCmd_default() {
    expect(layoutService.getCurrentRenderingLayout())
        .andReturn(new SpaceReference("MyLayout", new WikiReference(context.getDatabase())))
        .atLeastOnce();
    replayDefault();
    cellsScriptService.inject_pageDepDocRefCmd(null);
    assertNotSame(cellsScriptService.getPageDepDocRefCmd(),
        cellsScriptService.getPageDepDocRefCmd());
    assertEquals(PageDependentDocumentReferenceCommand.class,
        cellsScriptService.getPageDepDocRefCmd().getClass());
    verifyDefault();
  }

  @Test
  public void testGetPageDepDocRefCmd_inject() {
    cellsScriptService.inject_pageDepDocRefCmd(mockPageDepDocRefCmd);
    assertSame(mockPageDepDocRefCmd, cellsScriptService.getPageDepDocRefCmd());
  }

  @Test
  public void testGetPageDependentDocRef_DocumentReference() {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myExpectedDoc");
    expect(mockPageDepDocRefCmd.getDocumentReference(eq(currentDocRef), same(cellDocRef)))
        .andReturn(expectedDocRef).once();
    replayDefault();
    assertEquals(expectedDocRef, cellsScriptService.getPageDependentDocRef(cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetPageDependentDocRef_DocumentReferenceDocumentReference() throws Exception {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myExpectedDoc");
    expect(mockPageDepDocRefCmd.getDocumentReference(eq(currentDocRef), same(cellDocRef)))
        .andReturn(expectedDocRef).once();
    replayDefault();
    assertEquals(expectedDocRef,
        cellsScriptService.getPageDependentDocRef(currentDocRef, cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetPageDependentTranslatedDocument() throws XWikiException {
    XWikiDocument expectedXDoc = createDefaultMock(XWikiDocument.class);
    expect(mockPageDepDocRefCmd.getTranslatedDocument(same(currentXDoc), same(cellDocRef)))
        .andReturn(expectedXDoc).once();
    expect(currentDoc.getDocumentReference()).andReturn(currentDocRef).anyTimes();
    expect(xwiki.getDocument(eq(currentDocRef), same(context))).andReturn(currentXDoc).once();
    expect(currentDoc.getLanguage()).andReturn("").once();
    Document expectedDoc = new Document(expectedXDoc, context);
    expect(expectedXDoc.newDocument(same(context))).andReturn(expectedDoc).once();
    replayDefault();
    assertSame(expectedDoc,
        cellsScriptService.getPageDependentTranslatedDocument(currentDoc, cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetPageDependentTranslatedDocument_translations() throws XWikiException {
    XWikiDocument expectedXDoc = createDefaultMock(XWikiDocument.class);
    expect(mockPageDepDocRefCmd.getTranslatedDocument(same(currentXDoc), same(cellDocRef)))
        .andReturn(expectedXDoc).once();
    expect(currentDoc.getDocumentReference()).andReturn(currentDocRef).anyTimes();
    XWikiDocument currentXDocDef = createDefaultMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(currentDocRef), same(context))).andReturn(currentXDocDef).once();
    expect(currentXDocDef.getTranslatedDocument(eq("fr"), same(context))).andReturn(currentXDoc)
        .once();
    expect(currentDoc.getLanguage()).andReturn("fr").atLeastOnce();
    Document expectedDoc = new Document(expectedXDoc, context);
    expect(expectedXDoc.newDocument(same(context))).andReturn(expectedDoc).once();
    replayDefault();
    assertSame(expectedDoc,
        cellsScriptService.getPageDependentTranslatedDocument(currentDoc, cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetPageDependentTranslatedDocument_Exception() throws XWikiException {
    try {
      expect(mockPageDepDocRefCmd.getTranslatedDocument(same(currentXDoc), same(cellDocRef)))
          .andThrow(new XWikiException()).once();
      expect(currentDoc.getDocumentReference()).andReturn(currentDocRef).anyTimes();
      expect(xwiki.getDocument(eq(currentDocRef), same(context))).andReturn(currentXDoc).once();
      expect(currentDoc.getLanguage()).andReturn("").once();
      replayDefault();
      assertSame(currentDoc,
          cellsScriptService.getPageDependentTranslatedDocument(currentDoc, cellDocRef));
      verifyDefault();
    } catch (XWikiException exp) {
      fail("Expecting to catch XWikiException and return currentDoc.");
    }
  }

}
