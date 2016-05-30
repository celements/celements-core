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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class RequestParameter {

  private static Log LOGGER = LogFactory.getFactory().getInstance(RequestParameter.class);

  private final String parameterName;
  private final String className;
  private final long objectNr;
  private final String fieldName;

  private RequestParameter(String parameterName, String className, long objectNr,
      String fieldName) {
    this.parameterName = parameterName;
    this.className = className;
    this.objectNr = objectNr;
    this.fieldName = fieldName;
  }

  /**
   * @param requestParameterName
   * @return new RequestParameter or null if requestParameterName is invalid
   */
  public static RequestParameter create(String requestParameterName) {
    if (isValidRequestParam(requestParameterName)) {
      int pos = includesDocName(requestParameterName) ? 1 : 0;
      String[] paramSplit = requestParameterName.split("_");
      String className = paramSplit[pos];
      long objectNr = Long.parseLong(paramSplit[pos + 1]);
      String fieldName = paramSplit[pos + 2];
      for (int i = pos + 3; i < paramSplit.length; i++) {
        fieldName += "_" + paramSplit[i];
      }
      return new RequestParameter(requestParameterName, className, objectNr, fieldName);
    }
    LOGGER.debug("request parameter is not valid '" + requestParameterName + "'");
    return null;
  }

  static boolean isValidRequestParam(String paramName) {
    return paramName.matches("([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){1,2}-?(\\d)*_(.*)");
  }

  static boolean includesDocName(String paramName) {
    return paramName.matches("([a-zA-Z0-9]*\\.[a-zA-Z0-9]*_){2}-?(\\d)*_(.*)");
  }

  public String getParameterName() {
    return parameterName;
  }

  public String getClassName() {
    return className;
  }

  public long getObjectNr() {
    return objectNr;
  }

  public String getFieldName() {
    return fieldName;
  }

  @Override
  public int hashCode() {
    return parameterName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RequestParameter) {
      return parameterName.equals(((RequestParameter) obj).parameterName);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "FieldName [parameterName=" + parameterName + ", className=" + className + ", objectNr="
        + objectNr + ", fieldName=" + fieldName + "]";
  }

}
