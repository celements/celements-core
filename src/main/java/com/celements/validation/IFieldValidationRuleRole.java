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
package com.celements.validation;

import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IFieldValidationRuleRole {

  /**
   * validates the given class name, field name and value
   * 
   * @param className
   * @param fieldName
   * @param value
   * @return empty map if value passes validation, else map [KEY = validation type / VALUE
   *         = set of validation messages (dictionary keys possible)]
   */
  public Map<ValidationType, Set<String>> validateField(String className, String fieldName,
      String value);

}
