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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.web.service.IWebUtilsService;

@Component
public class FormValidationService implements IFormValidationServiceRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(FormValidationService.class);

  private static final MapHandler<String, ValidationType, String> MAPHANDLER = new MapHandler<String, ValidationType, String>();

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private Map<String, IRequestValidationRuleRole> requestValidationRules;

  @Requirement
  private Map<String, IFieldValidationRuleRole> fieldValidationRules;

  void injectValidationRules(Map<String, IRequestValidationRuleRole> requestValidationRules,
      Map<String, IFieldValidationRuleRole> fieldValidationRules) {
    this.requestValidationRules = requestValidationRules;
    this.fieldValidationRules = fieldValidationRules;
  }

  public Map<String, Map<ValidationType, Set<String>>> validateRequest() {
    LOGGER.trace("validateRequest called");
    Map<String, String[]> requestMap = webUtils.getRequestParameterMap();
    LOGGER.debug("validateRequest: requestMap '" + requestMap + "'");
    Map<String, Map<ValidationType, Set<String>>> validatedMap = validateMap(requestMap);
    LOGGER.debug("validateRequest: validatedMap '" + validatedMap + "'");
    return validatedMap;
  }

  public Map<String, Map<ValidationType, Set<String>>> validateMap(
      Map<String, String[]> requestMap) {
    Map<String, Map<ValidationType, Set<String>>> ret = new HashMap<String, Map<ValidationType, Set<String>>>();
    Map<RequestParameter, String[]> convertedRequestMap = convertMapKeys(requestMap);
    for (IRequestValidationRuleRole validationRule : requestValidationRules.values()) {
      LOGGER.trace("Calling validateRequest() for rule '" + validationRule + "'");
      MAPHANDLER.mergeMultiMaps(validationRule.validateRequest(convertedRequestMap), ret);
    }
    return ret;
  }

  Map<RequestParameter, String[]> convertMapKeys(Map<String, String[]> requestMap) {
    Map<RequestParameter, String[]> convMap = new HashMap<RequestParameter, String[]>();
    for (String key : requestMap.keySet()) {
      RequestParameter requestparam = RequestParameter.create(key);
      if (requestparam != null) {
        convMap.put(requestparam, requestMap.get(key));
      }
    }
    return convMap;
  }

  public Map<ValidationType, Set<String>> validateField(String className, String fieldName,
      String value) {
    Map<ValidationType, Set<String>> ret = new HashMap<ValidationType, Set<String>>();
    for (IFieldValidationRuleRole validationRule : fieldValidationRules.values()) {
      LOGGER.trace("Calling validateField() for rule '" + validationRule + "'");
      MAPHANDLER.mergeMaps(validationRule.validateField(className, fieldName, value), ret);
    }
    return ret;
  }

}
