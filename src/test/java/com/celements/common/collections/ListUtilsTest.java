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
package com.celements.common.collections;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ListUtilsTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testImplode_null() {
    assertEquals("", ListUtils.implode(null, ", "));
  }

  @Test
  public void testImplode_empty() {
    List<String> list = new ArrayList<String>();
    assertEquals("", ListUtils.implode(list, ", "));
  }

  @Test
  public void testImplode() {
    List<String> list = new ArrayList<String>();
    list.add("String 1");
    list.add("String 2");
    list.add("String 3");
    assertEquals("String 1, String 2, String 3", ListUtils.implode(list, ", "));
  }
}
