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
package com.celements.common.collections;

import java.util.List;


public class ListUtils {

  private ListUtils() {}
  
  /**
   * Provides a type safe substract for Lists.
   * @param <T>
   * @param list1
   * @param list2
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> subtract(List<T> list1, List<T> list2) {
    return org.apache.commons.collections.ListUtils.subtract(list1, list2);
  }
  
  public static String implode(List<String> list, String delimiter) {
    String implodedString = "";
    if(list != null) {
      for (String element : list) {
        if(implodedString.length() > 0) {
          implodedString += delimiter;
        }
        implodedString += element;
      }
    }
    return implodedString;
  }

}
