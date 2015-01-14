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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.fileupload.FileUploadPlugin;
import com.xpn.xwiki.web.XWikiResponse;

/**
 * This Service makes the methods in the XWiki UploadAction-Class accessible
 * by other means. In celements we especially need them for the TokenBasedUpload because
 * the addAttachment on the Document class is buggy.
 * 
 * @author fabian
 * since 2.28.0
 *
 */
@Component
public class AttachmentService implements IAttachmentServiceRole {

  private static Logger _LOGGER  = LoggerFactory.getLogger(AttachmentService.class);

  /** The prefix of the corresponding filename input field name. */
  private static final String FILENAME_FIELD_NAME = "filename";

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  /**
   * Attach a file to the current document.
   * 
   * @param fieldName
   *          the target file field
   * @param filename
   * @param fileupload
   *          the {@link FileUploadPlugin} holding the form data
   * @param doc
   *          the target document
   * @return {@code true} if the file was successfully attached, {@code false}
   *         otherwise.
   * @throws XWikiException
   *           if the form data cannot be accessed, or if the database operation
   *           failed
   */
  @Override 
  public boolean uploadAttachment(String fieldName, String filename,
      FileUploadPlugin fileupload, XWikiDocument doc) throws XWikiException {
    XWikiResponse response = getContext().getResponse();
    String username = getContext().getUser();
    _LOGGER.debug("uploadAttachment: fieldName [" + fieldName + "], filename [" + filename
        + "], context username [" + username + "], doc [" + doc.getDocumentReference()
        + "].");

    // Read XWikiAttachment
    XWikiAttachment attachment = doc.getAttachment(filename);

    if (attachment == null) {
      attachment = new XWikiAttachment();
      doc.getAttachmentList().add(attachment);
    }

    try {
      attachment.setContent(fileupload.getFileItemInputStream(fieldName, getContext()));
    } catch (IOException e) {
      throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
          XWikiException.ERROR_XWIKI_APP_UPLOAD_FILE_EXCEPTION,
          "Exception while reading uploaded parsed file", e);
    }

    attachment.setFilename(filename);
    attachment.setAuthor(username);

    // Add the attachment to the document
    attachment.setDoc(doc);

    doc.setAuthor(username);
    if (doc.isNew()) {
      doc.setCreator(username);
    }

    // Adding a comment with a link to the download URL
    String comment;
    String nextRev = attachment.getNextVersion();
    ArrayList<String> params = new ArrayList<String>();
    params.add(filename);
    params.add(doc.getAttachmentRevisionURL(filename, nextRev, getContext()));
    if (attachment.isImage(getContext())) {
      comment = getContext().getMessageTool().get("core.comment.uploadImageComment",
          params);
    } else {
      comment = getContext().getMessageTool().get("core.comment.uploadAttachmentComment",
          params);
    }

