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

import com.celements.init.XWikiProvider;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.Contextualiser;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiException;

@Component
public class GroupService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GroupService.class);

  private final XWikiProvider xwiki;
  private final IWebUtilsService webUtils;
  private final ModelUtils modelUtils;
  private final ModelContext context;
  private final IModelAccessFacade modelAccess;

  @Inject
  public GroupService(XWikiProvider xwiki, IWebUtilsService webUtils,
      ModelUtils modelUtils, ModelContext context, IModelAccessFacade modelAccess) {
    super();
    this.xwiki = xwiki;
    this.webUtils = webUtils;
    this.modelUtils = modelUtils;
    this.context = context;
    this.modelAccess = modelAccess;
  }

  /**
   * Gets the document references for all local groups of a database/wiki and returns them in a list
   *
   * @param wikiRef
   * @return a list of group document references
   */
  public @NotNull List<DocumentReference> getAllGroups(@NotNull WikiReference wikiRef) {
    checkNotNull(wikiRef);
    List<DocumentReference> groupDocRefList = new ArrayList<>();
    List<String> groupNames = getAllGroupFullNames(wikiRef);
    for (String groupName : groupNames) {
      DocumentReference groupDocRef = modelUtils.resolveRef(groupName, DocumentReference.class);
      groupDocRefList.add(groupDocRef);
    }
    return groupDocRefList;
  }

  @SuppressWarnings("unchecked")
  private List<String> getAllGroupFullNames(WikiReference wikiRef) {
    List<String> groupFullNames = new ArrayList<>();
    try {
      groupFullNames = (List<String>) new Contextualiser()
          .withWiki(wikiRef)
          .execute(rethrow(() -> xwiki.get()
              .orElseThrow()
              .getGroupService(context.getXWikiContext())
              .getAllMatchedGroups(null, false, 0, 0, null, context.getXWikiContext())));
    } catch (XWikiException xwe) {
      LOGGER.error("failed to get all groups for [{}]", wikiRef, xwe);
    }
    return groupFullNames;
  }

  /**
   * Gets the PrettyName of a group out of the dictionary. If it has no entry it uses the document
   * title as fallback. If it has no document title it returns an absent Optional.
   *
   * @param groupDocRef
   * @return an Optional String
   */
  public @NotNull Optional<String> getGroupPrettyName(@NotNull DocumentReference groupDocRef) {
    checkNotNull(groupDocRef);
    String dictKey = "cel_groupname_" + groupDocRef.getName();
    return Optional.of(webUtils.getAdminMessageTool().get(dictKey))
        .filter(value -> !value.equals(dictKey))
        .or(() -> getDocumentTitle(groupDocRef));
  }

  private Optional<String> getDocumentTitle(DocumentReference groupDocRef) {
    Optional<String> docTitle = Optional.empty();
    try {
      docTitle = Optional.of(modelAccess.getDocument(groupDocRef).getTitle())
          .filter(t -> !t.isBlank());
    } catch (DocumentNotExistsException dnee) {
      LOGGER.warn("could not get Document for {}", groupDocRef, dnee);
    }
    return docTitle;
  }

}
