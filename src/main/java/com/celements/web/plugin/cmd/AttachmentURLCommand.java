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

import static com.celements.common.MoreOptional.*;
import static com.celements.execution.XWikiExecutionProp.*;
import static com.google.common.base.Strings.*;

import java.net.MalformedURLException;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.filebase.IAttachmentServiceRole;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.util.ModelUtils;
import com.celements.web.service.LastStartupTimeStampRole;
import com.celements.web.service.UrlService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

@Component
public class AttachmentURLCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentURLCommand.class);

  public String getAttachmentURL(String link, XWikiContext context) {
    return getAttachmentURL(link, getDefaultAction(), context);
  }

  protected String getDefaultAction() {
    return getContext().getWiki().getXWikiPreference("celdefaultAttAction",
        "celements.attachmenturl.defaultaction", "file", getContext());
  }

  public String getAttachmentURLPrefix() {
    return getAttachmentURLPrefix(getDefaultAction());
  }

  public String getAttachmentURLPrefix(String action) {
    XWikiURLFactory urlf = getContext().getURLFactory();
    return urlf.createResourceURL("", true, getContext()).toString().replace("/skin/", "/" + action
        + "/");
  }

  /**
   * @deprecated instead use {@link #getAttachmentURL(String, String, String)}
   */
  @Deprecated(since = "6.2")
  public String getAttachmentURL(String link, String action, XWikiContext context) {
    return getAttachmentURL(link, action, "")
        .map(UriComponents::toUriString)
        .orElse(null);
  }

  public Optional<UriComponents> getAttachmentURL(String link, String action, String queryString) {
    String url = link;
    Supplier<String> versionProvider = null;
    action = asNonBlank(action).orElseGet(this::getDefaultAction);
    if (isAttachmentLink(link)) {
      try {
        var attRef = asAttachmentRef(link);
        XWikiAttachment att = getAttachmentService().getAttachmentNameEqual(attRef);
        WikiReference attWiki = attRef.getDocumentReference().getWikiReference();
        url = getEContext().get(WIKI).map(attWiki::equals).orElse(true)
            ? getUrlService().getURL(attRef, action)
            : getUrlService().getExternalURL(attRef, action);
        versionProvider = () -> getLastStartupTimeStamp().getLastChangedTimeStamp(att.getDate());
      } catch (DocumentNotExistsException | AttachmentNotExistsException anee) {
        LOGGER.info("Attachment not found for link [{}] and action [{}]", link, action, anee);
        return Optional.empty();
      }
    } else if (isOnDiskLink(link)) {
      String path = link.trim().substring(1);
      url = getContext().getWiki().getSkinFile(path, true, getContext())
          .replace("/skin/", "/" + action + "/");
      versionProvider = () -> getLastStartupTimeStamp().getFileModificationDate(path);
    }
    if (url.startsWith("?")) {
      url = getUrlService().getURL(getContext().getDoc().getDocRef(), "view") + url;
    }
    try {
      UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
      builder = builder.query(nullToEmpty(queryString)); // null clears the query string
      if ((versionProvider != null) && !builder.build().getQueryParams().containsKey("version")) {
        builder = builder.queryParam("version", versionProvider.get());
      }
      return Optional.of(builder.build());
    } catch (IllegalArgumentException iae) {
      LOGGER.error("Failed building URI for link [{}] and action [{}]", link, action, iae);
      return Optional.empty();
    }
  }

  private LastStartupTimeStampRole getLastStartupTimeStamp() {
    return Utils.getComponent(LastStartupTimeStampRole.class);
  }

  public String getAttachmentName(String link) {
    return link.split(";")[1];
  }

  public String getPageFullName(String link) {
    return link.split(";")[0];
  }

  public AttachmentReference asAttachmentRef(String link) {
    var split = link.split(";");
    DocumentReference docRef = getModelUtils().resolveRef(split[0], DocumentReference.class);
    return new AttachmentReference(split[1], docRef);
  }

  public boolean isAttachmentLink(String link) {
    boolean isAttachmentLink = false;
    if (link != null) {
      String regex = "([\\w\\-]*:)?([\\w\\-]*\\.[\\w\\-]*){1};.*";
      isAttachmentLink = link.matches(regex);
    }
    return isAttachmentLink;
  }

  public boolean isOnDiskLink(String link) {
    boolean isAttachmentLink = false;
    if (link != null) {
      String regex = "^:[/\\w\\-\\.]*";
      isAttachmentLink = link.trim().matches(regex);
    }
    return isAttachmentLink;
  }

  public String getExternalAttachmentURL(String fileName, String action, XWikiContext context) {
    try {
      return context.getURLFactory().getServerURL(context).toExternalForm() + getAttachmentURL(
          fileName, action, context);
    } catch (MalformedURLException exp) {
      LOGGER.error("Failed to getServerURL.", exp);
    }
    return "";
  }

  private IAttachmentServiceRole getAttachmentService() {
    return Utils.getComponent(IAttachmentServiceRole.class);
  }

  private UrlService getUrlService() {
    return Utils.getComponent(UrlService.class);
  }

  private ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

  private XWikiContext getContext() {
    return getEContext().get(XWIKI_CONTEXT).orElseThrow();
  }

  private ExecutionContext getEContext() {
    return Utils.getComponent(Execution.class).getContext();
  }

}
