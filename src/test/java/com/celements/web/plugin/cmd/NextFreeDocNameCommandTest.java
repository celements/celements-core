package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public class NextFreeDocNameCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private NextFreeDocNameCommand nextFreeDocNameCmd;

  @Before
  public void setUp_NextFreeDocNameCommandTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    nextFreeDocNameCmd = new NextFreeDocNameCommand();
  }

  @Test
  public void testGetNextUntitledPageFullName() throws Exception {
    DocumentReference untitled1DocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "untitled1");
    expect(xwiki.exists(eq(untitled1DocRef), same(context))).andReturn(true).once();
    DocumentReference untitled2DocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "untitled2");
    expect(xwiki.exists(eq(untitled2DocRef), same(context))).andReturn(false).once();
    XWikiDocument untitled2Doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(untitled2DocRef), same(context))).andReturn(untitled2Doc
        ).once();
    expect(untitled2Doc.getLock(same(context))).andReturn(null);
    replayDefault();
    assertEquals("mySpace.untitled2", nextFreeDocNameCmd.getNextUntitledPageFullName(
        "mySpace", context));
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
  public void testGetNextTitledPageFullName() throws Exception {
    DocumentReference product1DocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "product1");
    expect(xwiki.exists(eq(product1DocRef), same(context))).andReturn(true).once();
    DocumentReference product2DocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "product2");
    expect(xwiki.exists(eq(product2DocRef), same(context))).andReturn(false).once();
    XWikiDocument product2Doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(product2DocRef), same(context))).andReturn(product2Doc
        ).once();
    expect(product2Doc.getLock(same(context))).andReturn(null);
    replayDefault();
    assertEquals("mySpace.product2", nextFreeDocNameCmd.getNextTitledPageFullName(
        "mySpace", "product", context));
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
    DocumentReference product1DocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "product1");
    expect(xwiki.exists(eq(product1DocRef), same(context))).andReturn(true).once();
    DocumentReference product2DocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "product2");
    expect(xwiki.exists(eq(product2DocRef), same(context))).andReturn(false).once();
    XWikiDocument product2Doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(product2DocRef), same(context))).andReturn(product2Doc
        ).once();
    expect(product2Doc.getLock(same(context))).andReturn(null);
    replayDefault();
    assertEquals(product2DocRef, nextFreeDocNameCmd.getNextTitledPageDocRef("mySpace",
        "product", context));
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
  public void testGetNextUntitledPageName() throws Exception {
    DocumentReference untitled1DocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "untitled1");
    expect(xwiki.exists(eq(untitled1DocRef), same(context))).andReturn(true).once();
    DocumentReference untitled2DocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "untitled2");
    expect(xwiki.exists(eq(untitled2DocRef), same(context))).andReturn(false).once();
    XWikiDocument untitled2Doc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(untitled2DocRef), same(context))).andReturn(untitled2Doc
        ).once();
    expect(untitled2Doc.getLock(same(context))).andReturn(null);
    replayDefault();
    assertEquals("untitled2", nextFreeDocNameCmd.getNextUntitledPageName("mySpace",
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
      //expected
    }
    verifyDefault();
  }

}
