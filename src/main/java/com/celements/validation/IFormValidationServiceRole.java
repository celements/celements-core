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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.docform.DocFormRequestParam;

@ComponentRole
public interface IFormValidationServiceRole {

  /**
   * validates the given form fields by all {@link IRequestValidationRule} implementations
   */
  @NotNull
  List<ValidationResult> validate(@NotNull List<DocFormRequestParam> params);

  /**
   * validates any form fields in the given http request for all validationRule
   * implementations
   *
   * @return map [KEY = request field-name / VALUE = map [KEY = validation type / VALUE =
   *         set of validation messages (dictionary keys possible)]]
   */
  Map<String, Map<ValidationType, Set<String>>> validateRequest();

  /**
   * validates any form fields in the given Map for all validationRule implementations
   *
   * @return map [KEY = request field-name / VALUE = map [KEY = validation type / VALUE =
   *         set of validation messages (dictionary keys possible)]]
   */
  Map<String, Map<ValidationType, Set<String>>> validateMap(
      Map<String, String[]> requestMap);

  /**
   * validateField validates the given class name, field name and value for all
   * validationRule implementations
   *
   * @param className
   * @param fieldName
   * @param value
   * @return map [KEY = validation type / VALUE = set of validation messages (dictionary
   *         keys possible)]]
   */
  Map<ValidationType, Set<String>> validateField(String className, String fieldName,
      String value);

}
