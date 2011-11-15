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
package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class GetCellDocumentTest extends AbstractBridgedComponentTestCase {
  private XWikiContext context;
  private XWiki xwiki;
  private XWikiStoreInterface store;
  private GetCellDocument cmd;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    cmd = new GetCellDocument();
    context = getContext();
    xwiki = createMock(XWiki.class);
    store = createMock(XWikiStoreInterface.class);
    context.setWiki(xwiki);
  }

  @Test
  public void testGetCellDoc_FromContext() throws XWikiException {
    PageLayoutCommand plc = createMock(PageLayoutCommand.class);
    XWikiDocument doc = createMock(XWikiDocument.class);
    List<XWikiDocument> docList = new ArrayList<XWikiDocument>();
    docList.add(doc);
    List<String> paramList = new ArrayList<String>();
    paramList.add("Space");
    paramList.add("Class.Name");
    paramList.add("field");
    paramList.add("value");
    expect(xwiki.getStore()).andReturn(store).anyTimes();
    expect(store.searchDocuments((String)anyObject(), eq(paramList),same(context)))
        .andReturn(docList).once();
    XWikiDocument lDoc = new XWikiDocument("Space", "Name");
    expect(plc.getLayoutPropDoc(same(context))).andReturn(lDoc).once();
    replay(plc, store, xwiki);
    cmd.injectPageLayoutCommand(plc);
    XWikiDocument result = cmd.getCellDoc("Class.Name", "field", "value", 
        context);
    verify(plc, store, xwiki);
    assertEquals(doc, result);
  }

  @Test
  public void testGetCellDoc() throws XWikiException {
    XWikiDocument doc = createMock(XWikiDocument.class);
    List<XWikiDocument> docList = new ArrayList<XWikiDocument>();
    docList.add(doc);
    List<String> paramList = new ArrayList<String>();
    paramList.add("Space");
    paramList.add("Class.Name");
    paramList.add("field");
    paramList.add("value");
    expect(xwiki.getStore()).andReturn(store).anyTimes();
    expect(store.searchDocuments((String)anyObject(), eq(paramList),same(context)))
        .andReturn(docList).once();
    replay(store, xwiki);
    XWikiDocument result = cmd.getCellDoc("Space", "Class.Name", "field", "value", 
        context);
    verify(store, xwiki);
    assertEquals(doc, result);
  }

}
