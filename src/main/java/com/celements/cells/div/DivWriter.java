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
package com.celements.cells.div;

import static com.celements.common.MoreOptional.*;
import static com.google.common.base.Preconditions.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringEscapeUtils;

import com.celements.cells.AbstractWriter;
import com.celements.cells.attribute.CellAttribute;

@NotThreadSafe
public class DivWriter extends AbstractWriter {

  public static final String DEFAULT_TAGNAME = "div";
  private static final Set<String> VOID_ELEMENTS = Set.of("area", "base", "br", "col", "embed",
      "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr");

  private final StringBuilder out;

  public DivWriter() {
    this(new StringBuilder());
  }

  public DivWriter(StringBuilder out) {
    this.out = checkNotNull(out);
  }

  @Override
  public void closeLevel() {
    if (!openLevels.isEmpty()) {
      String tagName = openLevels.pop().getKey();
      if (!VOID_ELEMENTS.contains(tagName)) {
        out.append("</").append(tagName).append(">");
      }
    }
  }

  @Override
  public void openLevel(@Nullable String tagName, @NotNull List<CellAttribute> attributes) {
    tagName = asNonBlank(tagName).orElse(DEFAULT_TAGNAME);
    getCurrentLevel().ifPresent(e -> e.setValue(true));
    openLevels.push(new SimpleEntry<>(tagName, false));
    out.append("<");
    out.append(tagName);
    for (CellAttribute cellAttr : attributes) {
      String attrName = cellAttr.getName();
      out.append(" ");
      out.append(attrName);
      cellAttr.getValue().ifPresent(attrValue -> {
        out.append("=\"");
        out.append(StringEscapeUtils.escapeHtml(attrValue));
        out.append("\"");
      });
    }
    out.append(">");
  }

  @Override
  public boolean hasLevelContent() {
    return hasLevelContentOptional()
        .orElse(out.length() > 0);
  }

  @Override
  public DivWriter appendContent(@Nullable String content) {
    final String con = Objects.toString(content, "").trim();
    if (!con.isEmpty() && !getOpenLevels().findFirst()
        .map(VOID_ELEMENTS::contains).orElse(false)) {
      getCurrentLevel().ifPresent(e -> e.setValue(true));
      out.append(con);
    }
    return this;
  }

  @Override
  public void clear() {
    super.clear();
    out.setLength(0);
  }

  @Override
  public String getAsString() {
    return out.toString();
  }

  @Override
  public StringBuilder getAsStringBuilder() {
    return out;
  }

  @Override
  public String toString() {
    return getAsString();
  }

}
