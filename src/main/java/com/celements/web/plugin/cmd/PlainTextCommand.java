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

import static com.google.common.base.Strings.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.web.utils.Html2Text;

public class PlainTextCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlainTextCommand.class);

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
    try {
      return internalConvert(htmlContent);
    } catch (IOException ioExp) {
      LOGGER.error("Fail to convertToPlainText: ", ioExp);
    }
    return "";
  }

  private String internalConvert(String htmlContent) throws IOException {
    Reader in = new StringReader(nullToEmpty(htmlContent));
    Html2Text parser = new Html2Text();
    parser.parse(in);
    in.close();
    return parser.getText();
  }

}
