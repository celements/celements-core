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
package com.celements.web.plugin.cmd;

import org.jsoup.Jsoup;

import com.google.common.base.Strings;

@Deprecated
public class PlainTextCommand {

  /**
   * @deprecated since 5.9, instead use Jsoup.parse(String).text()
   */
  @Deprecated
  public String convertHtmlToPlainText(String htmlContent) throws ConvertToPlainTextException {
    try {
      return internalConvert(htmlContent);
    } catch (StackOverflowError | Exception exp) {
      // Catching Stackoverflow because of https://bugs.openjdk.java.net/browse/JDK-7172359
      throw new ConvertToPlainTextException("Fail to convertToPlainText.", exp);
    }
  }

  /**
   * @deprecated instead use <code>convertHtmlToPlainText</code>
   */
  @Deprecated
  public String convertToPlainText(String htmlContent) {
    return internalConvert(htmlContent);
  }

  private String internalConvert(String htmlContent) {
    return Jsoup.parse(Strings.nullToEmpty(htmlContent)).text();
  }

}
