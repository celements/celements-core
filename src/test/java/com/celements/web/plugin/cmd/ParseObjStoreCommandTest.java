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

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiContext;

public class ParseObjStoreCommandTest extends AbstractBridgedComponentTestCase{
  ParseObjStoreCommand cmd;
  XWikiContext context;
  
  @Before
  public void setUp_ParseObjStoreCommandTest() throws Exception {
    context = new XWikiContext();
    cmd = new ParseObjStoreCommand();
  }

  @Test
  public void testGetObjStoreOptionsMap_nullOptions() {
    Map<String, String> map = cmd.getObjStoreOptionsMap(null, context);
    assertNotNull(map);
    assertEquals(0, map.size());
  }

  @Test
  public void testGetObjStoreOptionsMap_emptyOptions() {
    Map<String, String> map = cmd.getObjStoreOptionsMap("", context);
    assertNotNull(map);
    assertEquals(0, map.size());
  }

  @Test
  public void testGetObjStoreOptionsMap_alreadyParsedGetsSameObj() {
    Map<String, String> map = cmd.getObjStoreOptionsMap("Hi", context);
    assertNotSame(map, cmd.getObjStoreOptionsMap("Hallo", context));
    assertSame(map, cmd.getObjStoreOptionsMap("Hi", context));
  }

  @Test
  public void testGetObjStoreOptionsMap_aCheckbox() {
    Map<String, String> map = cmd.getObjStoreOptionsMap("Hi", context);
    assertTrue(map.containsKey("Hi"));
  }

  @Test
  public void testGetObjStoreOptionsMap_wrongFormattedRadio1() {
    Map<String, String> map = cmd.getObjStoreOptionsMap("hi;member:Isch bin!;10;20;;0;;3", 
        context);
    System.out.println(map);
    assertTrue(map.containsKey("hi;member:Isch bin!;10;20;;0;;3"));
  }

  @Test
  public void testGetObjStoreOptionsMap_wrongFormattedRadio2() {
    Map<String, String> map = cmd.getObjStoreOptionsMap("member:Isch bin!", 
        context);
    assertTrue(map.containsKey("member:Isch bin!"));
  }

  @Test
  public void testGetObjStoreOptionsMap_wrongFormattedRadio3() {
    Map<String, String> map = cmd.getObjStoreOptionsMap("memberIsch bin!;10;20;;0;;3", 
        context);
    assertTrue(map.containsKey("memberIsch bin!;10;20;;0;;3"));
  }

  @Test
  public void testGetObjStoreOptionsMap_aRadio() {
    Map<String, String> map = cmd.getObjStoreOptionsMap("member:Isch bin!;10;20;;0;;3", 
        context);
    assertTrue(map.containsKey("member:3"));
    assertTrue(map.containsValue("Isch bin!"));
  }

  @Test
  public void testGetObjStoreOptionsMap_mixedCheckboxesAndRadio() {
    Map<String, String> map = cmd.getObjStoreOptionsMap("member:Isch bin!;10;20;;0;;3" +
        "\nHi\nthen\n\n  \n", 
        context);
    assertTrue(map.containsKey("member:3"));
    assertTrue(map.containsValue("Isch bin!"));
    assertTrue(map.containsKey("Hi"));
    assertTrue(map.containsKey("then"));
  }

  @Test
  public void testGetHash_emptyValues() {
    assertNotNull(cmd.getHash(null));
    assertNotNull(cmd.getHash(""));
  }

  @Test
  public void testGetHash_sameInSameOut() {
    String val1 = cmd.getHash("This is a text!");
    String val2 = cmd.getHash("This is a text!");
    assertEquals(val1, val2);
  }

  @Test
  public void testGetHash_differentInDifferentOut() {
    String val1 = cmd.getHash("This is a text!");
    String val2 = cmd.getHash("");
    assertFalse("expected '" + val1 + "' != '" + val2 + "'", val1.equals(val2));
  }
}
