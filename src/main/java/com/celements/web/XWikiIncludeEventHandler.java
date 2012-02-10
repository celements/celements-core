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
package com.celements.web;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.event.IncludeEventHandler;

public class XWikiIncludeEventHandler implements IncludeEventHandler {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      XWikiIncludeEventHandler.class);

  public String includeEvent(String includeResourcePath, String currentResourcePath,
      String directiveName) {
    mLogger.trace("velocity include event: [" + includeResourcePath + "], ["
        + currentResourcePath + "], [" + directiveName + "]");
    String template = URI.create("/templates/" + includeResourcePath).normalize(
        ).toString();
    if (!template.startsWith("/templates/")) {
        mLogger.warn("Illegal access, tried to use file [" + template
            + "] as a template. Possible break-in attempt!");
        return null;
    }
    return template;
  }

}
