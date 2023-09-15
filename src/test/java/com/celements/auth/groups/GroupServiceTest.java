package com.celements.auth.groups;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiGroupService;

public class GroupServiceTest extends AbstractComponentTest {

  private GroupService service;
  private WikiReference wiki = new WikiReference("xwikidb");

  @Before
  public void prepare_Test() throws Exception {
    registerComponentMocks(IWebUtilsService.class, IModelAccessFacade.class, ModelContext.class);
    expect(getMock(XWiki.class).getGroupService(getXContext())).andReturn(createDefaultMock(
        XWikiGroupService.class)).anyTimes();
    expect(getMock(IWebUtilsService.class).getAdminMessageTool())
        .andReturn(getMessageToolStub()).anyTimes();
    expect(getMock(ModelContext.class).getXWikiContext()).andReturn(getXContext()).anyTimes();
    expect(getMock(ModelContext.class).getWikiRef()).andReturn(wiki).anyTimes();
    service = getBeanFactory().getBean(GroupService.class);
  }

  @Test
  public void test_getAllGroups() throws Exception {
    List<String> groupNames = Arrays.asList("XWiki.group1", "XWiki.group2", "XWiki.group3");
    expect(getMock(XWikiGroupService.class).getAllMatchedGroups(null, false, 0, 0, null,
        getXContext())).andReturn(castListType(groupNames));

    replayDefault();
    List<DocumentReference> groupDocRefList = service.getAllGroups(wiki);
    verifyDefault();

    assertEquals(3, groupDocRefList.size());
    assertEquals("group1", groupDocRefList.get(0).getName());
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> castListType(List<?> list) {
    return (List<T>) list;
  }

  @Test
  public void test_getAllGroups_wikiRefNull() {
    WikiReference wiki2 = null;
    assertThrows(NullPointerException.class, () -> {
      service.getAllGroups(wiki2);
    });
  }

  @Test
  public void test_getAllGroups_XWikiException() throws Exception {
    expect(getMock(XWikiGroupService.class).getAllMatchedGroups(null, false, 0, 0, null,
        getXContext())).andThrow(new XWikiException());

    replayDefault();
    List<DocumentReference> groupDocRefList = service.getAllGroups(wiki);
    verifyDefault();

    assertEquals(0, groupDocRefList.size());
  }

  @Test
  public void test_getGroupPrettyName() {
    DocumentReference groupDocRef = new DocumentReference("wikidb", "XWiki", "group1");
    getMessageToolStub().injectMessage("cel_groupname_" + groupDocRef.getName(), "Gruppe 1");

    replayDefault();
    Optional<String> groupPrettyName = service.getGroupPrettyName(groupDocRef);
    verifyDefault();

    assertTrue(groupPrettyName.isPresent());
    assertEquals("Gruppe 1", groupPrettyName.get());
  }

  @Test
  public void test_getGroupPrettyName_docRefNull() {
    DocumentReference groupDocRef = null;
    assertThrows(NullPointerException.class, () -> {
      service.getGroupPrettyName(groupDocRef);
    });
  }

  @Test
  public void test_getGroupPrettyName_noDictKey() throws Exception {
    DocumentReference groupDocRef = new DocumentReference("wikidb", "XWiki", "group1");
    XWikiDocument groupDoc = new XWikiDocument(groupDocRef);
    groupDoc.setTitle("group1Title");
    getMessageToolStub().injectMessage("cel_groupname_" + groupDocRef.getName(),
        "cel_groupname_" + groupDocRef.getName());
    expect(getMock(IModelAccessFacade.class).getDocument(groupDocRef)).andReturn(groupDoc);

    replayDefault();
    Optional<String> groupPrettyName = service.getGroupPrettyName(groupDocRef);
    verifyDefault();

    assertTrue(groupPrettyName.isPresent());
    assertEquals("group1Title", groupPrettyName.get());
  }

  @Test
  public void test_getGroupPrettyName_noDocTitle() throws Exception {
    DocumentReference groupDocRef = new DocumentReference("wikidb", "XWiki", "group1");
    XWikiDocument groupDoc = new XWikiDocument(groupDocRef);
    getMessageToolStub().injectMessage("cel_groupname_" + groupDocRef.getName(),
        "cel_groupname_" + groupDocRef.getName());
    expect(getMock(IModelAccessFacade.class).getDocument(groupDocRef)).andReturn(groupDoc);

    replayDefault();
    Optional<String> groupPrettyName = service.getGroupPrettyName(groupDocRef);
    verifyDefault();

    assertTrue(groupPrettyName.isEmpty());
  }

  @Test
  public void test_getGroupPrettyName_DocumentNotExistsException() throws Exception {
    DocumentReference groupDocRef = new DocumentReference("wikidb", "XWiki", "group1");
    getMessageToolStub().injectMessage("cel_groupname_" + groupDocRef.getName(),
        "cel_groupname_" + groupDocRef.getName());
    expect(getMock(IModelAccessFacade.class).getDocument(groupDocRef))
        .andThrow(new DocumentNotExistsException(groupDocRef));

    replayDefault();
    Optional<String> groupPrettyName = service.getGroupPrettyName(groupDocRef);
    verifyDefault();

    assertTrue(groupPrettyName.isEmpty());
  }

}
