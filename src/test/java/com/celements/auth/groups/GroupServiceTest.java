package com.celements.auth.groups;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.web.Utils;

public class GroupServiceTest extends AbstractComponentTest {

  private GroupService service;

  @Before
  public void prepare_Test() throws Exception {
    registerComponentMocks(IWebUtilsService.class, IModelAccessFacade.class, ModelUtils.class,
        ModelContext.class);
    service = Utils.getComponent(GroupService.class);
    expect(getMock(XWiki.class).getGroupService(getXContext())).andReturn(createDefaultMock(
        XWikiGroupService.class)).anyTimes();

  }

  @Test
  public void test_getAllGroups() {
    WikiReference wiki = new WikiReference("xwikidb");
    List<String> groupNames = new ArrayList<>();
    groupNames.add("group1");
    groupNames.add("group2");
    groupNames.add("group3");
    List<DocumentReference> groupDocRefList = new ArrayList<>();

  }

  @Test
  public void test_getAllGroups_wikiRefNull() {
    WikiReference wiki = null;
    Exception exception = assertThrows(NullPointerException.class, () -> {
      service.getAllGroups(wiki);
    });

  }

  @Test
  public void test_getAllGroups_failing() {

  }

  @Test
  public void test_getGroupPrettyName() {

  }

  @Test
  public void test_getGroupPrettyName_docRefNull() {

  }

  @Test
  public void test_getGroupPrettyName_noDictKey() {

  }

  @Test
  public void test_getGroupPrettyName_noDocTitle() {

  }

}
