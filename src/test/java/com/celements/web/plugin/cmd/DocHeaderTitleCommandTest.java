package com.celements.web.plugin.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class DocHeaderTitleCommandTest extends AbstractComponentTest {

  DocHeaderTitleCommand command;
  DocumentReference docRef;
  XWikiDocument doc;
  XWiki wiki;

  @Before
  public void setUp_DocHeaderTitleCommandTest() throws Exception {
    command = new DocHeaderTitleCommand();
    docRef = new DocumentReference(getContext().getDatabase(), "Content", "Home");
    doc = createMock(XWikiDocument.class);
    getContext().setDoc(doc);
    wiki = createMock(XWiki.class);
    getContext().setWiki(wiki);
  }

  @Test
  public void testGetDocHeaderTitle_doc_theDoc_sameSpace() throws XWikiException {
    String spacePrefTitle = " - " + docRef.getLastSpaceReference().getName();
    expect(wiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc);
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(doc);
    expect(doc.getTitle()).andReturn("").anyTimes();
    DocumentReference objRef = new DocumentReference(getContext().getDatabase(), "Content",
        "Title");
    BaseObject obj = new BaseObject();
    expect(doc.getXObject(eq(objRef))).andReturn(obj);
    MultilingualMenuNameCommand mmnc = createMock(MultilingualMenuNameCommand.class);
    command.menuNameCmd = mmnc;
    expect(mmnc.getMultilingualMenuNameOnly(eq("Content.Home"), eq("de"), eq(false), same(
        getContext()))).andReturn("Home");
    expect(wiki.getSpacePreference(eq("title"), eq(docRef.getLastSpaceReference().getName()), eq(
        ""), same(getContext()))).andReturn(spacePrefTitle).anyTimes();
    expect(wiki.parseContent(eq(spacePrefTitle), same(getContext()))).andReturn(spacePrefTitle);
    replay(doc, mmnc, wiki);
    assertEquals(docRef.getName() + spacePrefTitle, command.getDocHeaderTitle(docRef));
    verify(doc, mmnc, wiki);
  }

  @Test
  public void testGetDocHeaderTitle_doc_theDoc_differentSpace() throws XWikiException {
    DocumentReference theDocRef = new DocumentReference(getContext().getDatabase(), "TheDocSpace",
        "TheDoc");
    String spacePrefTitle = " - " + theDocRef.getLastSpaceReference().getName();
    XWikiDocument theDoc = createMock(XWikiDocument.class);
    expect(wiki.getDocument(eq(theDocRef), same(getContext()))).andReturn(theDoc);
    expect(theDoc.getTranslatedDocument(same(getContext()))).andReturn(theDoc);
    expect(theDoc.getTitle()).andReturn("").anyTimes();
    DocumentReference objRef = new DocumentReference(getContext().getDatabase(), "Content",
        "Title");
    BaseObject obj = new BaseObject();
    expect(theDoc.getXObject(eq(objRef))).andReturn(obj);
    MultilingualMenuNameCommand mmnc = createMock(MultilingualMenuNameCommand.class);
    command.menuNameCmd = mmnc;
    expect(mmnc.getMultilingualMenuNameOnly(eq("TheDocSpace.TheDoc"), eq("de"), eq(false), same(
        getContext()))).andReturn("TheDoc");
    expect(wiki.getSpacePreference(eq("title"), eq(theDocRef.getLastSpaceReference().getName()), eq(
        ""), same(getContext()))).andReturn(spacePrefTitle).anyTimes();
    expect(wiki.parseContent(eq(spacePrefTitle), same(getContext()))).andReturn(spacePrefTitle);
    replay(doc, mmnc, theDoc, wiki);
    assertEquals(theDocRef.getName() + " - TheDocSpace", command.getDocHeaderTitle(theDocRef));
    verify(doc, mmnc, theDoc, wiki);
  }

  @Deprecated
  @Test
  public void testGetDocHeaderTitle_fullName() throws XWikiException {
    String spacePrefTitle = " - " + docRef.getLastSpaceReference().getName();
    expect(wiki.getDocument(eq(docRef), same(getContext()))).andReturn(doc);
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(doc);
    expect(doc.getTitle()).andReturn("").anyTimes();
    DocumentReference objRef = new DocumentReference(getContext().getDatabase(), "Content",
        "Title");
    BaseObject obj = new BaseObject();
    expect(doc.getXObject(eq(objRef))).andReturn(obj);
    MultilingualMenuNameCommand mmnc = createMock(MultilingualMenuNameCommand.class);
    command.menuNameCmd = mmnc;
    expect(mmnc.getMultilingualMenuNameOnly(eq("Content.Home"), eq("de"), eq(false), same(
        getContext()))).andReturn("Home");
    expect(wiki.getSpacePreference(eq("title"), eq(docRef.getLastSpaceReference().getName()), eq(
        ""), same(getContext()))).andReturn(spacePrefTitle).anyTimes();
    expect(wiki.parseContent(eq(spacePrefTitle), same(getContext()))).andReturn(spacePrefTitle);
    replay(doc, mmnc, wiki);
    assertEquals(docRef.getName() + spacePrefTitle, command.getDocHeaderTitle("Content.Home",
        getContext()));
    verify(doc, mmnc, wiki);
  }
}
