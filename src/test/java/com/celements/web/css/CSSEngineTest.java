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
package com.celements.web.css;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.*;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

public class CSSEngineTest extends AbstractBridgedComponentTestCase {

  private CSSEngine cssEngine;
  private XWikiContext context;

  @Before
  public void setUp_CSSEngineTest() throws Exception {
    context = getContext();
    cssEngine = (CSSEngine) CSSEngine.getCSSEngine(context);
    context.put("vcontext", new VelocityContext());
  }

  @Test
  public void testIncludeCSS_nullObject() {
    List<BaseObject> baseCSSList = new ArrayList<BaseObject>();
    BaseObject cssObj = new BaseObject();
    baseCSSList.add(cssObj);
    baseCSSList.add(null);
    BaseObject cssObj1 = new BaseObject();
    baseCSSList.add(cssObj1);

    List<CSS> cssList = cssEngine.includeCSS("", "", baseCSSList , context);

    assertFalse("includeCSS must not add null objects to the css list.",
        cssListContains(cssList, null));
    assertTrue("includeCSS must add cssObj to the css list.",
        cssListContains(cssList, cssObj));
    assertTrue("includeCSS must not add cssObj1 to the css list.",
        cssListContains(cssList, cssObj1));
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private boolean cssListContains(List<CSS> cssList, BaseObject containsObj) {
    boolean found = false;
    for (CSS cssObj : cssList) {
      if (cssObj instanceof CSSBaseObject) {
        BaseObject foundObj = ((CSSBaseObject)cssObj).getObject();
        if (containsObj == foundObj) {
          found = true;
        }
      }
    }
    return found;
  }

}
