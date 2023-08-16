package com.celements.auth.groups;

import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.context.Contextualiser;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiGroupService;

@Component
public class GroupService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GroupService.class);

  private final XWikiGroupService xwikiGroupService;
  private final IWebUtilsService webUtils;

  @Inject
  public GroupService(XWikiGroupService xwikiGroupService, IWebUtilsService webUtils) {
    super();
    this.xwikiGroupService = xwikiGroupService;
    this.webUtils = webUtils;
  }

  /**
   * Gets the document references for all local groups of a database/wiki and returns them in a list
   *
   * @return a list of group document references
   */
  public @NotNull List<DocumentReference> getAllGroups(@NotNull WikiReference wiki) {
    checkNotNull(wiki);
    // Woher bekomme ich den XWikiContext? ModelContext.getXWikiContext() is discouraged...
    XWikiContext context = null;
    List<DocumentReference> groupDocRefList = new ArrayList<>();
    try {
      List<XWikiDocument> groupxDocList = (List<XWikiDocument>) new Contextualiser()
          .withWiki(wiki)
          .execute(rethrow(() -> xwikiGroupService
              .getAllMatchedGroups(null, true, 0, 0, null, context)));
      for (XWikiDocument groupxDoc : groupxDocList) {
        Document groupDoc = new Document(groupxDoc, context);
        groupDocRefList.add(groupDoc.getDocumentReference());
      }
    } catch (XWikiException xwe) {
      LOGGER.error("failed to get all groups for [{}]", wiki, xwe);
      return groupDocRefList;
    }
    return groupDocRefList;
  }

  /**
   * Gets the PrettyName of a group out of the dictionary. If it has no entry it uses the document
   * title as fallback. If it has no document title it returns an absent Optional.
   *
   * @return a String with the pretty name of the group
   */
  public @NotNull Optional<String> getGroupPrettyName(@NotNull DocumentReference groupDocRef) {
    checkNotNull(groupDocRef);
    // prettyName aus Dictionary holen
    String dictKey = "cel_groupname_".concat(groupDocRef.getName());
    Optional<String> groupPrettyName = Optional.of(webUtils.getAdminMessageTool().get(dictKey));

    // Fallback 1: Document Title holen

    // Fallback 2: Document Name holen

    return groupPrettyName;
  }

}
