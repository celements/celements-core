package com.celements.navigation.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.NavigationClasses;
import com.celements.navigation.service.ITreeNodeCache;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class DeleteMenuItemCommand {

  private static Log LOGGER = LogFactory.getFactory().getInstance(DeleteMenuItemCommand.class);

  public boolean deleteMenuItem(DocumentReference docRef) {
    DocumentReference menuItemClassRef = new DocumentReference(getContext().getDatabase(),
        NavigationClasses.MENU_ITEM_CLASS_SPACE, NavigationClasses.MENU_ITEM_CLASS_DOC);
    XWikiDocument doc = null;
    try {
      doc = getContext().getWiki().getDocument(docRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get document by reference [" + docRef + "].", exp);
      return false;
    }
    if ((doc != null) && doc.getXObject(menuItemClassRef) != null) {
      try {
        boolean result = doc.removeXObjects(menuItemClassRef);
        getContext().getWiki().saveDocument(doc, "remove menu item", getContext());
        flushMenuItemCache();
        return result;
      } catch (XWikiException exp) {
        LOGGER.error("Failed to save document after removing menu items.", exp);
      }
    }
    return false;
  }

  private void flushMenuItemCache() {
    ITreeNodeCache cacheService = Utils.getComponent(ITreeNodeCache.class);
    if (cacheService != null) {
      cacheService.flushMenuItemCache();
    }
  }

  private XWikiContext getContext() {
    return (XWikiContext)getExecution().getContext().getProperty("xwikicontext");
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

}
