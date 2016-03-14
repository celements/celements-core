package com.celements.css;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.web.css.CSS;

@ComponentRole
public interface ICssExtensionRole {

  public List<CSS> getCssList();

}
