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


public class ListBuilder implements INavigationBuilder {

  private StringBuilder outStream;
  private boolean isFirstItem;
  private String uniqueName;

  public ListBuilder(String navUniqueId) {
    this.uniqueName = navUniqueId;
  }
  
  public void openLevel(String mainUlCSSClasses) {
    outStream.append("<ul" + addCssClasses(mainUlCSSClasses) + ">");
    isFirstItem = true;
  }
  
  private String addCssClasses(String cssClasses) {
    if (!"".equals(cssClasses.trim())) {
      return " class=\"" + cssClasses.trim() + "\" ";
    } else {
      return "";
    }
  }

  public void closeLevel() {
    outStream.append("</ul>");
  }

  public String getLayoutTypeName() {
    return Navigation.LIST_LAYOUT_TYPE;
  }

  public void useStream(StringBuilder outStream) {
    this.outStream = outStream;
  }

  private String addUniqueElementId(String menuItemName) {
    return "id=\"" + getUniqueId(menuItemName) + "\"";
  }

  String getUniqueId(String... params) {
    String uniqueId = uniqueName + ":";
    for (String param : params) {
      uniqueId += param + ":";
    }
    return uniqueId;
  }

  String getCssClasses(boolean isFirstItem,
      boolean isLastItem, boolean isActive, String additionalCssClasses) {
    String cssClass = additionalCssClasses;
    if (isFirstItem) {
      cssClass += " first";
    }
    if (isLastItem) {
      cssClass += " last";
    }
    if (isActive) {
      cssClass += " active";
    }
    return cssClass;
  }

  public void appendMenuItemLink(String menuItemName, String hrefLink,
      String multilingualName, boolean isActive, boolean isLastItem,
      String cssClasses) {
    outStream.append("<a " + addCssClasses(getCssClasses(isFirstItem,
        isLastItem, isActive, cssClasses))
        + " " + addUniqueElementId(menuItemName) + " href=\""
        + hrefLink + "\">" + multilingualName + "</a>");
    isFirstItem = false;
  }

  public void closeMenuItemOut() {
    outStream.append("<!-- IE6 --></li>");
    isFirstItem = false;
  }

  public void openMenuItemOut() {
    outStream.append("<li>");
  }

}
