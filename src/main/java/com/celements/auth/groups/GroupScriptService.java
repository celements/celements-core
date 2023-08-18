package com.celements.auth.groups;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

@Component(GroupScriptService.NAME)
public class GroupScriptService implements ScriptService {

  public static final String NAME = "group";

  private final GroupService groupService;

  @Inject
  public GroupScriptService(GroupService groupService) {
    super();
    this.groupService = groupService;
  }

  public List<DocumentReference> getAllLocalGroups() {
    // woher bekomme ich das lokale Wiki? aus dem Context ModelContext.getWikiRef()
    WikiReference wiki = null;

    return groupService.getAllGroups(wiki);
  }

  // Für getGroupPrettyName Fallback 2: Document Name holen

}
