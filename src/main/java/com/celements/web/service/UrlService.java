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

import java.nio.charset.Charset;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface UrlService {

  @NotNull
  public String getURL(@Nullable DocumentReference docRef);

  @NotNull
  public String getURL(@Nullable DocumentReference docRef, @Nullable String action);

  @NotNull
  public String getURL(@Nullable DocumentReference docRef, @Nullable String action,
      @Nullable String queryString);

  @NotNull
  public String getURL(@Nullable AttachmentReference docRef);

  @NotNull
  public String getURL(@Nullable AttachmentReference docRef, @Nullable String action);

  @NotNull
  public String getURL(@Nullable AttachmentReference docRef, @Nullable String action,
      @Nullable String queryString);

  @NotNull
  public String getExternalURL(@Nullable DocumentReference docRef);

  @NotNull
  public String getExternalURL(@Nullable DocumentReference docRef, @Nullable String action);

  @NotNull
  public String getExternalURL(@Nullable DocumentReference docRef, @Nullable String action,
      @Nullable String queryString);

  @NotNull
  public String getExternalURL(@Nullable AttachmentReference attRef);

  @NotNull
  public String getExternalURL(@Nullable AttachmentReference attrRef, @Nullable String action);

  @NotNull
  public String getExternalURL(@Nullable AttachmentReference attrRef, @Nullable String action,
      @Nullable String queryString);

  @NotNull
  String encodeUrl(@Nullable String url);

  @NotNull
  String encodeUrl(@Nullable String url, @NotNull Charset encoding);

}
