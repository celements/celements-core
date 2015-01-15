package com.celements.webform;

import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IActionServiceRole {
  
  public boolean executeAction(Document actionDoc, Map<String, String[]> request, 
      XWikiDocument includingDoc, XWikiContext context);
}
