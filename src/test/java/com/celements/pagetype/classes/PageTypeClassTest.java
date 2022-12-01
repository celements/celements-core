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
package com.celements.pagetype.classes;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.common.classes.XClassCreator;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.ClassDefinition;
import com.celements.pagetype.PageTypeClasses;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class PageTypeClassTest extends AbstractComponentTest {

  private PageTypeClass pageTypeClass;
  private PageTypeClasses pageTypeClassConfig;
  private XClassCreator xClassCreator;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void prepareTest() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    pageTypeClass = (PageTypeClass) Utils.getComponent(ClassDefinition.class,
        PageTypeClass.CLASS_DEF_HINT);
    pageTypeClassConfig = (PageTypeClasses) Utils.getComponent(IClassCollectionRole.class,
        "celements.celPageTypeClasses");
    xClassCreator = Utils.getComponent(XClassCreator.class);
  }

  @Test
  public void testGetName() {
    String expectedStr = "Celements2.PageType";
    assertEquals(expectedStr, pageTypeClass.getName());
  }

  @Test
  public void test_isInternalMapping() {
    assertFalse(pageTypeClass.isInternalMapping());
  }

  @Test
  public void test_fields() throws Exception {
    DocumentReference pageTypeClassRef = pageTypeClass.getClassRef();

    XWikiDocument doc = new XWikiDocument(pageTypeClassRef);
    expect(modelAccessMock.getOrCreateDocument(eq(pageTypeClassRef))).andReturn(doc).once();
    modelAccessMock.saveDocument(same(doc), anyObject(String.class));
    expectLastCall().once();
    replayDefault();
    pageTypeClassConfig.getPageTypeClass();
    verifyDefault();

    assertEquals(doc.getXClass(), xClassCreator.generateXClass(pageTypeClass));
  }
}
