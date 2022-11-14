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

import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Strings.*;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.StringEscapeUtils;

import com.celements.cells.attribute.CellAttribute;
import com.celements.cells.attribute.DefaultAttributeBuilder;

public class DivWriter implements ICellWriter {

  private static final String TAGNAME_DIV = "div";

  private StringBuilder out;
  private Deque<String> openLevels = new LinkedList<>();
  private boolean hasLevelContent = false;

  public DivWriter() {}

  public DivWriter(StringBuilder out) {
    this.out = checkNotNull(out);
  }

  @Override
  public Stream<String> getOpenLevels() {
    return openLevels.stream();
  }

  @Override
  public void closeLevel() {
    String tagName = openLevels.pop();
    getOut().append("</");
    getOut().append(tagName);
    getOut().append(">");
  }

  @Override
  @Deprecated
  public void openLevel(String tagName, String idname, String cssClasses, String cssStyles) {
    openLevel(tagName, new DefaultAttributeBuilder().addId(idname).addCssClasses(
        cssClasses).addStyles(cssStyles).build());
  }

  @Override
  public void openLevel() {
    openLevel("");
  }

  @Override
  public void openLevel(List<CellAttribute> attributes) {
    openLevel(null, attributes);
  }

  @Override
  public void openLevel(String tagName) {
    openLevel(tagName, Collections.<CellAttribute>emptyList());
  }

  @Override
  public void openLevel(String tagName, List<CellAttribute> attributes) {
    tagName = firstNonNull(emptyToNull(tagName), TAGNAME_DIV);
    openLevels.push(tagName);
    getOut().append("<");
    getOut().append(tagName);
    for (CellAttribute cellAttr : attributes) {
      String attrName = cellAttr.getName();
      getOut().append(" ");
      getOut().append(attrName);
      getOut().append("=\"");
      String attrValue = cellAttr.getValue().toJavaUtil()
          .orElse(attrName);
      // TODO CELDEV-343: check for HTML5 type. Only add default Value for XHMTL
      getOut().append(StringEscapeUtils.escapeHtml(attrValue));
      getOut().append("\"");
    }
    getOut().append(">");
    hasLevelContent = false;
  }

  @Override
  @Deprecated
  public void openLevel(String idname, String cssClasses, String cssStyles) {
    openLevel("", idname, cssClasses, cssStyles);
  }

  @Override
  public void clear() {
    out = null;
    openLevels.clear();
    hasLevelContent = false;
  }

  StringBuilder getOut() {
    if (out == null) {
      out = new StringBuilder();
    }
    return out;
  }

  @Override
  public boolean hasLevelContent() {
    return hasLevelContent;
  }

  @Override
  public DivWriter appendContent(String content) {
    content = nullToEmpty(content).trim();
    if (!content.isEmpty()) {
      getOut().append(content);
      hasLevelContent = true;
    }
    return this;
  }

  @Override
  public String getAsString() {
    return getOut().toString();
  }

  @Override
  public StringBuilder getAsStringBuilder() {
    return getOut();
  }

  @Override
  public String toString() {
    return getAsString();
  }

}
