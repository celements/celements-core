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
package com.celements.migrations;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SubSystemDBVersionTest {

  private SubSystemDBVersion version;

  @Before
  public void setUp() throws Exception {
    version = new SubSystemDBVersion();
  }

  @Test
  public void testSetSubSystemName() {
    version.setSubSystemName("testSubSystem");
    assertEquals("testSubSystem", version.getSubSystemName());
  }

  @Test
  public void testSetVersion() {
    version.setVersion(23456);
    assertEquals(23456, version.getVersion());
  }

  @Test
  public void testSubSystemDBVersion_constructor() {
    version = new SubSystemDBVersion("testSubSystem2", 2345);
    assertEquals("testSubSystem2", version.getSubSystemName());
    assertEquals(2345, version.getVersion());
  }

}
