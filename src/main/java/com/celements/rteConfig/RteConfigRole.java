package com.celements.rteConfig;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiException;

@ComponentRole
public interface RteConfigRole {

  List<DocumentReference> getRTEConfigsList();

  String getRTEConfigField(String name) throws XWikiException;

}
