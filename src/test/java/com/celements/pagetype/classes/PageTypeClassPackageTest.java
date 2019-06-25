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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.ClassPackage;
import com.xpn.xwiki.web.Utils;

public class PageTypeClassPackageTest extends AbstractComponentTest {

  private PageTypeClassPackage pageTypeClassPackage;

  @Before
  public void prepareTest() throws Exception {
    pageTypeClassPackage = (PageTypeClassPackage) Utils.getComponent(ClassPackage.class,
        PageTypeClassPackage.NAME);
  }

  @Test
  public void getNameTest() {
    String expectedStr = "pagetype";
    assertEquals(expectedStr, pageTypeClassPackage.getName());
  }

  @Test
  public void getClassDefinitionsTest() {
    assertEquals(2, pageTypeClassPackage.getClassDefinitions().size());
    assertTrue(pageTypeClassPackage.getClassDefinitions().contains(Utils.getComponent(
        ClassDefinition.class, PageTypeClass.CLASS_DEF_HINT)));
    assertTrue(pageTypeClassPackage.getClassDefinitions().contains(Utils.getComponent(
        ClassDefinition.class, PageTypePropertiesClass.CLASS_DEF_HINT)));
  }

}
