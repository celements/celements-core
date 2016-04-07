package com.celements.filebase;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.common.test.TestMessageTool;
import com.celements.filebase.matcher.IAttFileNameMatcherRole;
import com.celements.filebase.matcher.IAttachmentMatcher;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiAttachmentStoreInterface;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

public class AttachmentServiceTest extends AbstractBridgedComponentTestCase {

  private AttachmentService attService;
  private XWikiDocument doc;
  private XWikiStoreInterface xwikiStoreMock;
  
  @Before
  public void setUp_AttachmentServiceTest() throws ComponentLookupException, Exception {
    attService = (AttachmentService) Utils.getComponent(IAttachmentServiceRole.class);
    doc = new XWikiDocument(new DocumentReference("db", "space", "doc"));
    doc.setMetaDataDirty(false);
    xwikiStoreMock = createMockAndAddToDefault(XWikiStoreInterface.class);
    doc.setStore(xwikiStoreMock);
    doc.setNew(false);
    expect(getWikiMock().getStore()).andReturn(xwikiStoreMock).anyTimes();
  }
  
  @Test
  public void testClearFileName_empty() {
    String name = "";
    assertEquals(name, attService.clearFileName(name));
  }
  
  @Test
  public void testClearFileName_clean() {
    String name = "abc.jpg";
    assertEquals(name, attService.clearFileName(name));
  }
  
  @Test
  public void testClearFileName_minus() {
    String name = "abc-123.jpg";
    assertEquals(name, attService.clearFileName(name));
  }
  
  @Test
  public void testClearFileName_space() {
    String name = "abc 123.jpg";
    String target = "abc-123.jpg";
    assertEquals(target, attService.clearFileName(name));
  }
  
  @Test
  public void testClearFileName_underscore() {
    String name = "abc_123.jpg";
    assertEquals(name, attService.clearFileName(name));
  }
  
  @Test
  public void testDeleteAttachmentList_null() {
    assertEquals(0, attService.deleteAttachmentList(null));
  }
  
  @Test
  public void testDeleteAttachmentList_empty() {
    assertEquals(0, attService.deleteAttachmentList(
        new ArrayList<AttachmentReference>()));
  }
  
