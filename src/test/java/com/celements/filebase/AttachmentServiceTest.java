package com.celements.filebase;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.same;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.XWikiMessageTool;

public class AttachmentServiceTest extends AbstractBridgedComponentTestCase {

  private AttachmentService attService;
  
  @Before
  public void setUp_AttachmentServiceTest() throws ComponentLookupException, Exception {
    attService = (AttachmentService) getComponentManager().lookup(
        IAttachmentServiceRole.class);
    getContext().setWiki(createMock(XWiki.class));
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
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(getContext().getWiki().getDocument(eq(docRef), same(getContext()))
        ).andReturn(doc).once();
    doc.setAuthor(eq(getContext().getUser()));
    expectLastCall();
    String comment = "deleted attachments " + names;
    XWikiMessageTool msgTool = createMock(XWikiMessageTool.class);
    attService._injectedMsgTool = msgTool;
    expect(msgTool.get((String)anyObject(), eq(Arrays.asList(names)))
        ).andReturn(comment).once();
    doc.setComment(eq(comment));
    expectLastCall();
    doc.setOriginalDocument(same(doc));
    expectLastCall();
    getContext().getWiki().saveDocument(same(doc), same(getContext()));
    expectLastCall();
    XWikiAttachment att1 = new XWikiAttachment();
    XWikiAttachment att2 = new XWikiAttachment();
    XWikiAttachment att3 = new XWikiAttachment();
    expect(doc.getAttachment(name1)).andReturn(att1).once();
    doc.deleteAttachment(same(att1), same(getContext()));
    expectLastCall();
    expect(doc.getAttachment(name2)).andReturn(att2).once();
    doc.deleteAttachment(same(att2), same(getContext()));
    expectLastCall();
    expect(doc.getAttachment(name3)).andReturn(att3).once();
    doc.deleteAttachment(same(att3), same(getContext()));
    expectLastCall();
    replayDefault(getContext().getWiki(), doc, msgTool);
    assertEquals(3, attService.deleteAttachmentList(attList));
    verifyDefault(getContext().getWiki(), doc, msgTool);
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
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(getContext().getWiki().getDocument(eq(docRef), same(getContext()))
        ).andReturn(doc).once();
    doc.setAuthor(eq(getContext().getUser()));
    expectLastCall();
    String comment = "deleted attachments " + names;
    XWikiMessageTool msgTool = createMock(XWikiMessageTool.class);
    attService._injectedMsgTool = msgTool;
    expect(msgTool.get((String)anyObject(), eq(Arrays.asList(names)))
        ).andReturn(comment).once();
    doc.setComment(eq(comment));
    expectLastCall();
    doc.setOriginalDocument(same(doc));
    expectLastCall();
    getContext().getWiki().saveDocument(same(doc), same(getContext()));
    expectLastCall();
    XWikiDocument doc2 = createMock(XWikiDocument.class);
    expect(getContext().getWiki().getDocument(eq(docRef2), same(getContext()))
        ).andReturn(doc2).once();
    doc2.setAuthor(eq(getContext().getUser()));
    expectLastCall();
    String comment2 = "deleted attachments " + names2;
    expect(msgTool.get((String)anyObject(), eq(Arrays.asList(names2)))
        ).andReturn(comment2).once();
    doc2.setComment(eq(comment2));
    expectLastCall();
    doc2.setOriginalDocument(same(doc2));
    expectLastCall();
    getContext().getWiki().saveDocument(same(doc2), same(getContext()));
    expectLastCall();
    XWikiAttachment att2 = new XWikiAttachment();
    XWikiAttachment att3 = new XWikiAttachment();
    XWikiAttachment att4 = new XWikiAttachment();
    XWikiAttachment att5 = new XWikiAttachment();
    expect(doc.getAttachment(name1)).andReturn(null).once();
    expect(doc2.getAttachment(name2)).andReturn(att2).once();
    doc2.deleteAttachment(same(att2), same(getContext()));
    expectLastCall();
    expect(doc.getAttachment(name3)).andReturn(att3).once();
    doc.deleteAttachment(same(att3), same(getContext()));
    expectLastCall();
    expect(doc2.getAttachment(name4)).andReturn(att4).once();
    doc2.deleteAttachment(same(att4), same(getContext()));
    expectLastCall();
    expect(doc.getAttachment(name5)).andReturn(att5).once();
    doc.deleteAttachment(same(att5), same(getContext()));
    expectLastCall();
    replayDefault(getContext().getWiki(), doc, doc2, msgTool);
    assertEquals(4, attService.deleteAttachmentList(attList));
    verifyDefault(getContext().getWiki(), doc, doc2, msgTool);
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

}
