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
package com.celements.mandatory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiUser;

// TODO add unit tests
public abstract class AbstractMandatoryDocument implements IMandatoryDocumentRole {

  @Requirement
  protected IWebUtilsService webUtilsService;

  @Requirement("xwikiproperties")
  protected ConfigurationSource xwikiPropConfigSource;

  @Requirement
  private Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public abstract String getName();

  @Override
  public void checkDocuments() throws XWikiException {
    getLogger().debug("starting mandatory '{}' for db '{}'", getName(), getWiki());
    if (!skip()) {
      XWikiDocument doc = getDoc();
      boolean dirty;
      if (notMainWiki()) {
        dirty = checkDocuments(doc);
      } else {
        dirty = checkDocumentsMain(doc);
      }
      saveDoc(doc, dirty);
    } else {
      getLogger().debug("skipping mandatory '{}' for db '{}'", getName(), getWiki());
    }
    getLogger().debug("end mandatory '{}' for db '{}'", getName(), getWiki());
  }

  boolean notMainWiki() {
    boolean notMainWiki = (getWiki() != null) && !getWiki().equals(getContext(
        ).getMainXWiki());
    getLogger().debug("not main wiki '{}': {}", getWiki(), notMainWiki);
    return notMainWiki;
  }

  private XWikiDocument getDoc() throws XWikiException {
    XWikiDocument doc;
    if (!getContext().getWiki().exists(getDocRef(), getContext())) {
      XWikiUser originalUser = getContext().getXWikiUser();
      try {
        setUserInContext(getUser());
        doc = new CreateDocumentCommand().createDocument(getDocRef(), null);
        getLogger().info("created doc '{}'", doc);
      } finally {
        setUserInContext(originalUser);
      }
    } else {
      doc = getContext().getWiki().getDocument(getDocRef(), getContext());
      getLogger().debug("already exists doc '{}'", doc);
    }
    return doc;
  }

  private void setUserInContext(XWikiUser user) {
    if (user != null) {
      getContext().setUser(user.getUser(), user.isMain());
    } else {
      getContext().setUser(null);
    }
  }

  protected void saveDoc(XWikiDocument doc, boolean dirty) throws XWikiException {
    if (dirty) {
      getLogger().info("updated doc '{}' for '{}'", doc, getName());
      getContext().getWiki().saveDocument(doc, "autocreate " + getName(), getContext());
    } else {
      getLogger().debug("is uptodate '{}' for '{}'", doc, getName());
    }
  }

  protected abstract DocumentReference getDocRef();

  protected abstract boolean skip();

  protected abstract boolean checkDocuments(XWikiDocument doc) throws XWikiException;

  protected abstract boolean checkDocumentsMain(XWikiDocument doc) throws XWikiException;

  public abstract Logger getLogger();

  protected String getWiki() {
    return getContext().getDatabase();
  }

  protected XWikiUser getUser() {
    XWikiUser user = getContext().getXWikiUser();
    String defaultUserName = xwikiPropConfigSource.getProperty(
        "celements.mandatory.defaultGlobalUserName");
    if (StringUtils.isNotBlank(defaultUserName)) {
      user = new XWikiUser(defaultUserName, true);
    }
    return user;
  }

}
