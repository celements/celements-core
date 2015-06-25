package com.celements.navigation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.QueryManager;

import com.celements.common.cache.AbstractDocumentReferenceCache;
import com.celements.query.IQueryExecutionServiceRole;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;

@Component(NavigationCache.NAME)
public class NavigationCache extends AbstractDocumentReferenceCache<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NavigationCache.class);

  public static final String NAME = "NavigationCache";

  @Requirement
  private INavigationClassConfig navClassConf;

  @Override
  protected DocumentReference getCacheClassRef(WikiReference wikiRef) {
    return navClassConf.getNavigationConfigClassRef(wikiRef);
  }

  @Override
  protected Collection<String> getKeysForResult(DocumentReference docRef
      ) throws XWikiException {
    Collection<String> ret = new HashSet<String>();
    List<BaseObject> navConfObjs = getContext().getWiki().getDocument(docRef, getContext()
        ).getXObjects(getCacheClassRef(webUtils.getWikiRef(docRef)));
    if (navConfObjs != null) {
      for (BaseObject obj : navConfObjs) {
        String spaceName = obj.getStringValue(INavigationClassConfig.MENU_SPACE_FIELD);
        if (!Strings.isNullOrEmpty(spaceName)) {
          ret.add(spaceName);
        }
      }
    }
    return ret;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  void injectQueryManager(QueryManager queryManager) {
    this.queryManager = queryManager;
  }

  void injectQueryExecService(IQueryExecutionServiceRole queryExecService) {
    this.queryExecService = queryExecService;
  }

}
