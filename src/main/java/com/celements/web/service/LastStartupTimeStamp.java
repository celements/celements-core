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
package com.celements.web.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.web.FileAction;
import com.xpn.xwiki.XWikiContext;

@Singleton
@Component
public class LastStartupTimeStamp implements LastStartupTimeStampRole {

  private volatile String lastStartUpTimeStamp;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public LastStartupTimeStamp() {
    this.lastStartUpTimeStamp = getLastChangedTimeStamp(new Date());
  }

  @Override
  @NotNull
  public String getLastStartupTimeStamp() {
    return lastStartUpTimeStamp;
  }

  @Override
  public synchronized void resetLastStartupTimeStamp() {
    lastStartUpTimeStamp = getLastChangedTimeStamp(new Date());
  }

  @Override
  @NotNull
  public String getLastChangedTimeStamp(@NotNull Date lastChangeDate) {
    return new SimpleDateFormat("yyyyMMddHHmmss").format(lastChangeDate);
  }

  @Override
  @NotNull
  public String getFileModificationDate(@NotNull String path) {
    return getLastChangedTimeStamp(getContext().getWiki().getResourceLastModificationDate(
        FileAction.RESOURCES_DIRECTORY + FileAction.DELIMITER + path));
  }

}
