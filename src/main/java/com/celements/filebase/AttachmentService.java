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

import java.io.IOException;
import java.io.InputStream;
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
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentSaveException;
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
  IModelAccessFacade modelAccess;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public XWikiAttachment addAtachment(XWikiDocument doc, InputStream in, String filename,
      String username, String comment) throws AttachmentToBigException,
        AddingAttachmentContentFailedException, DocumentSaveException {
    //We do not want to change the document in xwiki cache in case an exception happens
    //those not saved changes would be left in memory
    XWikiDocument theDoc = (XWikiDocument) doc.clone();
    // Read XWikiAttachment
    XWikiAttachment attachment = theDoc.getAttachment(filename);

    if (attachment == null) {
      attachment = new XWikiAttachment();
      theDoc.getAttachmentList().add(attachment);
    }

    try {
      attachment.setContent(in);
    } catch (IOException exp) {
      throw new AddingAttachmentContentFailedException(exp);
    }

    attachment.setFilename(filename);
    attachment.setAuthor(username);

    // Add the attachment to the document
    attachment.setDoc(theDoc);
    theDoc.setAuthor(username);

    // Adding a comment with a link to the download URL
    String nextRev = attachment.getNextVersion();
    ArrayList<String> params = new ArrayList<String>();
    params.add(filename);
    params.add(theDoc.getAttachmentRevisionURL(filename, nextRev, getContext()));
    if (comment == null) {
      if (attachment.isImage(getContext())) {
        comment = getContext().getMessageTool().get("core.comment.uploadImageComment", 
            params);
      } else {
        comment = getContext().getMessageTool().get("core.comment.uploadAttachmentComment", 
            params);
      }
    }

    // Save the document.
    try {
      _LOGGER.debug("uploadAttachment: save document [" + theDoc.getDocumentReference()
          + "] after adding filename [" + filename + "] in revision [" + nextRev + "].");
      modelAccess.saveDocument(theDoc, comment);
    } catch (DocumentSaveException exp) {
      // check Exception is ERROR_XWIKI_APP_JAVA_HEAP_SPACE when saving
      // Attachment
      XWikiException xwe = (XWikiException)exp.getCause();
      if (xwe.getCode() == XWikiException.ERROR_XWIKI_APP_JAVA_HEAP_SPACE) {
        throw new AttachmentToBigException(xwe);
      }
      throw exp;
    }
    return attachment;
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

    if (doc.isNew()) {
      doc.setCreator(username);
    }
    try {
      InputStream in = fileupload.getFileItemInputStream(fieldName, getContext());
      addAtachment(doc, in, filename, username, null);
    } catch (AddingAttachmentContentFailedException|IOException exp) {
      throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
          XWikiException.ERROR_XWIKI_APP_UPLOAD_FILE_EXCEPTION,
          "Exception while reading uploaded parsed file", exp);
    } catch (AttachmentToBigException exp) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      getContext().put("message", "javaheapspace");
      return true;
    } catch (DocumentSaveException exp) {
      XWikiException xwe = (XWikiException)exp.getCause();
      throw xwe;
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
            XWikiException.ERROR_XWIKI_APP_INVALID_CHARS, "Invalid filename: " +
                filename);
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

    return clearFileName(filename);
  }

  @Override
  public String clearFileName(String fileName) {
    boolean stripDots = false;
    boolean ascii = true;
    String temp = fileName;
    temp = temp.replaceAll("[\u00c0\u00c1\u00c2\u00c3\u00c4\u00c5\u0100\u0102\u0104" +
        "\u01cd\u01de\u01e0\u01fa\u0200\u0202\u0226]", "A");
    temp = temp.replaceAll(
        "[\u00e0\u00e1\u00e2\u00e3\u00e4\u00e5\u0101\u0103\u0105\u01ce\u01df\u01e1" +
            "\u01fb\u0201\u0203\u0227]", "a");
    temp = temp.replaceAll("[\u00c6\u01e2\u01fc]", "AE");
    temp = temp.replaceAll("[\u00e6\u01e3\u01fd]", "ae");
    temp = temp.replaceAll("[\u008c\u0152]", "OE");
    temp = temp.replaceAll("[\u009c\u0153]", "oe");
    temp = temp.replaceAll("[\u00c7\u0106\u0108\u010a\u010c]", "C");
    temp = temp.replaceAll("[\u00e7\u0107\u0109\u010b\u010d]", "c");
    temp = temp.replaceAll("[\u00d0\u010e\u0110]", "D");
    temp = temp.replaceAll("[\u00f0\u010f\u0111]", "d");
    temp = temp.replaceAll("[\u00c8\u00c9\u00ca\u00cb\u0112\u0114\u0116\u0118\u011a" +
        "\u0204\u0206\u0228]", "E");
    temp = temp.replaceAll("[\u00e8\u00e9\u00ea\u00eb\u0113\u0115\u0117\u0119\u011b" +
        "\u01dd\u0205\u0207\u0229]", "e");
    temp = temp.replaceAll("[\u011c\u011e\u0120\u0122\u01e4\u01e6\u01f4]", "G");
    temp = temp.replaceAll("[\u011d\u011f\u0121\u0123\u01e5\u01e7\u01f5]", "g");
    temp = temp.replaceAll("[\u0124\u0126\u021e]", "H");
    temp = temp.replaceAll("[\u0125\u0127\u021f]", "h");
    temp = temp.replaceAll("[\u00cc\u00cd\u00ce\u00cf\u0128\u012a\u012c\u012e\u0130" +
        "\u01cf\u0208\u020a]", "I");
    temp = temp.replaceAll("[\u00ec\u00ed\u00ee\u00ef\u0129\u012b\u012d\u012f\u0131" +
        "\u01d0\u0209\u020b]", "i");
    temp = temp.replaceAll("[\u0132]", "IJ");
    temp = temp.replaceAll("[\u0133]", "ij");
    temp = temp.replaceAll("[\u0134]", "J");
    temp = temp.replaceAll("[\u0135]", "j");
    temp = temp.replaceAll("[\u0136\u01e8]", "K");
    temp = temp.replaceAll("[\u0137\u0138\u01e9]", "k");
    temp = temp.replaceAll("[\u0139\u013b\u013d\u013f\u0141]", "L");
    temp = temp.replaceAll("[\u013a\u013c\u013e\u0140\u0142\u0234]", "l");
    temp = temp.replaceAll("[\u00d1\u0143\u0145\u0147\u014a\u01f8]", "N");
    temp = temp.replaceAll("[\u00f1\u0144\u0146\u0148\u0149\u014b\u01f9\u0235]", "n");
    temp = temp.replaceAll("[\u00d2\u00d3\u00d4\u00d5\u00d6\u00d8\u014c\u014e\u0150" +
        "\u01d1\u01ea\u01ec\u01fe\u020c\u020e\u022a\u022c\u022e\u0230]", "O");
    temp = temp.replaceAll("[\u00f2\u00f3\u00f4\u00f5\u00f6\u00f8\u014d\u014f\u0151" +
        "\u01d2\u01eb\u01ed\u01ff\u020d\u020f\u022b\u022d\u022f\u0231]", "o");
    temp = temp.replaceAll("[\u0156\u0158\u0210\u0212]", "R");
    temp = temp.replaceAll("[\u0157\u0159\u0211\u0213]", "r");
    temp = temp.replaceAll("[\u015a\u015c\u015e\u0160\u0218]", "S");
    temp = temp.replaceAll("[\u015b\u015d\u015f\u0161\u0219]", "s");
    temp = temp.replaceAll("[\u00de\u0162\u0164\u0166\u021a]", "T");
    temp = temp.replaceAll("[\u00fe\u0163\u0165\u0167\u021b\u0236]", "t");
    temp = temp.replaceAll("[\u00d9\u00da\u00db\u00dc\u0168\u016a\u016c\u016e\u0170" +
        "\u0172\u01d3\u01d5\u01d7\u01d9\u01db\u0214\u0216]", "U");
    temp = temp.replaceAll("[\u00f9\u00fa\u00fb\u00fc\u0169\u016b\u016d\u016f\u0171" +
        "\u0173\u01d4\u01d6\u01d8\u01da\u01dc\u0215\u0217]", "u");
    temp = temp.replaceAll("[\u0174]", "W");
    temp = temp.replaceAll("[\u0175]", "w");
    temp = temp.replaceAll("[\u00dd\u0176\u0178\u0232]", "Y");
    temp = temp.replaceAll("[\u00fd\u00ff\u0177\u0233]", "y");
    temp = temp.replaceAll("[\u0179\u017b\u017d]", "Z");
    temp = temp.replaceAll("[\u017a\u017c\u017e]", "z");
    temp = temp.replaceAll("[\u00df]", "SS");
    temp = temp.replaceAll("[':,;\\\\/]", " ");
    temp = temp.replaceAll(" ", "-");
    fileName = temp;
    fileName = fileName.replaceAll("\\s+", "");
    fileName = fileName.replaceAll("[\\(\\)]", " ");
  
    if (stripDots) {
      fileName = fileName.replaceAll("[\\.]", "");
    }
  
    if (ascii) {
      fileName = fileName.replaceAll("[^a-zA-Z0-9\\-_\\.]", "");
    }
  
    if (fileName.length() > 250) {
      fileName = fileName.substring(0, 250);
    }
  
    return fileName;
  }
  
  public int deleteAttachmentList(List<AttachmentReference> attachmentRefList) {
    int nrDeleted = 0;
    if (attachmentRefList != null) {
      nrDeleted = deleteAttachmentMap(buildAttachmentsToDeleteMap(attachmentRefList));
    }
    return nrDeleted;
  }

  Map<DocumentReference, List<String>> buildAttachmentsToDeleteMap(
      List<AttachmentReference> attachmentRefList) {
    Map<DocumentReference, List<String>> attachmentMap = 
        new HashMap<DocumentReference, List<String>>();
    for (AttachmentReference attRef : attachmentRefList) {
      DocumentReference docRef = attRef.getDocumentReference();
      if (attachmentMap.containsKey(docRef)) {
        List<String> attList = attachmentMap.get(docRef);
        attList.add(attRef.getName());
        attachmentMap.put(docRef, attList);
      } else {
        List<String> attList = new ArrayList<String>();
        attList.add(attRef.getName());
        attachmentMap.put(docRef, attList);
      }
    }
    return attachmentMap;
  }
  
  int deleteAttachmentMap(Map<DocumentReference, List<String>> attachmentMap) {
    int nrDeleted = 0;
    for (DocumentReference docRef : attachmentMap.keySet()) {
      int nrDeletedOnDoc = 0;
      try {
        XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
        //Analogue to class DeleteAttachmentAction
        String versionCommentList = "";
        for (String filename : attachmentMap.get(docRef)) {
            XWikiAttachment attachment = doc.getAttachment(filename);
            if (attachment != null) {
              versionCommentList += ", " + filename;
              doc.deleteAttachment(attachment, getContext());
              nrDeletedOnDoc++;
            }
        }
        if (nrDeletedOnDoc > 0) {
          doc.setAuthor(getContext().getUser());
          // Set "deleted attachment" as the version comment.
          doc.setComment(getContext().getMessageTool().get("core.comment." +
              "deleteAttachmentComment", Arrays.asList(versionCommentList.substring(2))));
          // Needed to counter a side effect of XWIKI-1982: the attachment is deleted from
          // the newdoc.originalDoc as well
          doc.setOriginalDocument(doc);
          // Also save the document and attachment metadata
          getContext().getWiki().saveDocument(doc, getContext());
        }
        nrDeleted += nrDeletedOnDoc;
      } catch (XWikiException xwe) {
        _LOGGER.error("Exception deleting Attachments on doch " + docRef, xwe);
      }
    }
    return nrDeleted;
  }

}
