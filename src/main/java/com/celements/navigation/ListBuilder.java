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
package com.celements.navigation;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

public class ListBuilder implements INavigationBuilder {

  private StringBuilder outStream;
  private boolean isFirstItem;
  private String uniqueName;

  public ListBuilder(String navUniqueId) {
    this.uniqueName = navUniqueId;
  }

  @Override
  @Deprecated
  public void openLevel(String mainUlCSSClasses) {
    openLevel(new TreeSet<String>(Arrays.asList(StringUtils.split(mainUlCSSClasses, ' '))));
  }

  @Override
  public void openLevel(Set<String> mainUlCSSClasses) {
    outStream.append("<ul");
    addCssClasses(mainUlCSSClasses);
    outStream.append(">");
    isFirstItem = true;
  }

  private void addCssClasses(Set<String> cssClasses) {
    if (!cssClasses.isEmpty()) {
      outStream.append(" class=\"");
      outStream.append(StringUtils.join(cssClasses, ' '));
      outStream.append("\" ");
    }
  }

  @Override
  public void closeLevel() {
    outStream.append("</ul>");
  }

  @Override
  public String getLayoutTypeName() {
    return NavigationConfig.LIST_LAYOUT_TYPE;
  }

  @Override
  public void useStream(StringBuilder outStream) {
    this.outStream = outStream;
  }

  private void addUniqueElementId(String menuItemName) {
    outStream.append("id=\"");
    outStream.append(getUniqueId(menuItemName));
    outStream.append("\"");
  }

  String getUniqueId(String... params) {
    String uniqueId = uniqueName + ":";
    for (String param : params) {
      uniqueId += param + ":";
    }
    return uniqueId;
  }

  Set<String> getCssClasses(boolean isFirstItem, boolean isLastItem, boolean isActive,
      Set<String> additionalCssClasses) {
    Set<String> cssClass = new LinkedHashSet<>(additionalCssClasses);
    if (isFirstItem) {
      cssClass.add("first");
    }
    if (isLastItem) {
      cssClass.add("last");
    }
    if (isActive) {
      cssClass.add("active");
    }
    return cssClass;
  }

  @Override
  @Deprecated
  public void appendMenuItemLink(String menuItemName, String hrefLink, String multilingualName,
      boolean isActive, boolean isLastItem, String cssClasses) {
    appendMenuItemLink(menuItemName, hrefLink, multilingualName, isActive, isLastItem,
        new LinkedHashSet<String>(Arrays.asList(StringUtils.split(cssClasses, ' '))));
  }

  @Override
  public void appendMenuItemLink(String menuItemName, String hrefLink, String multilingualName,
      boolean isActive, boolean isLastItem, Set<String> cssClasses) {
    outStream.append("<a ");
    addCssClasses(getCssClasses(isFirstItem, isLastItem, isActive, cssClasses));
    outStream.append(" ");
    addUniqueElementId(menuItemName);
    outStream.append(" href=\"");
    outStream.append(hrefLink);
    outStream.append("\">");
    outStream.append(multilingualName);
    outStream.append("</a>");
    isFirstItem = false;
  }

  @Override
  public void closeMenuItemOut() {
    outStream.append("<!-- IE6 --></li>");
    isFirstItem = false;
  }

  @Override
  public void openMenuItemOut() {
    outStream.append("<li>");
  }

}
