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
package com.celements.xwikiPatches;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class SpacePreferencesConfigurationSourceTest
      extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private SpacePreferencesConfigurationSource spacePrefConfig;
  private XWiki xwiki;
  private DocumentReference curDocRef;
  private XWikiDocument currentDoc;

  @Before
  public void setUp_SpacePreferencesConfigurationSourceTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    spacePrefConfig = (SpacePreferencesConfigurationSource) Utils.getComponent(
        ConfigurationSource.class, "space");
    curDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    currentDoc = new XWikiDocument(curDocRef);
    context.setDoc(currentDoc);
  }

  @Test
  public void testGetPropertyStringT() throws Exception {
    DocumentReference webPrefRef = new DocumentReference(context.getDatabase(), "mySpace",
        "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefRef);
    BaseObject webPrefObj = new BaseObject();
    DocumentReference xwikiPrefClassRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiPreferences");
    webPrefObj.setXClassReference(xwikiPrefClassRef);
    webPrefObj.setStringValue("default_language", "en");
    webPrefDoc.addXObject(webPrefObj);
    expect(xwiki.getDocument(eq(webPrefRef), same(context))).andReturn(webPrefDoc);

    replayDefault();
    String defLang = spacePrefConfig.getProperty("default_language", "");
    assertEquals("en", defLang);
    verifyDefault();
  }

}
