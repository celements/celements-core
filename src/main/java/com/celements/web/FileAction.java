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
 *
 */
package com.celements.web;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.util.Util;
import com.xpn.xwiki.web.SkinAction;
import com.xpn.xwiki.web.XWikiResponse;

/**
 * <p>
 * Action for serving skin files. It allows skins to be defined using XDocuments
 * as skins, by letting files be placed as text fields in an XWiki.XWikiSkins
 * object, or as attachments to the document, or as a file in the filesystem. If
 * the file is not found in the current skin, then it is searched in its base
 * skin, and eventually in the default base skins,
 * </p>
 * <p>
 * This action indicates that the results should be publicly cacheable for 30
 * days.
 * </p>
 * 
 * @version $Id: FileAction.java 28858 2010-05-13 20:43:41Z sdumitriu $
 * @since 1.0
 */
public class FileAction extends SkinAction {
  /** Logging helper. */
  private static final Log mLogger = LogFactory.getLog(FileAction.class);

  /** Path delimiter. */
  public static final String DELIMITER = "/";

  /** The directory where the skins are placed in the webapp. */
  private static final String SKINS_DIRECTORY = "skins";

  /** The directory where resources are placed in the webapp. */
  public static final String RESOURCES_DIRECTORY = "resources";

  /**
   * The encoding to use when reading text resources from the filesystem and
   * when sending css/javascript responses.
   */
  private static final String ENCODING = "UTF-8";

  @Override
  public String render(String path, XWikiContext context) throws XWikiException,
      IOException {
    XWiki xwiki = context.getWiki();
    // Since skin paths usually contain the name of skin document, it is likely
    // that the context document belongs to
    // the current skin.
    XWikiDocument doc = context.getDoc();

    // The base skin could be either a filesystem directory, or an xdocument.
    String baseskin = xwiki.getBaseSkin(context, true);
    XWikiDocument baseskindoc = xwiki.getDocument(baseskin, context);
    // The default base skin is always a filesystem directory.
    String defaultbaseskin = xwiki.getDefaultBaseSkin(context);

    if (mLogger.isDebugEnabled()) {
      mLogger.debug("document: " + doc.getFullName() + " ; baseskin: " + baseskin
          + " ; defaultbaseskin: " + defaultbaseskin);
    }

    // Since we don't know exactly what does the URL point at, meaning that we
    // don't know where the skin identifier
    // ends and where the path to the file starts, we must try to split at every
    // '/' character.
    int idx = path.lastIndexOf(DELIMITER);
    boolean found = false;
    while (idx > 0) {
      try {
        String filename = Util.decodeURI(path.substring(idx + 1), context);
        if (mLogger.isDebugEnabled()) {
          mLogger.debug("Trying '" + filename + "'");
        }

        // Try on the current skin document.
        if (renderSkin(filename, doc, context)) {
          found = true;
          break;
        }

        // Try on the base skin document, if it is not the same as above.
        if (!doc.getName().equals(baseskin)) {
          if (renderSkin(filename, baseskindoc, context)) {
            found = true;
            break;
          }
        }

        // Try on the default base skin, if it wasn't already tested above.
        if (!(doc.getName().equals(defaultbaseskin) || baseskin.equals(defaultbaseskin))) {
          // defaultbaseskin can only be on the filesystem, so don't try to use
          // it as a
          // skin document.
          if (renderFileFromFilesystem(getSkinFilePath(filename, defaultbaseskin),
              context)) {
            found = true;
            break;
          }
        }

        // Try in the resources directory.
        if (renderFileFromFilesystem(getResourceFilePath(filename), context)) {
          found = true;
          break;
        }
      } catch (XWikiException ex) {
        if (ex.getCode() == XWikiException.ERROR_XWIKI_APP_SEND_RESPONSE_EXCEPTION) {
          // This means that the response couldn't be sent, although the file
          // was
          // successfully found. Signal this further, and stop trying to render.
          throw ex;
        }
        mLogger.debug(new Integer(idx), ex);
      }
      idx = path.lastIndexOf(DELIMITER, idx - 1);
    }
    if (!found) {
      context.getResponse().setStatus(404);
      return "docdoesnotexist";
    }
    return null;
  }

  /**
   * Tries to serve a skin file using <tt>doc</tt> as a skin document. The file
   * is searched in the following places:
   * <ol>
   * <li>As the content of a property with the same name as the requested
   * filename, from an XWikiSkins object attached to the document.</li>
   * <li>As the content of an attachment with the same name as the requested
   * filename.</li>
   * <li>As a file located on the filesystem, in the directory with the same
   * name as the current document (in case the URL was actually pointing to
   * <tt>/skins/directory/file</tt>).</li>
   * </ol>
   * 
   * @param filename
   *          The name of the skin file that should be rendered.
   * @param doc
   *          The skin {@link XWikiDocument document}.
   * @param context
   *          The current {@link XWikiContext request context}.
   * @return <tt>true</tt> if the attachment was found and the content was
   *         successfully sent.
   * @throws XWikiException
   *           If the attachment cannot be loaded.
   * @throws IOException
   *           if the filename is invalid
   */
  private boolean renderSkin(String filename, XWikiDocument doc, XWikiContext context)
      throws XWikiException, IOException {
    if (mLogger.isDebugEnabled()) {
      mLogger.debug("Rendering file '" + filename + "' within the '" + doc.getFullName()
          + "' document");
    }
    try {
      if (doc.isNew()) {
        if (mLogger.isDebugEnabled()) {
          mLogger.debug(doc.getName() + " is not a document");
        }
      } else {
        return renderFileFromObjectField(filename, doc, context)
            || renderFileFromAttachment(filename, doc, context)
            || (SKINS_DIRECTORY.equals(doc.getSpace()) && renderFileFromFilesystem(
                getSkinFilePath(filename, doc.getName()), context));
      }
    } catch (IOException e) {
      throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
          XWikiException.ERROR_XWIKI_APP_SEND_RESPONSE_EXCEPTION,
          "Exception while sending response:", e);
    }