  @Test
  public void testDeleteAttachmentList_list() throws XWikiException {
    String name1 = "file1.jpg";
    String name2 = "file2.txt";
    String name3 = "file3.png";
    String names = name1 + ", " + name2 + ", " + name3;
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "Doc");
    ArrayList<AttachmentReference> attList = new ArrayList<AttachmentReference>();
    attList.add(new AttachmentReference(name1, docRef));
    attList.add(new AttachmentReference(name2, docRef));
    attList.add(new AttachmentReference(name3, docRef));
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getContext().getWiki().getDocument(eq(docRef), same(getContext()))
        ).andReturn(doc).once();
    String comment = "deleted attachments " + names;
    ((TestMessageTool)getContext().getMessageTool()).injectMessage("core.comment." +
              "deleteAttachmentComment", Arrays.asList(names), comment);
    getContext().getWiki().saveDocument(same(doc), same(getContext()));
    expectLastCall();
    XWikiAttachment att1 = new XWikiAttachment();
    att1.setFilename(name1);
    doc.getAttachmentList().add(att1);
    XWikiAttachment att2 = new XWikiAttachment();
    att2.setFilename(name2);
    doc.getAttachmentList().add(att2);
    XWikiAttachment att3 = new XWikiAttachment();
    att3.setFilename(name3);
    doc.getAttachmentList().add(att3);
    expect(getWikiMock().hasAttachmentRecycleBin(same(getContext()))).andReturn(false
        ).anyTimes();
    XWikiAttachmentStoreInterface store = createMockAndAddToDefault(
        XWikiAttachmentStoreInterface.class);
    expect(getWikiMock().getAttachmentStore()).andReturn(store).anyTimes();
    store.deleteXWikiAttachment(same(att1), same(getContext()), eq(true));
    expectLastCall();
    store.deleteXWikiAttachment(same(att2), same(getContext()), eq(true));
    expectLastCall();
    store.deleteXWikiAttachment(same(att3), same(getContext()), eq(true));
    expectLastCall();
    replayDefault();
    assertEquals(3, attService.deleteAttachmentList(attList));
    verifyDefault();
    assertEquals(getContext().getUser(), doc.getAuthor());
    assertEquals(comment, doc.getComment());
  }
  
  @Test
  public void testDeleteAttachmentList_list_with_nonexistant() throws XWikiException {
    String name1 = "file1.jpg";
    String name3 = "file3.png";
    String name5 = "file5.txt";
    String names = name3 + ", " + name5;
    String name2 = "file2.txt";
    String name4 = "file4.zip";
    String names2 = name2 + ", " + name4;
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "Doc");
    DocumentReference docRef2 = new DocumentReference(getContext().getDatabase(), "Space",
        "Doc2");
    ArrayList<AttachmentReference> attList = new ArrayList<AttachmentReference>();
    attList.add(new AttachmentReference(name1, docRef));
    attList.add(new AttachmentReference(name2, docRef2));
    attList.add(new AttachmentReference(name3, docRef));
    attList.add(new AttachmentReference(name4, docRef2));
    attList.add(new AttachmentReference(name5, docRef));
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getContext().getWiki().getDocument(eq(docRef), same(getContext()))
        ).andReturn(doc).once();    String comment = "deleted attachments " + names;
    ((TestMessageTool)getContext().getMessageTool()).injectMessage("core.comment." +
        "deleteAttachmentComment", Arrays.asList(names), comment);
    getContext().getWiki().saveDocument(same(doc), same(getContext()));
    expectLastCall();
    XWikiDocument doc2 = new XWikiDocument(docRef2);
    expect(getContext().getWiki().getDocument(eq(docRef2), same(getContext()))
        ).andReturn(doc2).once();
    String comment2 = "deleted attachments " + names2;
    ((TestMessageTool)getContext().getMessageTool()).injectMessage("core.comment." +
        "deleteAttachmentComment", Arrays.asList(names2), comment2);
    getContext().getWiki().saveDocument(same(doc2), same(getContext()));
    expectLastCall();
    XWikiAttachment att2 = new XWikiAttachment();
    att2.setFilename(name2);
    doc2.getAttachmentList().add(att2);
    XWikiAttachment att3 = new XWikiAttachment();
    att3.setFilename(name3);
    doc.getAttachmentList().add(att3);
    XWikiAttachment att4 = new XWikiAttachment();
    att4.setFilename(name4);
    doc2.getAttachmentList().add(att4);
    XWikiAttachment att5 = new XWikiAttachment();
    att5.setFilename(name5);
    doc.getAttachmentList().add(att5);
    expect(getWikiMock().hasAttachmentRecycleBin(same(getContext()))).andReturn(false
        ).anyTimes();
    XWikiAttachmentStoreInterface store = createMockAndAddToDefault(
        XWikiAttachmentStoreInterface.class);
    expect(getWikiMock().getAttachmentStore()).andReturn(store).anyTimes();
    store.deleteXWikiAttachment(same(att2), same(getContext()), eq(true));
    expectLastCall();
    store.deleteXWikiAttachment(same(att3), same(getContext()), eq(true));
    expectLastCall();
    store.deleteXWikiAttachment(same(att4), same(getContext()), eq(true));
    expectLastCall();
    store.deleteXWikiAttachment(same(att5), same(getContext()), eq(true));
    expectLastCall();
    replayDefault();
    assertEquals(4, attService.deleteAttachmentList(attList));
    verifyDefault();
    assertEquals(getContext().getUser(), doc.getAuthor());
    assertEquals(comment, doc.getComment());
    assertEquals(getContext().getUser(), doc2.getAuthor());
    assertEquals(comment2, doc2.getComment());
  }
  
  @Test
  public void testBuildAttachmentsToDeleteMap() {
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "Doc");
    DocumentReference docRef2 = new DocumentReference(getContext().getDatabase(), "Space",
        "Doc2");
    ArrayList<AttachmentReference> attList = new ArrayList<AttachmentReference>();
    attList.add(new AttachmentReference("file1.jpg", docRef));
    attList.add(new AttachmentReference("file2.txt", docRef2));
    attList.add(new AttachmentReference("file3.png", docRef));
    attList.add(new AttachmentReference("file4.zip", docRef2));
    attList.add(new AttachmentReference("file5.txt", docRef));
    Map<DocumentReference, List<String>> attMap = attService.buildAttachmentsToDeleteMap(
        attList);
    assertEquals(2, attMap.keySet().size());
    assertEquals(3, attMap.get(docRef).size());
    assertEquals(2, attMap.get(docRef2).size());
  }
  
  @Test
  public void testAddAttachment() throws Exception {
    XWikiDocument doc = createMockAndAddToDefault(XWikiDocument.class);
    XWikiAttachment att = new XWikiAttachment();
    String comment = "comment";
    String filename = "file.txt";
    String author = "XWiki.test";
    att.setFilename(filename);
    expect(doc.clone()).andReturn(doc).once();
    expect(doc.getAttachmentRevisionURL(eq(filename), (String)anyObject(), 
        same(getContext()))).andReturn("").once();
    expect(doc.getDocumentReference()).andReturn(new DocumentReference(getContext(
        ).getDatabase(), "Spc", "Doc")).once();
    IModelAccessFacade modelAccess = registerComponentMock(IModelAccessFacade.class);
    attService.modelAccess = modelAccess;
    expect(modelAccess.getAttachmentNameEqual(same(doc), eq(filename))).andReturn(att);
    modelAccess.saveDocument(same(doc), eq(comment));
    expectLastCall();
    doc.setAuthor(eq(author));
    expectLastCall();
    doc.setMetaDataDirty(eq(true));
    expectLastCall().atLeastOnce();
    doc.setContentDirty(eq(true));
    expectLastCall().atLeastOnce();
    replayDefault();
    XWikiAttachment retAtt = attService.addAttachment(doc, new byte[]{'x'}, filename, 
        author, comment);
    assertSame(att, retAtt);
    verifyDefault();
    assertEquals(filename, retAtt.getFilename());
  }
  
  @Test
  public void testAddAttachment_newFileNameIsPrefixOfExisting() throws Exception {
    String comment = "comment";
    String filename = "file.txt";
    String author = "XWiki.test";
    String spc = "Spc";
    String docName = "Doc";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), spc, 
        docName);
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setSyntax(new Syntax(SyntaxType.XWIKI, "1.0"));
    XWikiURLFactory URLFactory = createMockAndAddToDefault(XWikiURLFactory.class);
    URL url = null;
    expect(URLFactory.createAttachmentRevisionURL(eq(filename), eq(spc), eq(docName), 
        (String)anyObject(), (String)anyObject(), eq(getContext().getDatabase()), 
        same(getContext()))).andReturn(url);
    expect(URLFactory.getURL(eq(url), same(getContext()))).andReturn("");
    getContext().setURLFactory(URLFactory);
    List<XWikiAttachment> attList = new ArrayList<XWikiAttachment>();
    XWikiAttachment att = new XWikiAttachment();
    att.setFilename(filename + ".zip");
    attList.add(att);
    doc.setAttachmentList(attList);
    IModelAccessFacade modelAccess = registerComponentMock(IModelAccessFacade.class);
    attService.modelAccess = modelAccess;
    expect(modelAccess.getAttachmentNameEqual((XWikiDocument)anyObject(), eq(filename))
        ).andReturn(null);
    modelAccess.saveDocument((XWikiDocument)anyObject(), eq(comment));
    expectLastCall();
    replayDefault();
    XWikiAttachment retAtt = attService.addAttachment(doc, new byte[]{'x'}, filename, 
        author, comment);
    verifyDefault();
    assertEquals(2, retAtt.getDoc().getAttachmentList().size());
    assertEquals(filename, retAtt.getFilename());
  }

  @Test
  public void test_getAttachmentNameEqual() throws Exception {
    String filename = "image.jpg";
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment imageAtt = new XWikiAttachment(doc, filename);
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, imageAtt, lastAtt);
    doc.setAttachmentList(attList);
    replayDefault();
    XWikiAttachment att = attService.getAttachmentNameEqual(doc, filename);
    verifyDefault();
    assertNotNull("Expected image.jpg attachment - not null", att);
    assertEquals(filename, att.getFilename());
  }

  @Test
  public void test_getAttachmentNameEqual_not_exists() {
    String filename = "image.jpg";
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, lastAtt);
    doc.setAttachmentList(attList);
    replayDefault();
    try {
      attService.getAttachmentNameEqual(doc, filename);
      fail("AttachmentNotExistsException expected");
    } catch (AttachmentNotExistsException exp) {
      //expected
    }
    verifyDefault();
  }

  @Test
  public void test_getAttachmentNameEqual_attRef() throws Exception {
    String filename = "image.jpg";
    AttachmentReference attRef = new AttachmentReference(filename,
        doc.getDocumentReference());
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment imageAtt = new XWikiAttachment(doc, filename);
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, imageAtt, lastAtt);
    doc.setAttachmentList(attList);
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    XWikiAttachment att = attService.getAttachmentNameEqual(attRef);
    verifyDefault();
    assertNotNull("Expected image.jpg attachment - not null", att);
    assertEquals(filename, att.getFilename());
  }

  @Test
  public void test_getAttachmentNameEqual_attRef_not_exists() throws Exception {
    String filename = "image.jpg";
    AttachmentReference attRef = new AttachmentReference(filename,
        doc.getDocumentReference());
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, lastAtt);
    doc.setAttachmentList(attList);
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    try {
      attService.getAttachmentNameEqual(attRef);
      fail("AttachmentNotExistsException expected");
    } catch (AttachmentNotExistsException exp) {
      //expected
    }
    verifyDefault();
  }

  @Test
  public void test_getAttachmentNameEqual_attRef_doc_not_exists() throws Exception {
    String filename = "image.jpg";
    AttachmentReference attRef = new AttachmentReference(filename,
        doc.getDocumentReference());
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, lastAtt);
    doc.setAttachmentList(attList);
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(false).once();
    replayDefault();
    try {
      attService.getAttachmentNameEqual(attRef);
      fail("AttachmentNotExistsException expected");
    } catch (DocumentNotExistsException exp) {
      //expected
    }
    verifyDefault();
  }

  @Test
  public void test_existsAttachmentNameEqual() {
    String filename = "image.jpg";
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment imageAtt = new XWikiAttachment(doc, filename);
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, imageAtt, lastAtt);
    doc.setAttachmentList(attList);
    replayDefault();
    assertTrue(attService.existsAttachmentNameEqual(doc, filename));
    verifyDefault();
  }

  @Test
  public void test_existsAttachmentNameEqual_not_exists() {
    String filename = "image.jpg";
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, lastAtt);
    doc.setAttachmentList(attList);
    replayDefault();
    assertFalse(attService.existsAttachmentNameEqual(doc, filename));
    verifyDefault();
  }

  @Test
  public void test_existsAttachmentNameEqual_attRef() throws Exception {
    String filename = "image.jpg";
    AttachmentReference attRef = new AttachmentReference(filename,
        doc.getDocumentReference());
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment imageAtt = new XWikiAttachment(doc, filename);
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, imageAtt, lastAtt);
    doc.setAttachmentList(attList);
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    assertTrue(attService.existsAttachmentNameEqual(attRef));
    verifyDefault();
  }

  @Test
  public void test_existsAttachmentNameEqual_attRef_doc_not_exists() throws Exception {
    String filename = "image.jpg";
    AttachmentReference attRef = new AttachmentReference(filename,
        doc.getDocumentReference());
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, lastAtt);
    doc.setAttachmentList(attList);
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(false).once();
    replayDefault();
    assertFalse(attService.existsAttachmentNameEqual(attRef));
    verifyDefault();
  }

  @Test
  public void test_existsAttachmentNameEqual_attRef_not_exists() throws Exception {
    String filename = "image.jpg";
    AttachmentReference attRef = new AttachmentReference(filename,
        doc.getDocumentReference());
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, lastAtt);
    doc.setAttachmentList(attList);
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(doc).once();
    replayDefault();
    assertFalse(attService.existsAttachmentNameEqual(attRef));
    verifyDefault();
  }

  @Test
  public void test_existsAttachmentNameEqual_attRef_docLoadExp() throws Exception {
    String filename = "image.jpg";
    AttachmentReference attRef = new AttachmentReference(filename,
        doc.getDocumentReference());
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, lastAtt);
    doc.setAttachmentList(attList);
    expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(getContext()))
        ).andReturn(true).once();
    expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(getContext()))
        ).andThrow(new XWikiException()).once();
    replayDefault();
    try {
      attService.existsAttachmentNameEqual(attRef);
      fail("DocumentLoadException expected to be passed on.");
    } catch (DocumentLoadException exp) {
      //expected
    }
    verifyDefault();
  }
  
  @Test
  public void test_getAttachmentFirstNameMatch() throws AttachmentNotExistsException {
    String filename = "image.jpg";
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename);
    XWikiAttachment imageAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, imageAtt, lastAtt);
    doc.setAttachmentList(attList);
    IAttFileNameMatcherRole mockMatcher = createMockAndAddToDefault(
        IAttFileNameMatcherRole.class);
    expect(mockMatcher.getFileNamePattern()).andReturn(filename).anyTimes();
    expect(mockMatcher.accept(firstAtt)).andReturn(true).anyTimes();
    expect(mockMatcher.accept(imageAtt)).andReturn(true).anyTimes();
    expect(mockMatcher.accept(lastAtt)).andReturn(false).anyTimes();
    replayDefault();
    XWikiAttachment resFirstImage;
    resFirstImage = attService.getAttachmentFirstNameMatch(doc, mockMatcher);
    verifyDefault();
    assertNotNull("Expected emptyList - not null", resFirstImage);
    assertEquals(filename, resFirstImage.getFilename());
  }
  
  @Test
  public void test_getAttachmentFirstNameMatch_reverseOrder(
      ) throws AttachmentNotExistsException {
    String filename = "image.jpg";
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment imageAtt = new XWikiAttachment(doc, filename);
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, imageAtt, lastAtt);
    doc.setAttachmentList(attList);
    IAttFileNameMatcherRole mockMatcher = createMockAndAddToDefault(
        IAttFileNameMatcherRole.class);
    expect(mockMatcher.getFileNamePattern()).andReturn(filename).anyTimes();
    expect(mockMatcher.accept(firstAtt)).andReturn(true).anyTimes();
    expect(mockMatcher.accept(imageAtt)).andReturn(true).anyTimes();
    expect(mockMatcher.accept(lastAtt)).andReturn(false).anyTimes();
    replayDefault();
    XWikiAttachment resFirstImage;
    resFirstImage = attService.getAttachmentFirstNameMatch(doc, mockMatcher);
    verifyDefault();
    assertNotNull("Expected emptyList - not null", resFirstImage);
    assertEquals(filename, resFirstImage.getFilename());
  }
  
  @Test
  public void test_getAttachmentFirstNameMatch_empty() {
    String filename = "image.jpg";
    XWikiAttachment firstAtt = new XWikiAttachment(doc, "bli.zip");
    XWikiAttachment imageAtt = new XWikiAttachment(doc, "other.file");
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, imageAtt, lastAtt);
    doc.setAttachmentList(attList);
    IAttFileNameMatcherRole mockMatcher = createMockAndAddToDefault(
        IAttFileNameMatcherRole.class);
    expect(mockMatcher.getFileNamePattern()).andReturn(filename).anyTimes();
    expect(mockMatcher.accept(firstAtt)).andReturn(false).anyTimes();
    expect(mockMatcher.accept(imageAtt)).andReturn(false).anyTimes();
    expect(mockMatcher.accept(lastAtt)).andReturn(false).anyTimes();
    replayDefault();
    try {
      attService.getAttachmentFirstNameMatch(doc, mockMatcher);
      fail("AttachmentNotExistsException expected");
    } catch (AttachmentNotExistsException e) {
      //expected outcome
    }
    verifyDefault();
  }

  @Test
  public void test_getAttachmentsNameMatch() throws Exception {
    String filename = "image.jpg";
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment imageAtt = new XWikiAttachment(doc, filename);
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, imageAtt, lastAtt);
    doc.setAttachmentList(attList);
    IAttachmentMatcher mockMatcher = createMockAndAddToDefault(
        IAttachmentMatcher.class);
    expect(mockMatcher.accept(firstAtt)).andReturn(true).anyTimes();
    expect(mockMatcher.accept(imageAtt)).andReturn(true).anyTimes();
    expect(mockMatcher.accept(lastAtt)).andReturn(false).anyTimes();
    replayDefault();
    List<XWikiAttachment> resAttList = attService.getAttachmentsNameMatch(doc,
        mockMatcher);
    verifyDefault();
    assertNotNull("Expected emptyList - not null", resAttList);
    assertEquals(2, resAttList.size());
    XWikiAttachment resFirstImage = resAttList.get(0);
    assertEquals(firstAtt.getFilename(), resFirstImage.getFilename());
    XWikiAttachment resSecondImage = resAttList.get(1);
    assertEquals(imageAtt.getFilename(), resSecondImage.getFilename());
  }

  @Test
  public void test_getAttachmentsNameMatch_noMatch_emptyList() {
    String filename = "image.jpg";
    XWikiAttachment firstAtt = new XWikiAttachment(doc, filename + ".zip");
    XWikiAttachment imageAtt = new XWikiAttachment(doc, filename);
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(firstAtt, lastAtt);
    doc.setAttachmentList(attList);
    IAttachmentMatcher mockMatcher = createMockAndAddToDefault(
        IAttachmentMatcher.class);
    expect(mockMatcher.accept(firstAtt)).andReturn(false).anyTimes();
    expect(mockMatcher.accept(imageAtt)).andReturn(false).anyTimes();
    expect(mockMatcher.accept(lastAtt)).andReturn(false).anyTimes();
    replayDefault();
    List<XWikiAttachment> resAttList = attService.getAttachmentsNameMatch(doc,
        mockMatcher);
    assertNotNull("Expected emptyList - not null", resAttList);
    assertTrue(resAttList.isEmpty());
    verifyDefault();
  }

  @Test
  public void test_getAttachmentsNameMatch_null_matcher() {
    XWikiAttachment lastAtt = new XWikiAttachment(doc, "bli.gaga");
    List<XWikiAttachment> attList = Arrays.asList(lastAtt);
    doc.setAttachmentList(attList);
    replayDefault();
    List<XWikiAttachment> resAttList = attService.getAttachmentsNameMatch(doc, null);
    assertNotNull("Expected emptyList - not null", resAttList);
    assertTrue(resAttList.isEmpty());
    verifyDefault();
  }

  @Test
  public void test_getApiAttachment_hasAccess() throws Exception {
    XWikiRightService mockRightSrv = createMockAndAddToDefault(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(mockRightSrv).anyTimes();
    XWikiAttachment xwikiAtt = new XWikiAttachment(doc, "bli.gaga");
    expect(mockRightSrv.hasAccessLevel(eq("view"), eq(getContext().getUser()),
        eq("db:space.doc"), same(getContext()))).andReturn(true).atLeastOnce();
    replayDefault();
    Attachment attachment = attService.getApiAttachment(xwikiAtt);
    assertNotNull("Expected Attachment api object - not null", attachment);
    verifyDefault();
  }

  @Test
  public void test_getApiAttachment_noAccess() throws Exception {
    XWikiRightService mockRightSrv = createMockAndAddToDefault(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(mockRightSrv).anyTimes();
    XWikiAttachment xwikiAtt = new XWikiAttachment(doc, "bli.gaga");
    expect(mockRightSrv.hasAccessLevel(eq("view"), eq(getContext().getUser()),
        eq("db:space.doc"), same(getContext()))).andReturn(false).atLeastOnce();
    replayDefault();
    try {
      attService.getApiAttachment(xwikiAtt);
      fail("NoAccessRightsException expected");
    } catch (NoAccessRightsException exp) {
      //expected
    }
    verifyDefault();
  }

}
