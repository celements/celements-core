package com.celements.pagetype.xobject;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.PageType;
import com.celements.pagetype.cmd.PageTypeCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public class XObjectPageTypeConfigTest extends AbstractBridgedComponentTestCase {

  private XObjectPageTypeConfig xObjPTconfig;
  private PageType pageTypeMock;
  private XWikiContext context;
  private XWiki xwiki;
  private PageTypeCommand pageTypeCmdMock;

  @Before
  public void setUp_XObjectPageTypeConfigTest() throws Exception {
    context = getContext();
    xObjPTconfig = new XObjectPageTypeConfig("PageTypes.TestPageType");
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    pageTypeMock = createMock(PageType.class);
    xObjPTconfig.pageType = pageTypeMock;
    pageTypeCmdMock = createMock(PageTypeCommand.class);
    xObjPTconfig.pageTypeCmd = pageTypeCmdMock;
    expect(pageTypeMock.getConfigName(same(context))).andReturn("TestPageType"
        ).anyTimes();
  }

  @Test
  public void testGetCategories_noEmptyCategories() {
    expect(pageTypeMock.getCategories(same(context))).andReturn(
        Collections.<String>emptyList());
    replayAll();
    assertEquals(Arrays.asList(""), xObjPTconfig.getCategories());
    verifyAll();
  }

  @Test
  public void testGetCategories() {
    expect(pageTypeMock.getCategories(same(context))).andReturn(
        Arrays.asList("cellType"));
    replayAll();
    assertEquals(Arrays.asList("cellType"), xObjPTconfig.getCategories());
    verifyAll();
  }

  @Test
  public void testDisplayInFrameLayout_yes() {
    expect(pageTypeMock.showFrame(same(context))).andReturn(true);
    replayAll();
    assertTrue(xObjPTconfig.displayInFrameLayout());
    verifyAll();
  }

  @Test
  public void testDisplayInFrameLayout_no() {
    expect(pageTypeMock.showFrame(same(context))).andReturn(false);
    replayAll();
    assertFalse(xObjPTconfig.displayInFrameLayout());
    verifyAll();
  }

  @Test
  public void testGetName() {
    replayAll();
    assertEquals("TestPageType", xObjPTconfig.getName());
    verifyAll();
  }

  @Test
  public void testGetPrettyName() {
    String expectedPrettyName = "Test Page Type Pretty Name";
    expect(pageTypeMock.getPrettyName(same(context))).andReturn(expectedPrettyName);
    replayAll();
    assertEquals(expectedPrettyName, xObjPTconfig.getPrettyName());
    verifyAll();
  }

  @Test
  public void testHasPageTitle_yes() {
    expect(pageTypeMock.hasPageTitle(same(context))).andReturn(true);
    replayAll();
    assertTrue(xObjPTconfig.hasPageTitle());
    verifyAll();
  }

  @Test
  public void testHasPageTitle_no() {
    expect(pageTypeMock.hasPageTitle(same(context))).andReturn(false);
    replayAll();
    assertFalse(xObjPTconfig.hasPageTitle());
    verifyAll();
  }

  @Test
  public void testGetRenderTemplateForRenderMode_view() throws Exception {
    String expectedRenderTemplate = "Templates.TestPageTypeView";
    expect(pageTypeMock.getRenderTemplate(eq("view"), same(context))).andReturn(
        expectedRenderTemplate);
    replayAll();
    assertEquals(expectedRenderTemplate, xObjPTconfig.getRenderTemplateForRenderMode(
        "view"));
    verifyAll();
  }

  @Test
  public void testGetRenderTemplateForRenderMode_edit() throws Exception {
    String expectedRenderTemplate = "Templates.TestPageTypeEdit";
    expect(pageTypeMock.getRenderTemplate(eq("edit"), same(context))).andReturn(
        expectedRenderTemplate);
    replayAll();
    assertEquals(expectedRenderTemplate, xObjPTconfig.getRenderTemplateForRenderMode(
        "edit"));
    verifyAll();
  }

  @Test
  public void testIsVisible_no() throws Exception {
    DocumentReference pageTypeConfigDocRef = new DocumentReference(context.getDatabase(),
        "PageTypes", "TestPageType");
    XWikiDocument pageTypeConfigDoc = new XWikiDocument(pageTypeConfigDocRef);
    expect(pageTypeMock.getTemplateDocument(same(context))).andReturn(pageTypeConfigDoc);
    expect(pageTypeCmdMock.isVisible(same(pageTypeConfigDoc), same(context))).andReturn(
        false);
    replayAll();
    assertFalse(xObjPTconfig.isVisible());
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki, pageTypeMock, pageTypeCmdMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, pageTypeMock, pageTypeCmdMock);
    verify(mocks);
  }
}