    return renderFileFromFilesystem(getSkinFilePath(filename, doc.getName()), context);
  }

  /**
   * Tries to serve a file from the filesystem.
   * 
   * @param path
   *          Path of the file that should be rendered.
   * @param context
   *          The current {@link XWikiContext request context}.
   * @return <tt>true</tt> if the file was found and its content was
   *         successfully sent.
   * @throws XWikiException
   *           If the response cannot be sent.
   */
  private boolean renderFileFromFilesystem(String path, XWikiContext context)
      throws XWikiException {
    if (mLogger.isDebugEnabled()) {
      mLogger.debug("Rendering filesystem file from path [" + path + "]");
    }
    XWikiResponse response = context.getResponse();
    try {
      byte[] data = context.getWiki().getResourceContentAsBytes(path);
      if (data != null && data.length > 0) {
        String filename = path.substring(path.lastIndexOf("/") + 1, path.length());
        String mimetype = context.getEngineContext().getMimeType(filename.toLowerCase());
        Date modified = context.getWiki().getResourceLastModificationDate(path);
        if (isCssMimeType(mimetype) || isJavascriptMimeType(mimetype)) {
          response.setCharacterEncoding(ENCODING);
        }
        setupHeaders(response, mimetype, modified, data.length);
        try {
          response.getOutputStream().write(data);
        } catch (IOException e) {
          throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
              XWikiException.ERROR_XWIKI_APP_SEND_RESPONSE_EXCEPTION,
              "Exception while sending response", e);
        }
        return true;
      }
    } catch (IOException ex) {
      mLogger.info("Skin file '" + path + "' does not exist or cannot be accessed");
    }
    return false;
  }

  /**
   * Tries to serve the content of an XWikiSkins object field as a skin file.
   * 
   * @param filename
   *          The name of the skin file that should be rendered.
   * @param doc
   *          The skin {@link XWikiDocument document}.
   * @param context
   *          The current {@link XWikiContext request context}.
   * @return <tt>true</tt> if the object exists, and the field is set to a
   *         non-empty value, and its content was successfully sent.
   * @throws IOException
   *           If the response cannot be sent.
   */
  @Override
  public boolean renderFileFromObjectField(String filename, XWikiDocument doc,
      XWikiContext context) throws IOException {
    mLogger.debug("... as object property");
    BaseObject object = doc.getObject("XWiki.XWikiSkins");
    String content = null;
    if (object != null) {
      content = object.getStringValue(filename);
    }

    if (!StringUtils.isBlank(content)) {
      XWiki xwiki = context.getWiki();
      XWikiResponse response = context.getResponse();
      String mimetype = xwiki.getEngineContext().getMimeType(filename.toLowerCase());
      // Since object fields are read as unicode strings, the result does not
      // depend on the wiki encoding. Force
      // the output to UTF-8.
      response.setCharacterEncoding(ENCODING);
      byte[] data = content.getBytes(ENCODING);
      setupHeaders(response, mimetype, doc.getDate(), data.length);
      response.getOutputStream().write(data);
      return true;
    } else {
      mLogger.debug("Object field not found or empty");
    }
    return false;
  }

  /**
   * Tries to serve the content of an attachment as a skin file.
   * 
   * @param filename
   *          The name of the skin file that should be rendered.
   * @param doc
   *          The skin {@link XWikiDocument document}.
   * @param context
   *          The current {@link XWikiContext request context}.
   * @return <tt>true</tt> if the attachment was found and its content was
   *         successfully sent.
   * @throws IOException
   *           If the response cannot be sent.
   * @throws XWikiException
   *           If the attachment cannot be loaded.
   */
  @Override
  public boolean renderFileFromAttachment(String filename, XWikiDocument doc,
      XWikiContext context) throws IOException, XWikiException {
    mLogger.debug("... as attachment");
    XWikiAttachment attachment = doc.getAttachment(filename);
    if (attachment != null) {
      XWiki xwiki = context.getWiki();
      XWikiResponse response = context.getResponse();
      String mimetype = xwiki.getEngineContext().getMimeType(filename.toLowerCase());
      if (isCssMimeType(mimetype) || isJavascriptMimeType(mimetype)) {
        byte[] data = attachment.getContent(context);
        // Always force UTF-8, as this is the assumed encoding for text files.
        response.setCharacterEncoding(ENCODING);
        setupHeaders(response, mimetype, attachment.getDate(), data.length);
        response.getOutputStream().write(data);
      } else {
        setupHeaders(response, mimetype, attachment.getDate(), attachment.getContentSize(
            context));
        IOUtils.copy(attachment.getContentInputStream(context), response.getOutputStream()
            );
      }
      return true;
    } else {
      mLogger.debug("Attachment not found");
    }
    return false;
  }

}
