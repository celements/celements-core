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
package com.celements.inheritor;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.doc.XWikiDocument;

public class DefaultEmptyDocumentCheckerTest extends AbstractComponentTest {

  private IEmptyDocumentChecker _emptyDocumentChecker;

  @Before
  public void setUp_DefaultEmptyFieldCheckerTest() throws Exception {
    _emptyDocumentChecker = new DefaultEmptyDocumentChecker();
  }

  @Test
  public void testIsEmpty_documentNotExist() {
    assertTrue(_emptyDocumentChecker.isEmpty(null));
  }

  @Test
  public void testIsEmpty_emptyTitle() {
    XWikiDocument doc = new XWikiDocument();
    doc.setTitle("Title");
    assertFalse(_emptyDocumentChecker.isEmptyTitle(doc));
    doc.setTitle("");
    assertTrue(_emptyDocumentChecker.isEmptyTitle(doc));
  }

  @Test
  public void testIsEmpty_emptyContent() {
    XWikiDocument doc = new XWikiDocument();
    doc.setContent("Content");
    assertFalse(_emptyDocumentChecker.isEmptyContent(doc));
    doc.setContent("");
    assertTrue(_emptyDocumentChecker.isEmptyContent(doc));
  }

  @Test
  public void testIsEmpty() {
    XWikiDocument doc = new XWikiDocument();
    doc.setTitle("Title");
    doc.setContent("Content");
    assertFalse(_emptyDocumentChecker.isEmpty(doc));
    doc.setTitle("");
    doc.setContent("");
    assertTrue(_emptyDocumentChecker.isEmpty(doc));
  }
}
