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

import java.net.MalformedURLException;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.filebase.IAttachmentServiceRole;
import com.celements.filebase.uri.FileUriServiceRole;
import com.celements.filebase.uri.FileNotExistException;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.web.service.LastStartupTimeStampRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

/**
 * @deprecated since 5.4 instead use {@link FileUriServiceRole}
 */
@Deprecated
public class AttachmentURLCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentURLCommand.class);

  /**
   * @deprecated since 5.4 instead use
   *             {@link FileUriServiceRole#createRessourceUrl(String, Optional)}
   */
  @Deprecated
  public String getAttachmentURL(String link, XWikiContext context) {
    return getAttachmentURL(link, getDefaultAction(), context);
  }

  private String getDefaultAction() {
    return getContext().getWiki().getXWikiPreference("celdefaultAttAction",
        "celements.attachmenturl.defaultaction", "file", getContext());
  }

  /**
   * @deprecated since 5.4 instead use {@link FileUriServiceRole#getRessourceURLPrefix()}
   */
  @Deprecated
  public String getAttachmentURLPrefix() {
    return getAttachmentURLPrefix(getDefaultAction());
  }

  /**
   * @deprecated since 5.4 instead use {@link FileUriServiceRole#getRessourceURLPrefix(String)}
   */
  @Deprecated
  public String getAttachmentURLPrefix(String action) {
    XWikiURLFactory urlf = getContext().getURLFactory();
    return urlf.createResourceURL("", true, getContext()).toString().replace("/skin/", "/" + action
        + "/");
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link RessourceUrlServiceRole#createRessourceUrl(String, Optional<String>)}
   */
  @Deprecated
  @Nullable
  public String getAttachmentURL(String link, String action, XWikiContext context) {
    try {
      return getAttachmentURL(link, Optional.ofNullable(action));
    } catch (FileNotExistException exp) {
      return null;
    }
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link RessourceUrlServiceRole#createRessourceUrl(String, Optional<String>)}
   */
  @Deprecated
  @NotEmpty
  public String getAttachmentURL(@NotNull String link, @NotNull Optional<String> action)
      throws FileNotExistException {
    String url = link;
    if (isAttachmentLink(link)) {
      String attName = getAttachmentName(link);
      try {
        XWikiDocument doc = getModelAccess().getDocument(getPageDocRef(link));
        XWikiAttachment att = getAttachmentService().getAttachmentNameEqual(doc, attName);
        url = doc.getAttachmentURL(attName, getAction(action), getContext());
        url += "?version=" + getLastStartupTimeStamp().getLastChangedTimeStamp(att.getDate());
      } catch (DocumentNotExistsException exp) {
        LOGGER.error("Error getting attachment URL for doc '{}' and file {}", getPageFullName(link),
            attName, exp);
        // 01.02.2022;F.Pichler; is this functionality used? No test available. Adding exception.
        // url = link;
        throw new FileNotExistException(link);
      } catch (AttachmentNotExistsException anee) {
        LOGGER.info("Attachment not found for link [{}] and action [{}]", link, action, anee);
        throw new FileNotExistException(link);
      }
    } else if (isOnDiskLink(link)) {
      String path = link.trim().substring(1);
      url = getContext().getWiki().getSkinFile(path, true, getContext()).replace("/skin/",
          "/" + getAction(action) + "/");
      url += "?version=" + getLastStartupTimeStamp().getFileModificationDate(path);
    }
    Optional<XWikiDocument> currentDoc = getModelContext().getCurrentDoc().toJavaUtil();
    if (currentDoc.isPresent() && url.startsWith("?")) {
      url = currentDoc.get().getURL("view", getContext()) + url;
    }
    return url;
  }

  private String getAction(Optional<String> action) {
    if (action.isPresent()) {
      return action.get();
    }
    return getDefaultAction();
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link RessourceUrlServiceRole#createRessourceUrl(String, Optional<String>,
   *             Optional<String>)}
   */
  @Deprecated
  @Nullable
  public String getAttachmentURL(@NotNull String link, @NotNull Optional<String> action,
      @NotNull Optional<String> queryString) throws FileNotExistException {
    String attUrl = getAttachmentURL(link, action);
    if (queryString.isPresent()) {
      if (attUrl.indexOf("?") > -1) {
        attUrl += "&" + queryString.get();
      } else {
        attUrl += "?" + queryString.get();
      }
    }
    return attUrl;
  }

  private LastStartupTimeStampRole getLastStartupTimeStamp() {
    return Utils.getComponent(LastStartupTimeStampRole.class);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link FileUriServiceRole#getAttachmentName(String)}
   */
  @Deprecated
  public String getAttachmentName(String link) {
    return link.split(";")[1];
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link FileUriServiceRole#getPageDocRef(String)}
   */
  @Deprecated
  public String getPageFullName(String link) {
    return link.split(";")[0];
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link FileUriServiceRole#getPageDocRef(String)}
   */
  @Deprecated
  public DocumentReference getPageDocRef(String link) {
    return getModelUtils().resolveRef(getPageFullName(link), DocumentReference.class);
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link FileUriServiceRole#isAttachmentLink(String)}
   */
  @Deprecated
  public boolean isAttachmentLink(String link) {
    boolean isAttachmentLink = false;
    if (link != null) {
      String regex = "([\\w\\-]*:)?([\\w\\-]*\\.[\\w\\-]*){1};.*";
      isAttachmentLink = link.matches(regex);
    }
    return isAttachmentLink;
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link FileUriServiceRole#isOnDiskLink(String)}
   */
  @Deprecated
  public boolean isOnDiskLink(String link) {
    boolean isAttachmentLink = false;
    if (link != null) {
      String regex = "^:[/\\w\\-\\.]*";
      isAttachmentLink = link.trim().matches(regex);
    }
    return isAttachmentLink;
  }

  /**
   * @deprecated since 5.4 instead use {@link RessourceUrlServiceRole#createRessourceUrl(String,
   *             Optional<String>)}
   */
  @Deprecated
  public String getExternalAttachmentURL(String fileName, String action, XWikiContext context) {
    return getExternalAttachmentURL(fileName, Optional.ofNullable(action));
  }

  /**
   * @deprecated since 5.4 instead use
   *             {@link RessourceUrlServiceRole#getExternalRessourceURL(String, Optional<String>)}
   */
  @Deprecated
  public String getExternalAttachmentURL(String fileName, Optional<String> action) {
    try {
      return getContext().getURLFactory().getServerURL(getContext()).toExternalForm()
          + getAttachmentURL(fileName, action);
    } catch (MalformedURLException | FileNotExistException exp) {
      LOGGER.error("Failed to getServerURL.", exp);
    }
    return "";
  }

  private IAttachmentServiceRole getAttachmentService() {
    return Utils.getComponent(IAttachmentServiceRole.class);
  }

  private XWikiContext getContext() {
    return getModelContext().getXWikiContext();
  }

  private @NotNull ModelContext getModelContext() {
    return Utils.getComponent(ModelContext.class);
  }

  private @NotNull IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  private @NotNull ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
