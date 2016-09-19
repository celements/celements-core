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
package com.celements.cells;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.python.google.common.base.Strings;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.CellAttribute;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

public class DivWriter implements ICellWriter {

  private static final String TAGNAME_DIV = "div";

  private StringBuilder out;
  private Deque<String> openLevels = new LinkedList<>();

  @Override
  public void closeLevel() {
    String tagName = openLevels.pop();
    getOut().append("</");
    getOut().append(tagName);
    getOut().append(">");
  }

  @Override
  public void openLevel(String tagName, String idname, String cssClasses, String cssStyles) {
    openLevel(tagName, new AttributeBuilder().addId(idname).addCssClasses(cssClasses).addStyles(
        cssStyles).build());
  }

  @Override
  public void openLevel() {
    openLevel("");
  }

  @Override
  public void openLevel(List<CellAttribute> attributes) {
    openLevel("", attributes);
  }

  @Override
  public void openLevel(String tagName) {
    openLevel(tagName, Collections.<CellAttribute>emptyList());
  }

  @Override
  public void openLevel(String tagName, List<CellAttribute> attributes) {
    tagName = MoreObjects.firstNonNull(Strings.emptyToNull(tagName), TAGNAME_DIV);
    openLevels.push(tagName);
    getOut().append("<");
    getOut().append(tagName);
    for (CellAttribute cellAttr : attributes) {
      String attrName = cellAttr.getName();
      getOut().append(" ");
      getOut().append(attrName);
      getOut().append("=\"");
      Optional<String> attrValue = cellAttr.getValue();
      // TODO check for HTML5 type. Only add default Value for XHMTL
      getOut().append(attrValue.or(attrName));
      getOut().append("\"");
    }
    getOut().append(">");
  }

  @Override
  @Deprecated
  public void openLevel(String idname, String cssClasses, String cssStyles) {
    openLevel("", idname, cssClasses, cssStyles);
  }

  @Override
  public void clear() {
    out = null;
  }

  StringBuilder getOut() {
    if (out == null) {
      out = new StringBuilder();
    }
    return out;
  }

  @Override
  public String getAsString() {
    return getOut().toString();
  }

  @Override
  public void appendContent(String content) {
    getOut().append(content);
  }

}
