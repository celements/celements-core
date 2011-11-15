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

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class AddTranslationCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private AddTranslationCommand addTransCmd;

  @Before
  public void setUp_AddTranslationCommandTest() throws Exception {
    context = getContext();
    addTransCmd = new AddTranslationCommand();
  }

  @Test
  public void testAddTranslation_noDoc() {
    String fullName = "MyTestSpace.MyTestDoc";
    XWiki xwiki = createMock(XWiki.class);
    expect(xwiki.exists(eq(fullName), same(context))).andReturn(false);
    context.setWiki(xwiki);
    replay(xwiki);
    assertFalse("expecting true if translation successfully created",
        addTransCmd.addTranslation(fullName, "fr", context));
    verify(xwiki);
  }

  @Test
  public void testAddTranslation_already_exists() throws XWikiException {
    String fullName = "MyTestSpace.MyTestDoc";
    XWiki xwiki = createMock(XWiki.class);
    expect(xwiki.exists(eq(fullName), same(context))).andReturn(true);
    XWikiDocument mainDoc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(fullName), same(context))).andReturn(mainDoc);
    expect(mainDoc.getFullName()).andReturn(fullName).anyTimes();
    expect(mainDoc.getDefaultLanguage()).andReturn("de");
    XWikiDocument transDoc = new XWikiDocument();
    transDoc.setLanguage("fr");
    transDoc.setDefaultLanguage("fr");
    transDoc.setNew(false);
    expect(mainDoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(transDoc);
    xwiki.saveDocument(same(transDoc), same(context));
    expectLastCall();
    context.setWiki(xwiki);
    replay(xwiki, mainDoc);
    assertFalse("expecting false if no new translation was created",
        addTransCmd.addTranslation(fullName, "fr", context));
    assertEquals("expecting translation flag to be set to 1.", 1,
        transDoc.getTranslation());
    verify(xwiki, mainDoc);
  }

  @Test
  public void testAddTranslation_create_translation() throws XWikiException {
    String fullName = "MyTestSpace.MyTestDoc";
    String mainDocContent = " The content \n of the main document copied into trans.";
    XWiki xwiki = createMock(XWiki.class);
    expect(xwiki.exists(eq(fullName), same(context))).andReturn(true);
    expect(xwiki.isMultiLingual(same(context))).andReturn(true);
    XWikiDocument mainDoc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(fullName), same(context))).andReturn(mainDoc);
    expect(mainDoc.getFullName()).andReturn(fullName).anyTimes();
    expect(mainDoc.getSpace()).andReturn("MyTestSpace").anyTimes();
    expect(mainDoc.getName()).andReturn("MyTestDoc").anyTimes();
    expect(mainDoc.getContent()).andReturn(mainDocContent).anyTimes();
    XWikiStoreInterface mockStore = createMock(XWikiStoreInterface.class);
    expect(mainDoc.getStore()).andReturn(mockStore).anyTimes();
    expect(mainDoc.getDefaultLanguage()).andReturn("de").anyTimes();
    expect(mainDoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(mainDoc);
    Capture<XWikiDocument> transDocCapture = new Capture<XWikiDocument>();
    xwiki.saveDocument(capture(transDocCapture), same(context));
    expectLastCall();
    context.setWiki(xwiki);
    replay(xwiki, mainDoc);
    assertTrue("expecting true if translation successfully created",
        addTransCmd.addTranslation(fullName, "fr", context));
    XWikiDocument transDoc = transDocCapture.getValue();
    assertEquals("expecting document language to be set.", "fr",
        transDoc.getLanguage());
    assertEquals("expecting document default language to be set.", "de",
        transDoc.getDefaultLanguage());
    assertEquals("expecting translation flag to be set to 1.", 1,
        transDoc.getTranslation());
    assertTrue("expecting metadata dirty status set to true.",
        transDoc.isMetaDataDirty());
    assertTrue("expecting transdoc to be 'new'.", transDoc.isNew());
    verify(xwiki, mainDoc);
  }

  @Test
  public void testCreateTranslationDoc_create_translation() throws XWikiException {
    String fullName = "MyTestSpace.MyTestDoc";
    String mainDocContent = " The Content \n of the main Document.";
    XWiki xwiki = createMock(XWiki.class);
    expect(xwiki.isMultiLingual(same(context))).andReturn(true).atLeastOnce();
    XWikiDocument mainDoc = createMock(XWikiDocument.class);
    expect(mainDoc.getFullName()).andReturn(fullName).anyTimes();
    expect(mainDoc.getSpace()).andReturn("MyTestSpace").anyTimes();
    expect(mainDoc.getName()).andReturn("MyTestDoc").anyTimes();
    expect(mainDoc.getContent()).andReturn(mainDocContent).anyTimes();
    XWikiStoreInterface mockStore = createMock(XWikiStoreInterface.class);
    expect(mainDoc.getStore()).andReturn(mockStore).anyTimes();
    expect(mainDoc.getDefaultLanguage()).andReturn("de").anyTimes();
    expect(mainDoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(mainDoc);
    context.setWiki(xwiki);
    replay(xwiki, mainDoc);
    XWikiDocument transDoc = addTransCmd.createTranslationDoc(mainDoc, "fr", context);
    assertNotSame("new XWikiDocument being created.", mainDoc, transDoc);
    assertEquals("expecting document language to be set.", "fr", transDoc.getLanguage());
    assertEquals("expecting document default language to be set.", "de",
        transDoc.getDefaultLanguage());
    assertEquals("expecting translation flag to be set to 1.", 1,
        transDoc.getTranslation());
    assertTrue("expecting metadata dirty status set to true.",
        transDoc.isMetaDataDirty());
    assertTrue("expecting transdoc to be 'new'.", transDoc.isNew());
    assertEquals("main doc content must be copied in translation.", mainDocContent,
        transDoc.getContent());
    verify(xwiki, mainDoc);
  }

}
