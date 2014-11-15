package com.celements.pagetype.xobject;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.cmd.PageTypeCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeProviderTest extends AbstractBridgedComponentTestCase {

  private XObjectPageTypeProvider xObjPTprovider;
  private XWikiContext context;
  private XWiki xwiki;
  private PageTypeCommand pageTypeCmdMock;
  private PageTypeCommand backupPageTypeCmd;

  @Before
  public void setUp_XObjectPageTypeProviderTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    xObjPTprovider = (XObjectPageTypeProvider) Utils.getComponent(
        IPageTypeProviderRole.class, XObjectPageTypeProvider.X_OBJECT_PAGE_TYPE_PROVIDER);
    pageTypeCmdMock = createMockAndAddToDefault(PageTypeCommand.class);
    backupPageTypeCmd = xObjPTprovider.pageTypeCmd;
    xObjPTprovider.pageTypeCmd = pageTypeCmdMock;
  }

  @After
  public void tearDown_XObjectPageTypeProviderTest() {
    xObjPTprovider.pageTypeCmd = backupPageTypeCmd;
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
    replayDefault();
    IPageTypeConfig ptObj = xObjPTprovider.getPageTypeByReference(testPTref);
    assertNotNull(ptObj);
    assertEquals("TestPageTypeRef", ptObj.getName());
    verifyDefault();
  }

  @Test
  public void testGetPageTypes() throws Exception {
    Set<String> allPageTypeNames = new HashSet<String>(Arrays.asList(
        "PageTypes.RichText"));
    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(false
        ).anyTimes();
    DocumentReference centralRichTextPTdocRef = new DocumentReference("celements2web",
        "PageTypes", "RichText");
    XWikiDocument centralRichTextPTdoc = new XWikiDocument(centralRichTextPTdocRef);
    expect(xwiki.getDocument(eq("celements2web:PageTypes.RichText"), same(context))
        ).andReturn(centralRichTextPTdoc).anyTimes();
    List<String> pageTypeString = Arrays.asList("PageTypes.RichText");
    expect(xwiki.<String>search(isA(String.class), same(context))).andReturn(
        pageTypeString).times(2);
    replayDefault();
    List<PageTypeReference> allPageTypes = xObjPTprovider.getPageTypes();
    assertFalse("expecting RichtText page type reference.", allPageTypes.isEmpty());
    PageTypeReference richTextPTref = allPageTypes.get(0);
    assertEquals("RichText", richTextPTref.getConfigName());
    assertEquals(Arrays.asList(""), richTextPTref.getCategories());
    verifyDefault();
  }

}
