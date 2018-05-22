package com.celements.web.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.ClassDefinition;
import com.celements.rendering.RenderCommand;
import com.celements.web.UserCreateException;
import com.celements.web.UserService;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.celements.web.plugin.cmd.PossibleLoginsCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.util.Util;
import com.xpn.xwiki.web.XWikiResponse;

@Component
public class CelementsWebService implements ICelementsWebServiceRole {

  private static Logger _LOGGER = LoggerFactory.getLogger(CelementsWebService.class);

  private List<String> supportedAdminLangList;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private UserService userService;

  @Requirement(XWikiRightsClass.CLASS_DEF_HINT)
  private ClassDefinition xWikiRightsClass;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Deprecated
  public String getEmailAdressForUser(String username) {
    return getEmailAdressForUser(webUtilsService.resolveDocumentReference(username));
  }

  @Override
  @Deprecated
  public String getEmailAdressForUser(DocumentReference userDocRef) {
    return userService.getUserEmail(userDocRef).orNull();
  }

  @Override
  @Deprecated
  public int createUser(boolean validate) throws XWikiException {
    String possibleLogins = new PossibleLoginsCommand().getPossibleLogins();
    return createUser(getUniqueNameValueRequestMap(), possibleLogins, validate);
  }

  @Override
  @Deprecated
  public synchronized int createUser(Map<String, String> userData, String possibleLogins,
      boolean validate) throws XWikiException {
    try {
      if (createNewUser(userData, possibleLogins, validate) != null) {
        return 1;
      }
    } catch (UserCreateException uce) {
      if (uce.getCause() instanceof XWikiException) {
        throw (XWikiException) uce.getCause();
      } else {
        throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
            XWikiException.ERROR_XWIKI_UNKNOWN, "failed to create user", uce);
      }
    }
    return -1;
  }

  @Override
  @Deprecated
  public synchronized @NotNull XWikiUser createNewUser(@NotNull Map<String, String> userData,
      @NotNull String possibleLogins, boolean validate) throws UserCreateException {
    // TODO delegate
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, String> getUniqueNameValueRequestMap() {
    Map<String, String[]> params = getContext().getRequest().getParameterMap();
    Map<String, String> resultMap = new HashMap<>();
    for (String key : params.keySet()) {
      if ((params.get(key) != null) && (params.get(key).length > 0)) {
        resultMap.put(key, params.get(key)[0]);
      } else {
        resultMap.put(key, "");
      }
    }
    return resultMap;
  }

  @Override
  public List<String> getSupportedAdminLanguages() {
    if (supportedAdminLangList == null) {
      supportedAdminLangList = Arrays.asList(new String[] { "de", "fr", "en", "it" });
    }
    return supportedAdminLangList;
  }

  @Override
  public boolean writeUTF8Response(String filename, String renderDocFullName) {
    boolean success = false;
    if (getContext().getWiki().exists(webUtilsService.resolveDocumentReference(renderDocFullName),
        getContext())) {
      XWikiDocument renderDoc;
      try {
        renderDoc = getContext().getWiki().getDocument(webUtilsService.resolveDocumentReference(
            renderDocFullName), getContext());
        adjustResponseHeader(filename, getContext().getResponse());
        setResponseContent(renderDoc, getContext().getResponse());
      } catch (XWikiException e) {
        _LOGGER.error("", e);
      }
      getContext().setFinished(true);
    }
    return success;
  }

  private void adjustResponseHeader(String filename, XWikiResponse response) {
    response.setContentType("text/plain");
    String ofilename = Util.encodeURI(filename, getContext()).replaceAll("\\+", " ");
    response.addHeader("Content-disposition", "attachment; filename=\"" + ofilename
        + "\"; charset='UTF-8'");
  }

  private void setResponseContent(XWikiDocument renderDoc, XWikiResponse response)
      throws XWikiException {
    String renderedContent = new RenderCommand().renderDocument(renderDoc);
    byte[] data = {};
    try {
      data = renderedContent.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    }
    response.setContentLength(data.length + 3);
    try {
      response.getOutputStream().write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
      response.getOutputStream().write(data);
    } catch (IOException e) {
      throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
          XWikiException.ERROR_XWIKI_APP_SEND_RESPONSE_EXCEPTION,
          "Exception while sending response", e);
    }
  }

  @Override
  public void setSupportedAdminLanguages(List<String> supportedAdminLangList) {
    this.supportedAdminLangList = supportedAdminLangList;
  }

  @Override
  public String encodeUrlToUtf8(String urlStr) {
    String pattern = "://";
    int findIndex = urlStr.indexOf(pattern);
    if (findIndex > 0) {
      String urlPrefix = urlStr.substring(0, findIndex + pattern.length());
      String mainUrl = urlStr.substring(findIndex + pattern.length());
      try {
        urlStr = URLEncoder.encode(mainUrl, "UTF-8");
      } catch (UnsupportedEncodingException exp) {
        _LOGGER.error("Failed to encode url [" + urlStr + "] to utf-8", exp);
      }
      urlStr = urlPrefix + urlStr.replaceAll("%2F", "/");
    }
    return urlStr;
  }

  @Override
  public void sendRedirect(String urlStr) {
    try {
      getContext().getResponse().sendRedirect(encodeUrlToUtf8(urlStr));
    } catch (IOException exp) {
      _LOGGER.error("Failed to redirect to url [" + urlStr + "]", exp);
    }
  }

}
