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
package com.celements.common.classes;


import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

public class CelementsClassCollectionTest extends AbstractBridgedComponentTestCase {

  private TestClassCollection testClassColl;
  private XWikiContext context;
  private XWiki xwiki;

  @Before
  public void setUp_CelementsClassCollectionTest() throws Exception {
    testClassColl = new TestClassCollection();
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
  }

  @Test
  public void testRunUpdate_activated_Prefs() throws Exception {
    expect(xwiki.getXWikiPreference(eq("activated_classcollections"), same(context))
        ).andReturn("mycol,testcol").anyTimes();
    expect(xwiki.Param(eq("celements.classcollections"), eq(""))
        ).andReturn("").anyTimes();
    replay(xwiki);
    testClassColl.runUpdate(context);
    verify(xwiki);
    assertTrue(testClassColl.initClassExecuted);
  }

  @Test
  public void testRunUpdate_deactivated_Prefs() throws Exception {
    expect(xwiki.getXWikiPreference(eq("activated_classcollections"), same(context))
        ).andReturn("othercol,andcol").anyTimes();
    expect(xwiki.Param(eq("celements.classcollections"), eq(""))
      ).andReturn("").anyTimes();
    replay(xwiki);
    testClassColl.runUpdate(context);
    verify(xwiki);
    assertFalse(testClassColl.initClassExecuted);
  }

  @Test
  public void testRunUpdate_activated_Param() throws Exception {
    expect(xwiki.getXWikiPreference(eq("activated_classcollections"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.classcollections"), eq(""))
        ).andReturn("mycol,testcol").anyTimes();
    replay(xwiki);
    testClassColl.runUpdate(context);
    verify(xwiki);
    assertTrue(testClassColl.initClassExecuted);
  }

  @Test
  public void testRunUpdate_deactivated_Param() throws Exception {
    expect(xwiki.getXWikiPreference(eq("activated_classcollections"), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.Param(eq("celements.classcollections"), eq(""))
      ).andReturn("othercol,andcol").anyTimes();
    replay(xwiki);
    testClassColl.runUpdate(context);
    verify(xwiki);
    assertFalse(testClassColl.initClassExecuted);
  }

  
  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  static class TestClassCollection extends CelementsClassCollection {
    
    private final static Log mLogger = LogFactory.getFactory().getInstance(
        TestClassCollection.class);

    boolean initClassExecuted = false;

    public void initClasses(XWikiContext context) throws XWikiException {
      initClassExecuted = true;
    }

    public String getConfigName() {
      return "testcol";
    }

    @Override
    protected Log getLogger() {
      return mLogger;
    }

  }

}
