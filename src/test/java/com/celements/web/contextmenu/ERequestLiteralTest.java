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
package com.celements.web.contextmenu;

import org.junit.*;

import com.celements.web.sajson.Parser;


public class ERequestLiteralTest {
  
  private Parser cmReqParser;

  @Before
  public void setUp() {
    cmReqParser = Parser.createLexicalParser(ERequestLiteral.REQUEST_ARRAY,
        new TestRequestHandler());
  }

  @Test
  public void testContextMenuJSON() throws Exception {
    String cmTestRequest = "[{\"cmClassName\": \"cel_cm_agenda\","
      + " \"elemIds\": [\"Agenda.Event1\"]},"
      + " {\"cmClassName\": \"cel_cm_navigation_menuitem\","
      + " \"elemIds\": [\"N1:Content.aktuelles\", \"N1:Content.NewsBox\","
      + " \"N1:Content.aktuellesBlog\", \"N1:Content.AgendaTest2\","
      + " \"N1:Content.Kontakt\", \"N1:Content.Suche\","
      + " \"N1:Content.mitglieder\", \"N1:Content.organisation\","
      + " \"N1:Content.clinicalaffairs\", \"N1:Content.weiterbildung\","
      + " \"N1:Content.publikum\", \"N1:Content.professionals\","
      + " \"N1:Content.rheumaschweiz\", \"N1:Content.links\"]}]";
    cmReqParser.parse(cmTestRequest);
  }
}
