package com.celements.filebase.uri;

import java.net.MalformedURLException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.filebase.IAttachmentServiceRole;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.AttachmentNotExistsException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.web.service.LastStartupTimeStampRole;
import com.google.common.base.Suppliers;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.XWikiURLFactory;

public class FileUriService implements FileUriServiceRole {

  private static final String ATTACHMENT_LINK_REGEX = "([\\w\\-]*:)?([\\w\\-]*\\.[\\w\\-]*){1};.*";
  private static final Supplier<Pattern> ATTACHMENT_LINK_PATTERN = Suppliers
      .memoize(() -> Pattern.compile(ATTACHMENT_LINK_REGEX));
  private static final String ON_DISK_LINK_REGEX = "^:[/\\w\\-\\.]*";
  private static final Supplier<Pattern> ON_DISK_LINK_PATTERN = Suppliers
      .memoize(() -> Pattern.compile(ON_DISK_LINK_REGEX));

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUriService.class);

  @Requirement
  private ModelContext context;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private IAttachmentServiceRole attachmentSrv;

  @Requirement
  private LastStartupTimeStampRole lastStartupTimeStamp;

  @Override
  @NotNull
  public String getAttachmentName(@NotNull String link) {
    return link.split(";")[1];
  }

  @Override
  @NotNull
  public DocumentReference getPageDocRef(@NotNull String link) {
    return modelUtils.resolveRef(link.split(";")[0], DocumentReference.class);
  }

  @Override
  public boolean isAttachmentLink(String link) {
    if (link != null) {
      return ATTACHMENT_LINK_PATTERN.get().matcher(link.trim()).matches();
    }
    return false;
  }

  @Override
  public boolean isOnDiskLink(@Nullable String link) {
    if (link != null) {
      return ON_DISK_LINK_PATTERN.get().matcher(link.trim()).matches();
    }
    return false;
  }

  private String getAction(Optional<String> action) {
    if (action.isPresent()) {
      return action.get();
    }
    return getDefaultAction();
  }

  private String getDefaultAction() {
    return context.getXWikiContext().getWiki().getXWikiPreference("celdefaultAttAction",
        "celements.attachmenturl.defaultaction", "file", context.getXWikiContext());
  }

  @Override
  @NotEmpty
  public String getRessourceURLPrefix() {
    return getRessourceURLPrefix(getDefaultAction());
  }

  @Override
  @NotEmpty
  public String getRessourceURLPrefix(@NotEmpty String action) {
    XWikiURLFactory urlf = context.getXWikiContext().getURLFactory();
    return urlf.createResourceURL("", true, context.getXWikiContext()).toString().replace("/skin/",
        "/" + action + "/");
  }

  @Override
  @NotNull
  public String getExternalRessourceURL(@NotNull String fileName,
      @NotNull Optional<String> action) {
    try {
      return context.getXWikiContext().getURLFactory().getServerURL(context.getXWikiContext())
          .toExternalForm() + createRessourceUrl(fileName, action);
    } catch (MalformedURLException | FileNotExistException exp) {
      LOGGER.error("Failed to getServerURL.", exp);
    }
    return "";
  }

  @Override
  @NotNull
  public String createRessourceUrl(@NotNull String link, @NotNull Optional<String> action,
      @NotNull Optional<String> queryString) throws FileNotExistException {
    final String baseUrl = createRessourceUrl(link, action);
    if (queryString.isPresent()) {
      if (baseUrl.indexOf("?") > -1) {
        return baseUrl + "&" + queryString.get();
      } else {
        return baseUrl + "?" + queryString.get();
      }
    }
    return baseUrl;
  }

  @Override
  public @NotEmpty String createRessourceUrl(@NotNull String link, @NotNull Optional<String> action)
      throws FileNotExistException {
    String url = link;
    if (isAttachmentLink(link)) {
      url = createAttachmentUrl(link, action);
    } else if (isOnDiskLink(link)) {
      url = createOnDiskUrl(link, action);
    }
    return addContextUrl(url);
  }

  private String addContextUrl(String url) {
    Optional<XWikiDocument> currentDoc = context.getCurrentDoc().toJavaUtil();
    if (currentDoc.isPresent() && url.startsWith("?")) {
      url = currentDoc.get().getURL("view", context.getXWikiContext()) + url;
    }
    return url;
  }

  private String createAttachmentUrl(String link, Optional<String> action)
      throws FileNotExistException {
    String attName = getAttachmentName(link);
    try {
      XWikiDocument doc = modelAccess.getDocument(getPageDocRef(link));
      XWikiAttachment att = attachmentSrv.getAttachmentNameEqual(doc, attName);
      return doc.getAttachmentURL(attName, getAction(action), context.getXWikiContext())
          + "?version=" + lastStartupTimeStamp.getLastChangedTimeStamp(att.getDate());
    } catch (DocumentNotExistsException exp) {
      LOGGER.error("Error getting attachment URL for doc '{}' and file {}", getPageDocRef(link),
          attName, exp);
      throw new FileNotExistException(link);
    } catch (AttachmentNotExistsException anee) {
      LOGGER.info("Attachment not found for link [{}] and action [{}]", link, action, anee);
      throw new FileNotExistException(link);
    }
  }

  private String createOnDiskUrl(String link, Optional<String> action) {
    String url;
    String path = link.trim().substring(1);
    url = context.getXWikiContext().getWiki().getSkinFile(path, true, context.getXWikiContext())
        .replace("/skin/",
            "/" + getAction(action) + "/");
    url += "?version=" + lastStartupTimeStamp.getFileModificationDate(path);
    return url;
  }

}
