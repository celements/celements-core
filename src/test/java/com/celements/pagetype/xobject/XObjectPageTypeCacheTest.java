/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
import org.xwiki.model.reference.WikiReference;

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
  private WikiReference wikiRef;

  @Before
  public void setUp_XObjectPageTypeCacheTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    xObjPageTypeCache = (XObjectPageTypeCache) Utils.getComponent(IXObjectPageTypeCacheRole.class);
    getPageTypeCmdMock = createMockAndAddToDefault(GetPageTypesCommand.class);
    backupGetPageTypeCmd = xObjPageTypeCache.getPageTypeCmd;
    xObjPageTypeCache.getPageTypeCmd = getPageTypeCmdMock;
    wikiRef = new WikiReference(context.getDatabase());
  }

  @After
  public void tearDown_XObjectPageTypeCacheTest() {
    xObjPageTypeCache.getPageTypeCmd = backupGetPageTypeCmd;
  }

  @Test
  public void testInvalidateCacheForWiki_celements2web() {
    assertNotNull(xObjPageTypeCache.getPageTypeRefCache());
    replayDefault();
    xObjPageTypeCache.invalidateCacheForWiki(new WikiReference("celements2web"));
    assertNull(xObjPageTypeCache.pageTypeRefCache);
    verifyDefault();
  }

  @Test
  public void testInvalidateCacheForWiki() {
    Map<WikiReference, List<PageTypeReference>> pageTypeRefCache = xObjPageTypeCache.getPageTypeRefCache();
    assertNotNull(pageTypeRefCache);
    PageTypeReference pageTypeRefMack = createMockAndAddToDefault(PageTypeReference.class);
    pageTypeRefCache.put(wikiRef, Arrays.asList(pageTypeRefMack));
    assertTrue(pageTypeRefCache.containsKey(wikiRef));
    replayDefault();
    xObjPageTypeCache.invalidateCacheForWiki(wikiRef);
    assertNotNull(xObjPageTypeCache.pageTypeRefCache);
    assertFalse(pageTypeRefCache.containsKey(wikiRef));
    verifyDefault();
  }

  @Test
  public void testGetPageTypesRefsForWiki() throws Exception {
    Set<String> allPageTypeNames = new HashSet<String>(Arrays.asList("PageTypes.RichText"));
    expect(getPageTypeCmdMock.getAllXObjectPageTypes(same(context))).andReturn(allPageTypeNames);
    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(false).anyTimes();
    DocumentReference centralRichTextPTdocRef = new DocumentReference("celements2web", "PageTypes",
        "RichText");
    XWikiDocument centralRichTextPTdoc = new XWikiDocument(centralRichTextPTdocRef);
    expect(xwiki.getDocument(eq("celements2web:PageTypes.RichText"), same(context))).andReturn(
        centralRichTextPTdoc).anyTimes();
    replayDefault();
    List<PageTypeReference> allPageTypes = xObjPageTypeCache.getPageTypesRefsForWiki(wikiRef);
    assertFalse("expecting RichtText page type reference.", allPageTypes.isEmpty());
    PageTypeReference richTextPTref = allPageTypes.get(0);
    assertEquals("RichText", richTextPTref.getConfigName());
    assertEquals(Arrays.asList(""), richTextPTref.getCategories());
    try {
      allPageTypes.remove(0);
      fail("should throw UnsupportedOperationException");
    } catch (UnsupportedOperationException exc) {
      // expected
    }
    verifyDefault();
  }

}
