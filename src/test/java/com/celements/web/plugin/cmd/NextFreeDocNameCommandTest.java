package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.nextfreedoc.NextFreeDocService;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class NextFreeDocNameCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private NextFreeDocNameCommand nextFreeDocNameCmd;
  
  private SpaceReference spaceRef;
  private XWikiDocument docMock;
  private int num = 5;

  @Before
  public void setUp_NextFreeDocNameCommandTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    nextFreeDocNameCmd = new NextFreeDocNameCommand();
    spaceRef = new SpaceReference("mySpace", new WikiReference(context.getDatabase()));
    docMock = createMockAndAddToDefault(XWikiDocument.class);
  }

  @Test
  public void testGetNextTitledPageFullName() throws Exception {
    String title = "asdf";
    DocumentReference docRef = new DocumentReference(title + num, spaceRef);
    ((NextFreeDocService) Utils.getComponent(INextFreeDocRole.class)).injectNum(spaceRef, 
        title, num);
    
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(docMock).once();
    expect(docMock.getLock(same(context))).andReturn(null).once();
    replayDefault();
    assertEquals(serialize(docRef), nextFreeDocNameCmd.getNextTitledPageFullName(
        spaceRef.getName(), title, context));
    verifyDefault();
  }

  @Test
  public void testGetNextTitledPageFullName_emptySpace() throws Exception {
    replayDefault();
    try {
      nextFreeDocNameCmd.getNextTitledPageFullName("", "product", context);
      fail();
    } catch (IllegalArgumentException exp) {
      //expected
    }
    verifyDefault();
  }

  @Test
  public void testGetNextTitledPageDocRef() throws Exception {
    String title = "asdf";
    DocumentReference docRef = new DocumentReference(title + num, spaceRef);
    ((NextFreeDocService) Utils.getComponent(INextFreeDocRole.class)).injectNum(spaceRef, 
        title, num);
    
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(docMock).once();
    expect(docMock.getLock(same(context))).andReturn(null).once();
    replayDefault();
    assertEquals(docRef, nextFreeDocNameCmd.getNextTitledPageDocRef(spaceRef.getName(), 
        title, context));
    verifyDefault();
  }

  @Test
  public void testGetNextTitledPageDocRef_emptySpace() throws Exception {
    replayDefault();
    try {
      nextFreeDocNameCmd.getNextTitledPageDocRef("", "product", context);
      fail();
    } catch (IllegalArgumentException exp) {
      //expected
    }
    verifyDefault();
  }

  @Test
  public void testGetNextUntitledPageFullName() throws Exception {
    String title = "untitled";
    DocumentReference docRef = new DocumentReference(title + num, spaceRef);
    ((NextFreeDocService) Utils.getComponent(INextFreeDocRole.class)).injectNum(spaceRef, 
        title, num);
    
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(docMock).once();
    expect(docMock.getLock(same(context))).andReturn(null).once();
    replayDefault();
    assertEquals(serialize(docRef), nextFreeDocNameCmd.getNextUntitledPageFullName(
        spaceRef.getName(), context));
    verifyDefault();
  }

  @Test
  public void testGetNextUntitledPageFullName_emptySpace() throws Exception {
    replayDefault();
    try {
      nextFreeDocNameCmd.getNextUntitledPageFullName("", context);
      fail();
    } catch (IllegalArgumentException exp) {
      //expected
    }
    verifyDefault();
  }

  @Test
  public void testGetNextUntitledPageName() throws Exception {
    String title = "untitled";
    DocumentReference docRef = new DocumentReference(title + num, spaceRef);
    ((NextFreeDocService) Utils.getComponent(INextFreeDocRole.class)).injectNum(spaceRef, 
        title, num);
    
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(docMock).once();
    expect(docMock.getLock(same(context))).andReturn(null).once();
    replayDefault();
    assertEquals(docRef.getName(), nextFreeDocNameCmd.getNextUntitledPageName(
        spaceRef.getName(), context));
    verifyDefault();
  }

  @Test
  public void testGetNextUntitledPageName_emptySpace() throws Exception {
    replayDefault();
    try {
      nextFreeDocNameCmd.getNextUntitledPageName("", context);
      fail();
    } catch (IllegalArgumentException exp) {
      //expected
    }
    verifyDefault();
  }
  
  private String serialize(DocumentReference docRef) {
    return Utils.getComponent(IWebUtilsService.class).getRefLocalSerializer().serialize(
        docRef);
  }

}
