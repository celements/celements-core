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

import java.util.Deque;
import java.util.LinkedList;

import org.python.google.common.base.Strings;

public class DivWriter implements ICellWriter {

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
    if (Strings.isNullOrEmpty(tagName)) {
      tagName = "div";
    }
    openLevels.push(tagName);
    getOut().append("<");
    getOut().append(tagName);
    if ((idname != null) && !"".equals(idname)) {
      getOut().append(" id=\"");
      getOut().append(idname);
      getOut().append("\"");
    }
    if ((cssClasses != null) && !"".equals(cssClasses)) {
      getOut().append(" class=\"");
      getOut().append(cssClasses);
      getOut().append("\"");
    }
    if ((cssStyles != null) && !"".equals(cssStyles)) {
      getOut().append(" style=\"");
      getOut().append(cssStyles.replaceAll("[\n\r]", ""));
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
