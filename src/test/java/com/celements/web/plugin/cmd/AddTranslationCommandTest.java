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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.XWikiDocumentCreator;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class AddTranslationCommandTest extends AbstractComponentTest {

  private AddTranslationCommand addTransCmd;

  @Before
  public void prepare() throws Exception {
    registerComponentMock(XWikiDocumentCreator.class);
    addTransCmd = new AddTranslationCommand();
  }

  private XWikiDocument expectNewDoc(DocumentReference docRef, String lang) {
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setLanguage(lang);
    doc.setDefaultLanguage("default");
    expect(getMock(XWikiDocumentCreator.class).create(docRef, lang)).andReturn(doc);
    return doc;
  }

  @Test
  public void testAddTranslation_noDoc() {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MyTestSpace",
        "MyTestDoc");
    XWiki xwiki = createMock(XWiki.class);
    expect(xwiki.exists(eq(docRef), same(getContext()))).andReturn(false);
    getContext().setWiki(xwiki);
    replayDefault(xwiki);
    assertFalse("expecting true if translation successfully created", addTransCmd.addTranslation(
        docRef, "fr"));
    verifyDefault(xwiki);
  }

  @Test
  @Deprecated
  public void testAddTranslation_noDoc_deprecated() {
    String fullName = "MyTestSpace.MyTestDoc";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MyTestSpace",
        "MyTestDoc");
    XWiki xwiki = createMock(XWiki.class);
    expect(xwiki.exists(eq(docRef), same(getContext()))).andReturn(false);
    getContext().setWiki(xwiki);
    replayDefault(xwiki);
    assertFalse("expecting true if translation successfully created", addTransCmd.addTranslation(
        fullName, "fr", getContext()));
    verifyDefault(xwiki);
  }

  @Test
  public void testAddTranslation_already_exists() throws XWikiException {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MyTestSpace",
        "MyTestDoc");
    XWiki xwiki = createMock(XWiki.class);
    expect(xwiki.exists(eq(docRef), same(getContext()))).andReturn(true);
    XWikiDocument mainDoc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(mainDoc);
    expect(mainDoc.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(mainDoc.getDefaultLanguage()).andReturn("de");
    XWikiDocument transDoc = new XWikiDocument(docRef);
    transDoc.setLanguage("fr");
    transDoc.setDefaultLanguage("fr");
    transDoc.setNew(false);
    expect(mainDoc.getTranslatedDocument(eq("fr"), same(getContext()))).andReturn(transDoc);
    xwiki.saveDocument(same(transDoc), same(getContext()));
    expectLastCall();
    getContext().setWiki(xwiki);
    replayDefault(xwiki, mainDoc);
    assertFalse("expecting false if no new translation was created", addTransCmd.addTranslation(
        docRef, "fr"));
    assertEquals("expecting translation flag to be set to 1.", 1, transDoc.getTranslation());
    verifyDefault(xwiki, mainDoc);
  }

  @Test
  @Deprecated
  public void testAddTranslation_already_exists_deprecated() throws XWikiException {
    String fullName = "MyTestSpace.MyTestDoc";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MyTestSpace",
        "MyTestDoc");
    XWiki xwiki = createMock(XWiki.class);
    expect(xwiki.exists(eq(docRef), same(getContext()))).andReturn(true);
    XWikiDocument mainDoc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(mainDoc);
    expect(mainDoc.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(mainDoc.getDefaultLanguage()).andReturn("de");
    XWikiDocument transDoc = new XWikiDocument(docRef);
    transDoc.setLanguage("fr");
    transDoc.setDefaultLanguage("fr");
    transDoc.setNew(false);
    expect(mainDoc.getTranslatedDocument(eq("fr"), same(getContext()))).andReturn(transDoc);
    xwiki.saveDocument(same(transDoc), same(getContext()));
    expectLastCall();
    getContext().setWiki(xwiki);
    replayDefault(xwiki, mainDoc);
    assertFalse("expecting false if no new translation was created", addTransCmd.addTranslation(
        fullName, "fr", getContext()));
    assertEquals("expecting translation flag to be set to 1.", 1, transDoc.getTranslation());
    verifyDefault(xwiki, mainDoc);
  }

  @Test
  public void testAddTranslation_create_translation() throws XWikiException {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MyTestSpace",
        "MyTestDoc");
    String mainDocContent = " The content \n of the main document copied into trans.";
    XWiki xwiki = createMock(XWiki.class);
    expect(xwiki.exists(eq(docRef), same(getContext()))).andReturn(true);
    expect(xwiki.isMultiLingual(same(getContext()))).andReturn(true);
    XWikiDocument mainDoc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(mainDoc);
    expect(mainDoc.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(mainDoc.getContent()).andReturn(mainDocContent).anyTimes();
    XWikiStoreInterface mockStore = createMock(XWikiStoreInterface.class);
    expect(mainDoc.getStore()).andReturn(mockStore).anyTimes();
    expect(mainDoc.getDefaultLanguage()).andReturn("de").anyTimes();
    expect(mainDoc.getTranslatedDocument(eq("fr"), same(getContext()))).andReturn(mainDoc);
    XWikiDocument transDoc = expectNewDoc(docRef, "fr");
    xwiki.saveDocument(same(transDoc), same(getContext()));
    getContext().setWiki(xwiki);
    replayDefault(xwiki, mainDoc);
    assertTrue("expecting true if translation successfully created", addTransCmd.addTranslation(
        docRef, "fr"));
    assertEquals("expecting document language to be set.", "fr", transDoc.getLanguage());
    assertEquals("expecting document default language to be untouched", "default",
        transDoc.getDefaultLanguage());
    assertEquals("expecting translation flag to be set to 1.", 1, transDoc.getTranslation());
    assertTrue("expecting metadata dirty status set to true.", transDoc.isMetaDataDirty());
    assertTrue("expecting transdoc to be 'new'.", transDoc.isNew());
    verifyDefault(xwiki, mainDoc);
  }

  @Test
  @Deprecated
  public void testAddTranslation_create_translation_deprecated() throws XWikiException {
    String fullName = "MyTestSpace.MyTestDoc";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MyTestSpace",
        "MyTestDoc");
    String mainDocContent = " The content \n of the main document copied into trans.";
    XWiki xwiki = createMock(XWiki.class);
    expect(xwiki.exists(eq(docRef), same(getContext()))).andReturn(true);
    expect(xwiki.isMultiLingual(same(getContext()))).andReturn(true);
    XWikiDocument mainDoc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(docRef), same(getContext()))).andReturn(mainDoc);
    expect(mainDoc.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(mainDoc.getContent()).andReturn(mainDocContent).anyTimes();
    XWikiStoreInterface mockStore = createMock(XWikiStoreInterface.class);
    expect(mainDoc.getStore()).andReturn(mockStore).anyTimes();
    expect(mainDoc.getDefaultLanguage()).andReturn("de").anyTimes();
    expect(mainDoc.getTranslatedDocument(eq("fr"), same(getContext()))).andReturn(mainDoc);
    XWikiDocument transDoc = expectNewDoc(docRef, "fr");
    xwiki.saveDocument(same(transDoc), same(getContext()));
    getContext().setWiki(xwiki);
    replayDefault(xwiki, mainDoc);
    assertTrue("expecting true if translation successfully created", addTransCmd.addTranslation(
        fullName, "fr", getContext()));
    assertEquals("expecting document language to be set.", "fr", transDoc.getLanguage());
    assertEquals("expecting document default language to be untouched", "default",
        transDoc.getDefaultLanguage());
    assertEquals("expecting translation flag to be set to 1.", 1, transDoc.getTranslation());
    assertTrue("expecting metadata dirty status set to true.", transDoc.isMetaDataDirty());
    assertTrue("expecting transdoc to be 'new'.", transDoc.isNew());
    verifyDefault(xwiki, mainDoc);
  }

  @Test
  public void testCreateTranslationDoc_create_translation() throws XWikiException {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MyTestSpace",
        "MyTestDoc");
    String mainDocContent = " The Content \n of the main Document.";
    XWiki xwiki = createMock(XWiki.class);
    expect(xwiki.isMultiLingual(same(getContext()))).andReturn(true).atLeastOnce();
    XWikiDocument mainDoc = createMock(XWikiDocument.class);
    expect(mainDoc.getDocumentReference()).andReturn(docRef).anyTimes();
    expect(mainDoc.getContent()).andReturn(mainDocContent).anyTimes();
    XWikiStoreInterface mockStore = createMock(XWikiStoreInterface.class);
    expect(mainDoc.getStore()).andReturn(mockStore).anyTimes();
    expect(mainDoc.getDefaultLanguage()).andReturn("de").anyTimes();
    expect(mainDoc.getTranslatedDocument(eq("fr"), same(getContext()))).andReturn(mainDoc);
    getContext().setWiki(xwiki);
    XWikiDocument transDoc = expectNewDoc(docRef, "fr");
    replayDefault(xwiki, mainDoc);
    assertSame(transDoc, addTransCmd.createTranslationDoc(mainDoc, "fr"));
    assertNotSame("new XWikiDocument being created.", mainDoc, transDoc);
    assertEquals("expecting document language to be set.", "fr", transDoc.getLanguage());
    assertEquals("expecting document default language to be untouched", "default",
        transDoc.getDefaultLanguage());
    assertEquals("expecting translation flag to be set to 1.", 1, transDoc.getTranslation());
    assertTrue("expecting metadata dirty status set to true.", transDoc.isMetaDataDirty());
    assertTrue("expecting transdoc to be 'new'.", transDoc.isNew());
    assertEquals("main doc content must be copied in translation.", mainDocContent,
        transDoc.getContent());
    verifyDefault(xwiki, mainDoc);
  }

}
