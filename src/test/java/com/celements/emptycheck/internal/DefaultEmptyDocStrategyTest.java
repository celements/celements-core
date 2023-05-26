package com.celements.emptycheck.internal;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class DefaultEmptyDocStrategyTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private DefaultEmptyDocStrategy defEmptyDocStrategy;

  @Before
  public void setUp_DefaultEmptyDocStrategyTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    defEmptyDocStrategy = (DefaultEmptyDocStrategy) Utils.getComponent(
        IDefaultEmptyDocStrategyRole.class, "default");
  }

  @Test
  public void testIsEmptyRTEString_empty() {
    assertTrue(defEmptyDocStrategy.isEmptyRTEString(""));
  }

  @Test
  public void testIsEmptyRTEString_cel2_standard_oldRTE_2Space() {
    assertTrue("Lonly non breaking spaces (2) with break should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEString("<p>&nbsp;&nbsp;</p>"));
  }

  @Test
  public void testIsEmptyRTEString_cel2_standard_oldRTE_1Break() {
    assertTrue("Lonly non breaking spaces (2) with break should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEString("<p><br /></p>"));
  }

  @Test
  public void testIsEmptyRTEString_manualizer_example() {
    assertTrue("Paragraph with span surrounding break should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEString(
            "<p><span style=\"line-height: normal; font-size: 10px;\"><br /></span></p>"));
  }

  @Test
  public void testIsEmptyRTEString_cel2_standard_oldRTE_2Space1Break() {
    assertTrue("Lonly non breaking spaces (2) with break should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEString("<p>&nbsp;&nbsp; <br /></p>"));
  }

  @Test
  public void testIsEmptyRTEString_cel2_standard_oldRTE_3Space1Break() {
    assertTrue("Lonly non breaking spaces (3) with break should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEString("<p>&nbsp;&nbsp;&nbsp;<br /></p>"));
  }

  @Test
  public void testIsEmptyRTEString_cel2_standard_oldRTE_1Space2Break() {
    assertTrue("Lonly non breaking spaces with break (2) should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEString("<p>&nbsp;<br /><br /></p>"));
  }

  @Test
  public void testIsEmptyRTEString_cel2_standard_oldRTE_REGULAR_TEXT() {
    assertFalse("Regular Text (2) should not be treated as empty.",
        defEmptyDocStrategy.isEmptyRTEString("<p>adsf  &nbsp; <br />sadf</p>"));
  }

  @Test
  public void testIsEmptyRTEString_nbsp() {
    assertTrue("Lonly non breaking spaces should be treated as empty",
        defEmptyDocStrategy.isEmptyRTEString("&nbsp;"));
    assertTrue("Non breaking spaces in a paragraph should be treated as empty",
        defEmptyDocStrategy.isEmptyRTEString("<p>&nbsp;</p>"));
    assertTrue("Non breaking spaces in a paragraph with white spaces"
        + " should be treated as empty", defEmptyDocStrategy.isEmptyRTEString("<p>  &nbsp; </p>"));
    assertFalse("Regular Text should not be treated as empty.",
        defEmptyDocStrategy.isEmptyRTEString("<p>adsf  &nbsp; </p>"));
  }

  @Test
  public void testIsEmptyDocument_XWikiDocument_empty() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = new XWikiDocument(docRef);
    myDoc.setContent("");
    replayDefault();
    assertTrue(defEmptyDocStrategy.isEmptyDocument(myDoc));
    verifyDefault();
  }

  @Test
  public void testIsEmptyDocument_XWikiDocument_notEmpty() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = new XWikiDocument(docRef);
    myDoc.setContent("abcd");
    replayDefault();
    assertFalse(defEmptyDocStrategy.isEmptyDocument(myDoc));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocumentDefault_empty() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = new XWikiDocument(docRef);
    myDoc.setContent("");
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    replayDefault();
    assertTrue(defEmptyDocStrategy.isEmptyRTEDocumentDefault(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocumentDefault_notEmpty() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = new XWikiDocument(docRef);
    myDoc.setContent("abcd");
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    replayDefault();
    assertFalse(defEmptyDocStrategy.isEmptyRTEDocumentDefault(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocumentDefault_Exception() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    expect(xwiki.getDocument(eq(docRef), same(context))).andThrow(
        new XWikiException()).atLeastOnce();
    replayDefault();
    assertTrue(defEmptyDocStrategy.isEmptyRTEDocumentDefault(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocumentTranslated_empty() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = createDefaultMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    XWikiDocument myTDoc = new XWikiDocument(docRef);
    myTDoc.setContent("");
    expect(myDoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(myTDoc).atLeastOnce();
    replayDefault();
    assertTrue(defEmptyDocStrategy.isEmptyRTEDocumentTranslated(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocumentTranslated_notEmpty() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = createDefaultMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    XWikiDocument myTDoc = new XWikiDocument(docRef);
    myTDoc.setContent("asfd");
    expect(myDoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(myTDoc).atLeastOnce();
    replayDefault();
    assertFalse(defEmptyDocStrategy.isEmptyRTEDocumentTranslated(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocumentTranslated_Exception() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    expect(xwiki.getDocument(eq(docRef), same(context))).andThrow(
        new XWikiException()).atLeastOnce();
    replayDefault();
    assertTrue(defEmptyDocStrategy.isEmptyRTEDocumentTranslated(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocument_notEmpty_default() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = new XWikiDocument(docRef);
    myDoc.setContent("abcd");
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    replayDefault();
    assertFalse(defEmptyDocStrategy.isEmptyRTEDocument(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocument_notEmpty_translations() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = createDefaultMock(XWikiDocument.class);
    expect(myDoc.getContent()).andReturn("").anyTimes();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    XWikiDocument myTDoc = new XWikiDocument(docRef);
    myTDoc.setContent("asdf");
    expect(myDoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(myTDoc).atLeastOnce();
    replayDefault();
    assertFalse(defEmptyDocStrategy.isEmptyRTEDocument(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocument_empty_translations() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = createDefaultMock(XWikiDocument.class);
    expect(myDoc.getContent()).andReturn("").anyTimes();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    XWikiDocument myTDoc = new XWikiDocument(docRef);
    myTDoc.setContent("");
    expect(myDoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(myTDoc).atLeastOnce();
    replayDefault();
    assertTrue(defEmptyDocStrategy.isEmptyRTEDocument(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyDocumentDefault_empty() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = new XWikiDocument(docRef);
    myDoc.setContent("");
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    replayDefault();
    assertTrue(defEmptyDocStrategy.isEmptyDocumentDefault(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyDocumentDefault_notEmpty() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = new XWikiDocument(docRef);
    myDoc.setContent("abcd");
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    replayDefault();
    assertFalse(defEmptyDocStrategy.isEmptyDocumentDefault(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyDocumentDefault_Exception() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    expect(xwiki.getDocument(eq(docRef), same(context))).andThrow(
        new XWikiException()).atLeastOnce();
    replayDefault();
    assertTrue(defEmptyDocStrategy.isEmptyDocumentDefault(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyDocumentTranslated_empty() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = createDefaultMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    XWikiDocument myTDoc = new XWikiDocument(docRef);
    myTDoc.setContent("");
    expect(myDoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(myTDoc).atLeastOnce();
    replayDefault();
    assertTrue(defEmptyDocStrategy.isEmptyDocumentTranslated(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyDocumentTranslated_notEmpty() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = createDefaultMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    XWikiDocument myTDoc = new XWikiDocument(docRef);
    myTDoc.setContent("asfd");
    expect(myDoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(myTDoc).atLeastOnce();
    replayDefault();
    assertFalse(defEmptyDocStrategy.isEmptyDocumentTranslated(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyDocumentTranslated_Exception() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    expect(xwiki.getDocument(eq(docRef), same(context))).andThrow(
        new XWikiException()).atLeastOnce();
    replayDefault();
    assertTrue(defEmptyDocStrategy.isEmptyDocumentTranslated(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyDocument_notEmpty_default() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = new XWikiDocument(docRef);
    myDoc.setContent("abcd");
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    replayDefault();
    assertFalse(defEmptyDocStrategy.isEmptyDocument(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyDocument_notEmpty_translations() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = createDefaultMock(XWikiDocument.class);
    expect(myDoc.getContent()).andReturn("").anyTimes();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    XWikiDocument myTDoc = new XWikiDocument(docRef);
    myTDoc.setContent("asdf");
    expect(myDoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(myTDoc).atLeastOnce();
    replayDefault();
    assertFalse(defEmptyDocStrategy.isEmptyDocument(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyDocument_empty_translations() throws Exception {
    context.setLanguage("fr");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiDocument myDoc = createDefaultMock(XWikiDocument.class);
    expect(myDoc.getContent()).andReturn("").anyTimes();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(myDoc).atLeastOnce();
    XWikiDocument myTDoc = new XWikiDocument(docRef);
    myTDoc.setContent("");
    expect(myDoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(myTDoc).atLeastOnce();
    replayDefault();
    assertTrue(defEmptyDocStrategy.isEmptyDocument(docRef));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocument_empty() {
    assertTrue(defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc("")));
  }

  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_2Space() {
    replayDefault();
    assertTrue("Lonly non breaking spaces (2) with break should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc("<p>&nbsp;&nbsp;</p>")));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_1Break() {
    replayDefault();
    assertTrue("Lonly non breaking spaces (2) with break should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc("<p><br /></p>")));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocument_manualizer_example() {
    replayDefault();
    assertTrue("Paragraph with span surrounding break should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc(
            "<p><span style=\"line-height: normal; font-size: 10px;\"><br /></span></p>")));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_2Space1Break() {
    replayDefault();
    assertTrue("Lonly non breaking spaces (2) with break should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc("<p>&nbsp;&nbsp; <br /></p>")));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_3Space1Break() {
    replayDefault();
    assertTrue("Lonly non breaking spaces (3) with break should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc("<p>&nbsp;&nbsp;&nbsp;<br /></p>")));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_1Space2Break() {
    replayDefault();
    assertTrue("Lonly non breaking spaces with break (2) should be" + " treated as empty",
        defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc("<p>&nbsp;<br /><br /></p>")));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_REGULAR_TEXT() {
    replayDefault();
    assertFalse("Regular Text (2) should not be treated as empty.",
        defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc("<p>adsf  &nbsp; <br />sadf</p>")));
    verifyDefault();
  }

  @Test
  public void testIsEmptyRTEDocument_nbsp() {
    replayDefault();
    assertTrue("Lonly non breaking spaces should be treated as empty",
        defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc("&nbsp;")));
    assertTrue("Non breaking spaces in a paragraph should be treated as empty",
        defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc("<p>&nbsp;</p>")));
    assertTrue("Non breaking spaces in a paragraph with white spaces"
        + " should be treated as empty", defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc(
            "<p>  &nbsp; </p>")));
    assertFalse("Regular Text should not be treated as empty.",
        defEmptyDocStrategy.isEmptyRTEDocument(getTestDoc("<p>adsf  &nbsp; </p>")));
    verifyDefault();
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private XWikiDocument getTestDoc(String inStr) {
    DocumentReference testDocRef = new DocumentReference("xwiki", "testSpace", "testDoc");
    XWikiDocument doc = new XWikiDocument(testDocRef);
    doc.setContent(inStr);
    return doc;
  }

}
