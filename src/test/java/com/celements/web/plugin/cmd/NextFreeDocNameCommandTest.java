package com.celements.web.plugin.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.XWikiDocumentCreator;
import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.nextfreedoc.NextFreeDocService;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.doc.XWikiLock;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

@Deprecated
public class NextFreeDocNameCommandTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private NextFreeDocNameCommand nextFreeDocNameCmd;

  private SpaceReference spaceRef;
  private int num = 5;
  private XWikiStoreInterface storeMock;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(XWikiDocumentCreator.class);
    context = getContext();
    xwiki = getWikiMock();
    nextFreeDocNameCmd = new NextFreeDocNameCommand();
    spaceRef = new SpaceReference("mySpace", new WikiReference(context.getDatabase()));
    storeMock = createMockAndAddToDefault(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(storeMock).anyTimes();
  }

  @Test
  public void testGetNextTitledPageFullName() throws Exception {
    String title = "asdf";
    DocumentReference docRef = new DocumentReference(title + num, spaceRef);
    ((NextFreeDocService) Utils.getComponent(INextFreeDocRole.class)).injectNum(spaceRef, title,
        num);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    XWikiDocument theDoc = new XWikiDocument(docRef);
    expect(getMock(XWikiDocumentCreator.class).create(eq(docRef))).andReturn(theDoc);
    expect(storeMock.loadLock(eq(theDoc.getId()), same(context), eq(true))).andReturn(null).once();
    storeMock.saveLock(anyObject(XWikiLock.class), same(context), eq(true));
    expectLastCall().once();

    replayDefault();
    assertEquals(serialize(docRef), nextFreeDocNameCmd.getNextTitledPageFullName(spaceRef.getName(),
        title, context));
    verifyDefault();
  }

  @Test
  public void testGetNextTitledPageFullName_emptySpace() throws Exception {
    replayDefault();
    try {
      nextFreeDocNameCmd.getNextTitledPageFullName("", "product", context);
      fail();
    } catch (IllegalArgumentException exp) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetNextTitledPageDocRef() throws Exception {
    String title = "asdf";
    DocumentReference docRef = new DocumentReference(title + num, spaceRef);
    ((NextFreeDocService) Utils.getComponent(INextFreeDocRole.class)).injectNum(spaceRef, title,
        num);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    XWikiDocument theDoc = new XWikiDocument(docRef);
    expect(getMock(XWikiDocumentCreator.class).create(eq(docRef))).andReturn(theDoc);
    expect(storeMock.loadLock(eq(theDoc.getId()), same(context), eq(true))).andReturn(null).once();
    storeMock.saveLock(anyObject(XWikiLock.class), same(context), eq(true));
    expectLastCall().once();

    replayDefault();
    assertEquals(docRef, nextFreeDocNameCmd.getNextTitledPageDocRef(spaceRef.getName(), title,
        context));
    verifyDefault();
  }

  @Test
  public void testGetNextTitledPageDocRef_emptySpace() throws Exception {
    replayDefault();
    try {
      nextFreeDocNameCmd.getNextTitledPageDocRef("", "product", context);
      fail();
    } catch (IllegalArgumentException exp) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetNextUntitledPageFullName() throws Exception {
    String title = "untitled";
    DocumentReference docRef = new DocumentReference(title + num, spaceRef);
    ((NextFreeDocService) Utils.getComponent(INextFreeDocRole.class)).injectNum(spaceRef, title,
        num);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    XWikiDocument theDoc = new XWikiDocument(docRef);
    expect(getMock(XWikiDocumentCreator.class).create(eq(docRef))).andReturn(theDoc);
    expect(storeMock.loadLock(eq(theDoc.getId()), same(context), eq(true))).andReturn(null).once();
    storeMock.saveLock(anyObject(XWikiLock.class), same(context), eq(true));
    expectLastCall().once();

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
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetNextUntitledPageName() throws Exception {
    String title = "untitled";
    DocumentReference docRef = new DocumentReference(title + num, spaceRef);
    ((NextFreeDocService) Utils.getComponent(INextFreeDocRole.class)).injectNum(spaceRef, title,
        num);
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    XWikiDocument theDoc = new XWikiDocument(docRef);
    expect(getMock(XWikiDocumentCreator.class).create(eq(docRef))).andReturn(theDoc);
    expect(storeMock.loadLock(eq(theDoc.getId()), same(context), eq(true))).andReturn(null).once();
    storeMock.saveLock(anyObject(XWikiLock.class), same(context), eq(true));
    expectLastCall().once();

    replayDefault();
    assertEquals(docRef.getName(), nextFreeDocNameCmd.getNextUntitledPageName(spaceRef.getName(),
        context));
    verifyDefault();
  }

  @Test
  public void testGetNextUntitledPageName_emptySpace() throws Exception {
    replayDefault();
    try {
      nextFreeDocNameCmd.getNextUntitledPageName("", context);
      fail();
    } catch (IllegalArgumentException exp) {
      // expected
    }
    verifyDefault();
  }

  private String serialize(DocumentReference docRef) {
    return Utils.getComponent(IWebUtilsService.class).getRefLocalSerializer().serialize(docRef);
  }

}
