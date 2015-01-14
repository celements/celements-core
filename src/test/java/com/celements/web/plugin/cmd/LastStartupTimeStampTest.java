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

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class LastStartupTimeStampTest {

  private LastStartupTimeStamp lastStartupTS;

  @Before
  public void setUp() throws Exception {
    lastStartupTS = new LastStartupTimeStamp();
  }

  @Test
  public void testGetLastStartupTimeStamp() {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
    String lastStartUpTimeStampExpected = format.format(new Date());
    String lastStartupTimeStamp = lastStartupTS.getLastStartupTimeStamp();
    assertTrue(lastStartupTimeStamp.startsWith(lastStartUpTimeStampExpected));
    assertEquals("expecting yyyyMMddHHmmss", 14, lastStartupTimeStamp.length());
  }

  @Test
  public void testGetLastChangedTimeStamp() {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
    Date lastChangeDate = new Date();
    String lastStartUpTimeStampExpected = format.format(lastChangeDate);
    String lastStartupTimeStamp = lastStartupTS.getLastChangedTimeStamp(lastChangeDate);
    assertTrue(lastStartupTimeStamp.startsWith(lastStartUpTimeStampExpected));
    assertEquals("expecting yyyyMMddHHmmss", 14, lastStartupTimeStamp.length());
  }

}