    // Save the document.
    try {
      _LOGGER.debug("uploadAttachment: save document [" + doc.getDocumentReference()
          + "] after adding filename [" + filename + "] in revision [" + nextRev + "].");
      getContext().getWiki().saveDocument(doc, comment, getContext());
    } catch (XWikiException e) {
      // check Exception is ERROR_XWIKI_APP_JAVA_HEAP_SPACE when saving
      // Attachment
      if (e.getCode() == XWikiException.ERROR_XWIKI_APP_JAVA_HEAP_SPACE) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        getContext().put("message", "javaheapspace");
        return true;
      }
      throw e;
    }
    return false;
  }

  /**
   * 
   * @param attachToDoc
   * @param fieldNamePrefix
   * @return number of saved attachments
   */
  @Override 
  public int uploadMultipleAttachments(XWikiDocument attachToDoc, String fieldNamePrefix
      ) {
    XWikiDocument doc = attachToDoc.clone();

    // The document is saved for each attachment in the group.
    FileUploadPlugin fileupload = (FileUploadPlugin) getContext().getWiki().getPlugin(
        "fileupload", getContext());
    Map<String, String> fileNames = new HashMap<String, String>();
    List<String> wrongFileNames = new ArrayList<String>();
    List<String> failedFiles = new ArrayList<String>();
    for (String fieldName : fileupload.getFileItemNames(getContext())) {
      try {
        if (fieldName.startsWith(fieldNamePrefix)) {
          String fileName = getFileName(fieldName, fieldNamePrefix, fileupload);
          if (fileName != null) {
            fileNames.put(fileName, fieldName);
          }
        }
      } catch (Exception ex) {
        wrongFileNames.add(fileupload.getFileName(fieldName, getContext()));
      }
    }

    if (_LOGGER.isTraceEnabled()) {
      _LOGGER.trace("uploadMultipleAttachments: found fileNames [ key: "
          + Arrays.deepToString(fileNames.keySet().toArray()) + " values : "
          + Arrays.deepToString(fileNames.values().toArray()) + "].");
    }
    for (Entry<String, String> file : fileNames.entrySet()) {
      try {
        uploadAttachment(file.getValue(), file.getKey(), fileupload, doc);
      } catch (Exception ex) {
        _LOGGER.warn("Saving uploaded file failed", ex);
        failedFiles.add(file.getKey());
      }
    }

    int numSavedFiles = fileNames.size() - failedFiles.size();
    _LOGGER.debug("Found files to upload: " + fileNames + ", Failed attachments: "
        + failedFiles + ", Wrong attachment names: " + wrongFileNames + ", saved files: "
        + numSavedFiles);
    return numSavedFiles;
  }

  /**
   * Extract the corresponding attachment name for a given file field. It can
   * either be specified in a separate form input field, or it is extracted from
   * the original filename.
   * 
   * @param fieldName
   *          the target file field
   * @param fieldNamePrefix
   *          the fieldName prefix
   * @param fileupload
   *          the {@link FileUploadPlugin} holding the form data
   * @return a valid attachment name
   * @throws XWikiException
   *           if the form data cannot be accessed, or if the specified filename
   *           is invalid
   */
  String getFileName(String fieldName, String fieldNamePrefix,
      FileUploadPlugin fileupload) throws XWikiException {
    String filenameField = FILENAME_FIELD_NAME
        + fieldName.substring(fieldNamePrefix.length());
    String filename = null;

    // Try to use the name provided by the user
    filename = fileupload.getFileItemAsString(filenameField, getContext());
    if (!StringUtils.isBlank(filename)) {
      // TODO These should be supported, the URL should just contain escapes.
      if (filename.indexOf("/") != -1 || filename.indexOf("\\") != -1
          || filename.indexOf(";") != -1) {
        throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
            XWikiException.ERROR_XWIKI_APP_INVALID_CHARS, "Invalid filename: " + filename);
      }
    }

    if (StringUtils.isBlank(filename)) {
      // Try to get the actual filename on the client
      String fname = fileupload.getFileName(fieldName, getContext());
      if (StringUtils.indexOf(fname, "/") >= 0) {
        fname = StringUtils.substringAfterLast(fname, "/");
      }
      if (StringUtils.indexOf(fname, "\\") >= 0) {
        fname = StringUtils.substringAfterLast(fname, "\\");
      }
      filename = fname;
    }
    // Sometimes spaces are replaced with '+' by the browser.
    filename = filename.replaceAll("\\+", " ");

    if (StringUtils.isBlank(filename)) {
      // The file field was left empty, ignore this
      return null;
    }

    // Issues fixed by the clearName :
    // 1) Attaching images with a name containing special characters generates
    // bugs
    // (image are not displayed), XWIKI-2090.
    // 2) Attached files that we can't delete or link in the Wiki pages,
    // XWIKI-2087.
    filename = getContext().getWiki().clearName(filename, false, true, getContext());
    return filename;
  }

}
