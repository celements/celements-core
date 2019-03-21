package com.celements.auth;

import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Strings.*;

import org.xwiki.model.reference.DocumentReference;

import com.celements.auth.classes.RemoteLoginClass;
import com.celements.convert.bean.XDocBeanLoader;
import com.celements.convert.bean.XObjectBeanConverter;

/**
 * JavaBean for {@link RemoteLoginClass}.
 */
public class RemoteLogin {

  private DocumentReference documentReference;
  private String url = "";
  private Integer timeout = 0;
  private String username = "";
  private String password = "";

  /**
   * use {@link XDocBeanLoader} or {@link XObjectBeanConverter} for instantiation.
   */
  @Deprecated
  public RemoteLogin() {
  }

  public DocumentReference getDocumentReference() {
    return documentReference;
  }

  public void setDocumentReference(DocumentReference documentReference) {
    this.documentReference = documentReference;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = nullToEmpty(url);
  }

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = firstNonNull(timeout, 0);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = nullToEmpty(username);
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = nullToEmpty(password);
  }

  @Override
  public String toString() {
    return "RemoteLogin [docRef=" + documentReference + ", url=" + url + ", timeout=" + timeout
        + ", username=" + username + "]";
  }

}
