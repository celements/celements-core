package com.celements.rights.access;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.rights.access.internal.IEntityReferenceRandomCompleterRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;
import com.xpn.xwiki.web.Utils;

public class DefaultRightsAccessFacadeTest extends AbstractBridgedComponentTestCase {

  private XWiki xwiki;
  private XWikiContext context;
  private DefaultRightsAccessFacade rightsAccess;
  private XWikiGroupService groupSrvMock;
  private IWebUtilsService webUtilsService;

  @Before
  public void setUp_DefaultRightsAccessFacadeTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    rightsAccess = (DefaultRightsAccessFacade) Utils.getComponent(
        IRightsAccessFacadeRole.class);
    webUtilsService = Utils.getComponent(IWebUtilsService.class);
    groupSrvMock = createMockAndAddToDefault(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(context))).andReturn(groupSrvMock).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    expect(xwiki.getWikiOwner(eq(context.getDatabase()), same(context))).andReturn(
        "xwiki:XWiki.Admin").anyTimes();
    expect(xwiki.getMaxRecursiveSpaceChecks(same(context))).andReturn(0).anyTimes();
    expect(xwiki.isReadOnly()).andReturn(false).anyTimes();
  }

  @Test
  @Deprecated
  public void testHasAccessLevel_document_edit_true_deprecated() throws Exception {
    XWikiRightService xwikiRightsService = new XWikiRightServiceImpl();
    expect(xwiki.getRightService()).andReturn(xwikiRightsService).anyTimes();
    XWikiUser user = new XWikiUser("XWiki.TestUser");
    String spaceName = "MySpace";
    WikiReference wikiRef = new WikiReference(context.getDatabase());
    SpaceReference spaceRef = new SpaceReference(spaceName, wikiRef);
    DocumentReference docRef = new DocumentReference("MyTestDoc", spaceRef);
    XWikiDocument testDoc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(testDoc).anyTimes();
    String docFN = context.getDatabase() + ":MySpace.MyTestDoc";
    expect(xwiki.getDocument(eq(docFN), same(context))).andReturn(testDoc
        ).anyTimes();
    prepareEmptyGroupMembers(user);
    prepareMasterRights();
    prepareSpaceRights(spaceRef);
    replayDefault();
    assertTrue(rightsAccess.hasAccessLevel("edit", user, docRef));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testHasAccessLevel_document_edit_Guest_false_deprecated() throws Exception {
    XWikiRightService xwikiRightsService = new XWikiRightServiceImpl();
    expect(xwiki.getRightService()).andReturn(xwikiRightsService).anyTimes();
    XWikiUser user = new XWikiUser(XWikiRightService.GUEST_USER);
    String spaceName = "MySpace";
    WikiReference wikiRef = new WikiReference(context.getDatabase());
    SpaceReference spaceRef = new SpaceReference(spaceName, wikiRef);
    DocumentReference docRef = new DocumentReference("MyTestDoc", spaceRef);
    XWikiDocument testDoc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(testDoc).anyTimes();
    String docFN = context.getDatabase() + ":MySpace.MyTestDoc";
    expect(xwiki.getDocument(eq(docFN), same(context))).andReturn(testDoc
        ).anyTimes();
    prepareEmptyGroupMembers(user);
    prepareMasterRights();
    prepareSpaceRights(spaceRef);
    replayDefault();
    assertFalse(rightsAccess.hasAccessLevel("edit", user, docRef));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testHasAccessLevel_wiki_edit_false_deprecated() throws Exception {
    XWikiUser user = new XWikiUser("XWiki.TestUser");
    WikiReference wikiRef = new WikiReference(context.getDatabase());
    replayDefault();
    assertFalse(rightsAccess.hasAccessLevel("edit", user, wikiRef));
    verifyDefault();
  }

  @Test
  public void testHasAccessLevel_wiki_edit_false() throws Exception {
    XWikiUser user = new XWikiUser("XWiki.TestUser");
    WikiReference wikiRef = new WikiReference(context.getDatabase());
    replayDefault();
    assertFalse(rightsAccess.hasAccessLevel(wikiRef, EAccessLevel.EDIT, user));
    verifyDefault();
  }

  @Test
  public void testHasAccessLevel_docRef() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace", 
        "MyDocument");
    EAccessLevel level = EAccessLevel.EDIT;
    XWikiRightService mockRightsService = createMockAndAddToDefault(
        XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightsService).anyTimes();
    expect(mockRightsService.hasAccessLevel(eq(level.getIdentifier()), eq(getContext(
        ).getUser()), eq(webUtilsService.serializeRef(docRef)), same(context))
        ).andReturn(true).once();
    
    replayDefault();
    assertTrue(rightsAccess.hasAccessLevel(docRef, level));
    verifyDefault();
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testHasAccessLevel_document_edit_true() throws Exception {
    XWikiRightService xwikiRightsService = new XWikiRightServiceImpl();
    expect(xwiki.getRightService()).andReturn(xwikiRightsService).anyTimes();
    XWikiUser user = new XWikiUser("XWiki.TestUser");
    String spaceName = "MySpace";
    WikiReference wikiRef = new WikiReference(context.getDatabase());
    SpaceReference spaceRef = new SpaceReference(spaceName, wikiRef);
    DocumentReference docRef = new DocumentReference("MyTestDoc", spaceRef);
    XWikiDocument testDoc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(testDoc).anyTimes();
    String docFN = context.getDatabase() + ":MySpace.MyTestDoc";
    expect(xwiki.getDocument(eq(docFN), same(context))).andReturn(testDoc
        ).anyTimes();
    prepareEmptyGroupMembers(user);
    prepareMasterRights();
    prepareSpaceRights(spaceRef);
    replayDefault();
    assertTrue(rightsAccess.hasAccessLevel(docRef, EAccessLevel.EDIT, user));
    verifyDefault();
  }

  @Test
  public void testHasAccessLevel_document_edit_Guest_false() throws Exception {
    XWikiRightService xwikiRightsService = new XWikiRightServiceImpl();
    expect(xwiki.getRightService()).andReturn(xwikiRightsService).anyTimes();
    XWikiUser user = new XWikiUser(XWikiRightService.GUEST_USER);
    String spaceName = "MySpace";
    WikiReference wikiRef = new WikiReference(context.getDatabase());
    SpaceReference spaceRef = new SpaceReference(spaceName, wikiRef);
    DocumentReference docRef = new DocumentReference("MyTestDoc", spaceRef);
    prepareEmptyGroupMembers(user);
    prepareMasterRights();
    prepareSpaceRights(spaceRef);
    replayDefault();
    assertFalse(rightsAccess.hasAccessLevel(docRef, EAccessLevel.EDIT, user));
    verifyDefault();
  }

  @Test
  public void testHasAccessLevel_spaceRef() throws Exception {
    SpaceReference spaceRef = new SpaceReference("MySpace", webUtilsService.getWikiRef());
    DocumentReference docRef = new DocumentReference("untitled1", spaceRef);
    EAccessLevel level = EAccessLevel.EDIT;
    XWikiRightService mockRightsService = createMockAndAddToDefault(
        XWikiRightService.class);
    IEntityReferenceRandomCompleterRole randomCompleterMock = createMockAndAddToDefault(
        IEntityReferenceRandomCompleterRole.class);
    rightsAccess.randomCompleter = randomCompleterMock;
    expect(randomCompleterMock.randomCompleteSpaceRef(eq(spaceRef))).andReturn(docRef
        ).once();
    expect(xwiki.getRightService()).andReturn(mockRightsService).anyTimes();
    expect(mockRightsService.hasAccessLevel(eq(level.getIdentifier()), eq(getContext(
        ).getUser()), eq(webUtilsService.serializeRef(docRef)), same(context))
        ).andReturn(true).once();
    prepareSpaceRights(spaceRef);
    replayDefault();
    assertTrue(rightsAccess.hasAccessLevel(spaceRef, level));
    verifyDefault();
  }

  @Test
  public void testHasAccessLevel_attRef() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace", 
        "MyDocument");
    AttachmentReference attRef = new AttachmentReference("file", docRef);
    EAccessLevel level = EAccessLevel.EDIT;
    XWikiRightService mockRightsService = createMockAndAddToDefault(
        XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightsService).anyTimes();
    expect(mockRightsService.hasAccessLevel(eq(level.getIdentifier()), eq(getContext(
        ).getUser()), eq(webUtilsService.serializeRef(docRef)), same(context))
        ).andReturn(true).once();
    
    replayDefault();
    assertTrue(rightsAccess.hasAccessLevel(attRef, level));
    verifyDefault();
  }

  @Test
  public void testHasAccessLevel_wikiRefRef() throws Exception {
    EAccessLevel level = EAccessLevel.EDIT;
    
    replayDefault();
    assertFalse(rightsAccess.hasAccessLevel(webUtilsService.getWikiRef(), level));
    verifyDefault();
  }

  @Test
  public void testHasAccessLevel_user() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace", 
        "MyDocument");
    EAccessLevel level = EAccessLevel.VIEW;
    XWikiUser user = new XWikiUser("MySpace.MyUser");
    XWikiRightService mockRightsService = createMockAndAddToDefault(
        XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightsService).anyTimes();
    expect(mockRightsService.hasAccessLevel(eq(level.getIdentifier()), eq(user.getUser()), 
        eq(webUtilsService.serializeRef(docRef)), same(context))).andReturn(false).once();
    
    replayDefault();
    assertFalse(rightsAccess.hasAccessLevel(docRef, level, user));
    verifyDefault();
  }

  @Deprecated
  private void prepareSpaceRights(SpaceReference spaceRef) throws XWikiException {
    String webPrefDocName = "WebPreferences";
    DocumentReference spacePrefDocRef = new DocumentReference(webPrefDocName, spaceRef);
    XWikiDocument spacePrefDoc = new XWikiDocument(spacePrefDocRef);
    spacePrefDoc.setNew(false);
    expect(xwiki.getDocument(eq(spaceRef.getName()), eq(webPrefDocName), same(context))
        ).andReturn(spacePrefDoc).anyTimes();
  }

  private void prepareEmptyGroupMembers(XWikiUser user) throws XWikiException {
    DocumentReference userRef = webUtilsService.resolveDocumentReference(user.getUser());
    if (XWikiRightService.GUEST_USER.equals(userRef.getName())) {
      expect(groupSrvMock.getAllGroupsReferencesForMember(eq(userRef), eq(0),
          eq(0), same(context))).andReturn(Collections.<DocumentReference>emptyList()
              ).anyTimes();
    } else {
      DocumentReference xwikiAllGroupRef = new DocumentReference(context.getDatabase(),
          "XWiki", "XWikiAllGroup");
      expect(groupSrvMock.getAllGroupsReferencesForMember(eq(userRef), eq(0), eq(0),
          same(context))).andReturn(Arrays.asList(xwikiAllGroupRef)).anyTimes();
      expect(groupSrvMock.getAllGroupsReferencesForMember(eq(xwikiAllGroupRef), eq(0),
          eq(0), same(context))).andReturn(Collections.<DocumentReference>emptyList()
              ).anyTimes();
    }
  }

  @Deprecated
  private void prepareMasterRights() throws XWikiException {
    DocumentReference wikiPrefDocRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiPreferences");
    XWikiDocument wikiPrefDoc = new XWikiDocument(wikiPrefDocRef);
    wikiPrefDoc.setNew(false);
    expect(xwiki.getDocument(eq("XWiki.XWikiPreferences"), same(context))).andReturn(
        wikiPrefDoc).anyTimes();
    expect(xwiki.getXWikiPreference(eq("authenticate_edit"), eq(""), same(context))
        ).andReturn("yes").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("authenticate_edit"), eq(0), same(context))
        ).andReturn(1).anyTimes();
    expect(xwiki.getSpacePreference(eq("authenticate_edit"), eq(""), same(context))
        ).andReturn("").anyTimes();
    expect(xwiki.getSpacePreferenceAsInt(eq("authenticate_edit"), eq(0), same(context))
        ).andReturn(0).anyTimes();
  }

}
