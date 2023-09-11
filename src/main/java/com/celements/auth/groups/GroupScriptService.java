package com.celements.auth.groups;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;

@Component(GroupScriptService.NAME)
public class GroupScriptService implements ScriptService {

  public static final String NAME = "group";

  private final GroupService groupService;
  private final ModelContext context;

  @Inject
  public GroupScriptService(GroupService groupService, ModelContext context) {
    super();
    this.groupService = groupService;
    this.context = context;
  }

  public List<DocumentReference> getAllLocalGroups() {
    WikiReference wiki = context.getWikiRef();
    return groupService.getAllGroups(wiki);
  }

  // Für getGroupPrettyName Fallback 2: Document Name holen

}
