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

public interface INavigationBuilder {

  public void openLevel(String mainUlCSSClasses);

  public void closeLevel();

  public String getLayoutTypeName();

  /**
   * Deprecated the stream must be completely owned by the Builder
   *            clients must use toString to get the content of the
   *            stream.
   * @param outStream
   */
  @Deprecated
  public void useStream(StringBuilder outStream);

  public void openMenuItemOut();

  public void closeMenuItemOut();

  public void appendMenuItemLink(String menuItemName, String hrefLink,
      String multilingualName, boolean isActive, boolean isLastItem,
      String cssClasses);

}
