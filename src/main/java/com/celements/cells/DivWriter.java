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

public class DivWriter implements ICellWriter {

  private StringBuilder out;

  public void closeLevel() {
    getOut().append("</div>");
  }

  public void openLevel(String idname, String cssClasses, String cssStyles) {
    getOut().append("<div");
    if((idname != null) && !"".equals(idname)) {
      getOut().append(" id=\"" + idname + "\"");
    }
    if((cssClasses != null) && !"".equals(cssClasses)) {
      getOut().append(" class=\"" + cssClasses + "\"");
    }
    if((cssStyles != null) && !"".equals(cssStyles)) {
      getOut().append(" style=\"" + cssStyles.replaceAll("[\n\r]", "") + "\"");
    }
    getOut().append(">");
  }

  public void clear() {
    out = null;
  }

  StringBuilder getOut() {
    if (out == null) {
      out = new StringBuilder();
    }
    return out;
  }

  public String getAsString() {
    return getOut().toString();
  }

  public void appendContent(String content) {
    getOut().append(content);
  }

}
