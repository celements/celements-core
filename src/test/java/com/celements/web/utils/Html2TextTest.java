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
package com.celements.web.utils;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class Html2TextTest extends AbstractBridgedComponentTestCase {
  Html2Text h2t;
  
  @Before
  public void setUp_WebUtilsTest() {
    h2t = new Html2Text();
  }

  @Test
  public void testParse_simpleContent() throws IOException {
    Reader in = new StringReader("<html><head></head><body>Test String!</body></html>");
    h2t.parse(in);
    assertEquals("Test String!", h2t.getText());
  }

  @Test
  public void testParse_complexContent() throws IOException {
    Reader in = new StringReader("<html><head></head><body><div>Test String!<span>Mit &Uuml;ml&auml;uten.</span><div></body></html>");
    h2t.parse(in);
    assertEquals("Test String!\r\nMit Ümläuten.", h2t.getText());
  }

  @Test
  public void testHandleText() {
    StringBuffer sb = new StringBuffer();
    h2t.injectStringBuffer(sb);
    h2t.handleText(new char[]{'t', 'e', 's', 't'}, 100);
    assertEquals("test", h2t.getText());
    h2t.handleText(new char[]{'i', 't', ' ', 'n', 'o', 'w'}, 100);
    assertEquals("test\r\nit now", h2t.getText());
  }

  @Test
  public void testGetText() {
    h2t.injectStringBuffer(new StringBuffer("Test String"));
    assertEquals("Test String", h2t.getText());
  }
  
  @Test
  public void testInjectStringBuffer() {
    assertNotNull(h2t.getText());
    assertEquals("", h2t.getText());
    h2t.injectStringBuffer(new StringBuffer("Test String"));
    assertEquals("Test String", h2t.getText());
  }
}