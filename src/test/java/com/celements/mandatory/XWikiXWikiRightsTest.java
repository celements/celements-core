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
package com.celements.mandatory;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.web.classes.oldcore.XWikiGlobalRightsClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class XWikiXWikiRightsTest extends AbstractComponentTest {

  private XWikiXWikiRights mandatoryXWikiRights;

  @Before
  public void prepareTest() throws Exception {
    mandatoryXWikiRights = (XWikiXWikiRights) getBeanFactory().getBean(
        "celements.mandatory.wikirights", IMandatoryDocumentRole.class);
  }

  @Test
  public void testDependsOnMandatoryDocuments() throws Exception {
    assertEquals(1, mandatoryXWikiRights.dependsOnMandatoryDocuments().size());
    assertEquals("celements.MandatoryGroups",
        mandatoryXWikiRights.dependsOnMandatoryDocuments().get(0));
  }

  @Test
  public void checkAccessRightObjs_createsMissingBaselineRightsWhenOtherGlobalRightsExist()
      throws Exception {
    XWikiDocument doc = new XWikiDocument(mandatoryXWikiRights.getDocRef());
    expectNewBaseObject(getGlobalRightsRef());
    expectNewBaseObject(getGlobalRightsRef());
    expectNewBaseObject(getGlobalRightsRef());
    replayDefault();

    createGlobalRights(doc, "XWiki.OtherGroup", "view");

    assertTrue(mandatoryXWikiRights.checkAccessRightObjs(doc));

    assertEquals(1, countGlobalRights(doc, "XWiki.OtherGroup", "view"));
    assertEquals(1, countGlobalRights(doc, "XWiki.ContentEditorsGroup", "edit,delete,undelete"));
    assertEquals(1, countGlobalRights(doc, "XWiki.XWikiAdminGroup",
        "admin,edit,comment,delete,undelete,register"));
    verifyDefault();
  }

  @Test
  public void checkAccessRightObjs_isIdempotent() throws Exception {
    XWikiDocument doc = new XWikiDocument(mandatoryXWikiRights.getDocRef());
    expectNewBaseObject(getGlobalRightsRef());
    expectNewBaseObject(getGlobalRightsRef());
    replayDefault();

    assertTrue(mandatoryXWikiRights.checkAccessRightObjs(doc));
    assertFalse(mandatoryXWikiRights.checkAccessRightObjs(doc));

    assertEquals(1, countGlobalRights(doc, "XWiki.ContentEditorsGroup", "edit,delete,undelete"));
    assertEquals(1, countGlobalRights(doc, "XWiki.XWikiAdminGroup",
        "admin,edit,comment,delete,undelete,register"));
    verifyDefault();
  }

  private BaseObject createGlobalRights(XWikiDocument doc, String groupFN, String levels) {
    BaseObject obj = XWikiObjectEditor.on(doc)
        .filter(new ClassReference(getGlobalRightsRef()))
        .createFirst();
    obj.setStringValue("groups", groupFN);
    obj.setStringValue("levels", levels);
    obj.setStringValue("users", "");
    obj.setIntValue("allow", 1);
    return obj;
  }

  private int countGlobalRights(XWikiDocument doc, String groupFN, String levels) {
    return XWikiObjectFetcher.on(doc)
        .filter(new ClassReference(getGlobalRightsRef()))
        .filter(obj -> obj.getIntValue("allow", 0) == 1)
        .filter(obj -> groupFN.equals(obj.getStringValue("groups")))
        .filter(obj -> levels.equals(obj.getStringValue("levels")))
        .filter(obj -> "".equals(obj.getStringValue("users")))
        .count();
  }

  private DocumentReference getGlobalRightsRef() {
    return XWikiGlobalRightsClass.CLASS_REF.getDocRef(mandatoryXWikiRights.getDocRef()
        .getWikiReference());
  }

}
