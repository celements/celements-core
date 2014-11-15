package com.celements.pagetype.xobject;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.cmd.GetPageTypesCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeCacheTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private GetPageTypesCommand getPageTypeCmdMock;
  private XObjectPageTypeCache xObjPageTypeCache;
  private GetPageTypesCommand backupGetPageTypeCmd;

  @Before
  public void setUp_XObjectPageTypeCacheTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    xObjPageTypeCache = (XObjectPageTypeCache) Utils.getComponent(
        IXObjectPageTypeCacheRole.class);
    getPageTypeCmdMock = createMockAndAddToDefault(GetPageTypesCommand.class);
    backupGetPageTypeCmd = xObjPageTypeCache.getPageTypeCmd;
    xObjPageTypeCache.getPageTypeCmd = getPageTypeCmdMock;
  }

  @After
  public void tearDown_XObjectPageTypeCacheTest() {
    xObjPageTypeCache.getPageTypeCmd = backupGetPageTypeCmd;
  }

  @Test
  public void testInvalidateCacheForDatabase_celements2web() {
    assertNotNull(xObjPageTypeCache.getPageTypeRefCache());
    replayDefault();
    xObjPageTypeCache.invalidateCacheForDatabase("celements2web");
    assertNull(xObjPageTypeCache.pageTypeRefCache);
    verifyDefault();
  }

  @Test
  public void testInvalidateCacheForDatabase() {
    Map<String, List<PageTypeReference>> pageTypeRefCache =
        xObjPageTypeCache.getPageTypeRefCache();
    assertNotNull(pageTypeRefCache);
    PageTypeReference pageTypeRefMack = createMockAndAddToDefault(
        PageTypeReference.class);
    pageTypeRefCache.put(context.getDatabase(), Arrays.asList(pageTypeRefMack));
    assertTrue(pageTypeRefCache.containsKey(context.getDatabase()));
    replayDefault();
    xObjPageTypeCache.invalidateCacheForDatabase(context.getDatabase());
    assertNotNull(xObjPageTypeCache.pageTypeRefCache);
    assertFalse(pageTypeRefCache.containsKey(context.getDatabase()));
    verifyDefault();
  }

  @Test
  public void testGetPageTypesRefsForDatabase() throws Exception {
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
    replayDefault();
    List<PageTypeReference> allPageTypes = xObjPageTypeCache.getPageTypesRefsForDatabase(
        context.getDatabase());
    assertFalse("expecting RichtText page type reference.", allPageTypes.isEmpty());
    PageTypeReference richTextPTref = allPageTypes.get(0);
    assertEquals("RichText", richTextPTref.getConfigName());
    assertEquals(Arrays.asList(""), richTextPTref.getCategories());
    verifyDefault();
  }

}
