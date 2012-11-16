package com.celements.pagetype.xobject;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.cmd.GetPageTypesCommand;
import com.celements.pagetype.cmd.PageTypeCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeProviderTest extends AbstractBridgedComponentTestCase {

  private XObjectPageTypeProvider xObjPTprovider;
  private XWikiContext context;
  private XWiki xwiki;
  private GetPageTypesCommand getPageTypeCmdMock;
  private PageTypeCommand pageTypeCmdMock;

  @Before
  public void setUp_XObjectPageTypeProviderTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    xObjPTprovider = new XObjectPageTypeProvider();
    getPageTypeCmdMock = createMock(GetPageTypesCommand.class);
    xObjPTprovider.getPageTypeCmd = getPageTypeCmdMock;
    pageTypeCmdMock = createMock(PageTypeCommand.class);
    xObjPTprovider.pageTypeCmd = pageTypeCmdMock;
    xObjPTprovider.execution = Utils.getComponent(Execution.class);
  }

  @Test
  public void testComponentRegistration() {
    assertNotNull(Utils.getComponent(IPageTypeProviderRole.class,
        "com.celements.XObjectPageTypeProvider"));
  }

  @Test
  public void testGetPageTypeByReference() {
    PageTypeReference testPTref = new PageTypeReference("TestPageTypeRef",
        "xObjectProvider", Arrays.asList(""));
    expect(pageTypeCmdMock.completePageTypeDocName(eq("TestPageTypeRef"))).andReturn(
        "PageTypes.TestPageTypeRef");
    replayAll();
    IPageTypeConfig ptObj = xObjPTprovider.getPageTypeByReference(testPTref);
    assertNotNull(ptObj);
    assertEquals("TestPageTypeRef", ptObj.getName());
    verifyAll();
  }

  @Test
  public void testGetPageTypes() throws Exception {
    Set<String> allPageTypeNames = new HashSet<String>(Arrays.asList(
        "PageTypes.RichText"));
    expect(getPageTypeCmdMock.getAllXObjectPageTypes(same(context))).andReturn(
        allPageTypeNames);
    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(false
        ).anyTimes();
    DocumentReference centralRichTextPTdocRef = new DocumentReference("celements2web",
        "PageTypes", "RichText");
    XWikiDocument centralRichTextPTdoc = new XWikiDocument(centralRichTextPTdocRef);
    expect(xwiki.getDocument(eq("celements2web:PageTypes.RichText"), same(context))
        ).andReturn(centralRichTextPTdoc).anyTimes();
    replayAll();
    List<PageTypeReference> allPageTypes = xObjPTprovider.getPageTypes();
    assertFalse("expecting RichtText page type reference.", allPageTypes.isEmpty());
    PageTypeReference richTextPTref = allPageTypes.get(0);
    assertEquals("RichText", richTextPTref.getConfigName());
    assertEquals(Arrays.asList(""), richTextPTref.getCategories());
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki, pageTypeCmdMock, getPageTypeCmdMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, pageTypeCmdMock, getPageTypeCmdMock);
    verify(mocks);
  }

}
