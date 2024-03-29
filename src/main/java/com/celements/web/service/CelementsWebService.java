package com.celements.web.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.auth.user.User;
import com.celements.auth.user.UserCreateException;
import com.celements.auth.user.UserInstantiationException;
import com.celements.auth.user.UserService;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.rendering.RenderCommand;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.util.Util;
import com.xpn.xwiki.web.XWikiRequest;
import com.xpn.xwiki.web.XWikiResponse;

import one.util.streamex.EntryStream;

@Component
public class CelementsWebService implements ICelementsWebServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(CelementsWebService.class);

  private List<String> supportedAdminLangList;

  @Requirement
  private UserService userService;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  @Deprecated
  private XWikiContext getXWikiContext() {
    return context.getXWikiContext();
  }

  @Deprecated
  public String getEmailAdressForUser(String username) {
    return getEmailAdressForUser(userService.resolveUserDocRef(username));
  }

  @Override
  @Deprecated
  public String getEmailAdressForUser(DocumentReference userDocRef) {
    try {
      User user = userService.getUser(userDocRef);
      return user.getEmail().orNull();
    } catch (UserInstantiationException exc) {
      LOGGER.info("getEmailAdressForUser - invalid user", exc);
      return null;
    }
  }

  @Override
  @Deprecated
  public int createUser(boolean validate) throws XWikiException {
    return createUser(getUniqueNameValueRequestMap(), null, validate);
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
    return userService.createNewUser(userData, validate).asXWikiUser();
  }

  @Override
  public Map<String, String> getUniqueNameValueRequestMap() {
    return EntryStream.of(context.request()
        .map(XWikiRequest::getParameterMap)
        .orElse(ImmutableMap.of()))
        .mapValues(values -> Stream.of(values).findFirst().orElse(""))
        .toMap();
  }

  @Override
  public List<String> getSupportedAdminLanguages() {
    if (supportedAdminLangList == null) {
      supportedAdminLangList = ImmutableList.of("de", "fr", "en", "it");
    }
    return supportedAdminLangList;
  }

  @Override
  public boolean writeUTF8Response(String filename, String renderDocFullName) {
    boolean success = false;
    try {
      XWikiDocument renderDoc = modelAccess.getDocument(modelUtils.resolveRef(renderDocFullName,
          DocumentReference.class));
      adjustResponseHeader(filename, context.getResponse().get());
      setResponseContent(renderDoc, context.getResponse().get());
      success = true;
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("writeUTF8Response - failed for '{}'", renderDocFullName, exc);
    } catch (XWikiException exc) {
      LOGGER.error("writeUTF8Response - failed for '{}'", renderDocFullName, exc);
    }
    getXWikiContext().setFinished(true);
    return success;
  }

  private void adjustResponseHeader(String filename, XWikiResponse response) {
    response.setContentType("text/plain");
    String ofilename = Util.encodeURI(filename, getXWikiContext()).replaceAll("\\+", " ");
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

  @Deprecated
  @Override
  public String encodeUrlToUtf8(String urlStr) {
    try {
      return encodeUrlForRedirect(urlStr);
    } catch (MalformedURLException exc) {
      LOGGER.error("Failed to encode url [{}] to utf-8", urlStr, exc);
      return urlStr;
    }
  }

  @Override
  public void sendRedirect(String urlStr) {
    try {
      context.getResponse().get().sendRedirect(encodeUrlForRedirect(urlStr));
    } catch (IOException exp) {
      LOGGER.error("Failed to redirect to url [" + urlStr + "]", exp);
    }
  }

  String encodeUrlForRedirect(String urlStr) throws MalformedURLException {
    try {
      URL url = new URL(urlStr);
      UriBuilder builder = UriBuilder.fromUri(url.toURI());
      builder.host(url.getHost());
      builder.port(url.getPort());
      builder.replacePath(url.getPath());
      String encodedUrl = builder.build().toString();
      LOGGER.info("encodeUrlForRedirect - [{}] from [{}]", encodedUrl, urlStr);
      return encodedUrl;
    } catch (URISyntaxException exc) {
      throw new MalformedURLException(exc.getMessage());
    }
  }

}
