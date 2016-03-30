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
package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeRole;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class CreateDocumentCommandTest extends AbstractBridgedComponentTestCase {

  private CreateDocumentCommand createDocumentCmd;
  private IPageTypeRole pageTypeServiceMock;

  @Before
  public void setUp_CreateDocumentCommandTest() throws Exception {
    pageTypeServiceMock = registerComponentMock(IPageTypeRole.class);
    createDocumentCmd = new CreateDocumentCommand();
  }

  @Test
  public void testCreateDocument_exists() throws Exception {
    String pageType = "RichText";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(),
        "mySpace", "myNewDocument");
    expect(getWikiMock().exists(eq(docRef), same(getContext()))).andReturn(true).once();
    replayDefault();
    assertNull("only create document if NOT exists.",
        createDocumentCmd.createDocument(docRef, pageType));
    verifyDefault();
  }

  @Test
  public void testCreateDocument_noPageType() throws Exception {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(),
        "mySpace", "myNewDocument");
    expect(getWikiMock().exists(eq(docRef), same(getContext()))).andReturn(false).once();
    XWikiDocument theNewDoc = new XWikiDocument(docRef);
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(theNewDoc)
        .once();
    expect(pageTypeServiceMock.getPageTypeRefByConfigName(eq(""))).andReturn(null).once();
    getWikiMock().saveDocument(same(theNewDoc), eq("init document"), eq(false),
        same(getContext()));
    expectLastCall().once();
    replayDefault();
    XWikiDocument theDoc = createDocumentCmd.createDocument(docRef, null);
    assertNotNull(theDoc);
    assertSame(theNewDoc, theDoc);
    verifyDefault();
  }

  @Test
  public void testCreateDocument() throws Exception {
    String pageType = "RichText";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(),
        "mySpace", "myNewDocument");
    expect(getWikiMock().exists(eq(docRef), same(getContext()))).andReturn(false).once();
    XWikiDocument theNewDoc = new XWikiDocument(docRef);
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(theNewDoc)
        .once();
    PageTypeReference ptRef = new PageTypeReference(pageType, "",
        Collections.<String>emptyList());
    expect(pageTypeServiceMock.getPageTypeRefByConfigName(eq(pageType))).andReturn(ptRef)
        .once();
    expect(pageTypeServiceMock.setPageType(same(theNewDoc), eq(ptRef))).andReturn(true)
        .once();
    getWikiMock().saveDocument(same(theNewDoc), eq("init RichText-document"), eq(false),
        same(getContext()));
    expectLastCall().once();
    replayDefault();
    XWikiDocument theDoc = createDocumentCmd.createDocument(docRef, pageType);
    assertNotNull(theDoc);
    verifyDefault();
  }

  @Test
  public void testCreateDocument_noSave() throws Exception {
    String pageType = "RichText";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(),
        "mySpace", "myNewDocument");
    expect(getWikiMock().exists(eq(docRef), same(getContext()))).andReturn(false).once();
    XWikiDocument theNewDoc = new XWikiDocument(docRef);
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(theNewDoc)
        .once();
    PageTypeReference ptRef = new PageTypeReference(pageType, "",
        Collections.<String>emptyList());
    expect(pageTypeServiceMock.getPageTypeRefByConfigName(eq(pageType))).andReturn(ptRef)
        .once();
    expect(pageTypeServiceMock.setPageType(same(theNewDoc), eq(ptRef))).andReturn(true)
        .once();
    replayDefault();
    XWikiDocument theDoc = createDocumentCmd.createDocument(docRef, pageType, false);
    assertNotNull(theDoc);
    verifyDefault();
  }

  @Test
  public void testCreateDocument_XWE() throws Exception {
    String pageType = "RichText";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(),
        "mySpace", "myNewDocument");
    expect(getWikiMock().exists(eq(docRef), same(getContext()))).andReturn(false).once();
    expect(getWikiMock().getDocument(eq(docRef), same(getContext())))
        .andThrow(new XWikiException()).once();
    replayDefault();
    try {
      createDocumentCmd.createDocument(docRef, pageType, false);
      fail("expecting XWE");
    } catch (XWikiException xwe) {
      // expected
    }
    verifyDefault();
  }

}
