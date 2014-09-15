package com.celements.web.service;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface ICaptchaServiceRole {

  public boolean checkCaptcha();

  public String getCaptchaId();

  public String getCaptchaId(String captchaType);

}
