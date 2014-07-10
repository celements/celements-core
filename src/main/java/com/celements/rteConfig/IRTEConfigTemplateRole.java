package com.celements.rteConfig;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface IRTEConfigTemplateRole {

  public List<BaseObject> getRTETemplateList() throws XWikiException;

}
