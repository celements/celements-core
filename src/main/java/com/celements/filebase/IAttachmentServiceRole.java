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
package com.celements.filebase;

import java.io.InputStream;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.AttachmentReference;

import com.celements.filebase.matcher.IAttFileNameMatcherRole;
import com.celements.filebase.matcher.IAttachmentMatcher;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.fileupload.FileUploadPlugin;

@ComponentRole
public interface IAttachmentServiceRole {

  public int uploadMultipleAttachments(XWikiDocument attachToDoc, String fieldNamePrefix);

  public boolean uploadAttachment(String fieldName, String filename, FileUploadPlugin fileupload,
      XWikiDocument doc) throws XWikiException;

  public XWikiAttachment addAttachment(XWikiDocument doc, byte[] data, String filename,
      String username, String comment) throws AttachmentToBigException,
          AddingAttachmentContentFailedException, DocumentSaveException;

  public XWikiAttachment addAttachment(XWikiDocument doc, InputStream in, String filename,
      String username, String comment) throws AttachmentToBigException,
          AddingAttachmentContentFailedException, DocumentSaveException;

  public String clearFileName(String fileName);

  public int deleteAttachmentList(List<AttachmentReference> attachmentRefList);

  public boolean existsAttachmentNameEqual(XWikiDocument document, String filename);

  public boolean existsAttachmentNameEqual(AttachmentReference attachmentRef)
      throws DocumentLoadException;

  public XWikiAttachment getAttachmentNameEqual(XWikiDocument document, String filename)
      throws AttachmentNotExistsException;

  public XWikiAttachment getAttachmentNameEqual(AttachmentReference attachmentRef)
      throws DocumentLoadException, AttachmentNotExistsException, DocumentNotExistsException;

  public List<XWikiAttachment> getAttachmentsNameMatch(XWikiDocument document,
      IAttachmentMatcher attMatcher);

  public Attachment getApiAttachment(XWikiAttachment attachment) throws NoAccessRightsException;

  public Attachment getApiAttachmentWithoutRightChecks(XWikiAttachment attachment);

  public XWikiAttachment getAttachmentFirstNameMatch(XWikiDocument document,
      IAttFileNameMatcherRole attMatcher) throws AttachmentNotExistsException;

}
