package com.celements.rights.access;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
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
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;
import com.xpn.xwiki.web.Utils;

public class DefaultRightsAccessFacade_SpaceTest extends AbstractBridgedComponentTestCase {

  private XWiki xwiki;
  private XWikiContext context;
  private DefaultRightsAccessFacade rightsAccess;
  private XWikiGroupService groupSrvMock;
  private IWebUtilsService webUtilsService;
  private IEntityReferenceRandomCompleterRole randomCompleterMock;

  @Before
  public void setUp_DefaultRightsAccessFacadeTest() throws Exception {
    randomCompleterMock = registerComponentMock(IEntityReferenceRandomCompleterRole.class);
    context = getContext();
    xwiki = getWikiMock();
    rightsAccess = (DefaultRightsAccessFacade) Utils.getComponent(IRightsAccessFacadeRole.class);
    webUtilsService = Utils.getComponent(IWebUtilsService.class);
    XWikiRightService xwikiRightsService = new XWikiRightServiceImpl();
    expect(xwiki.getRightService()).andReturn(xwikiRightsService).anyTimes();
    groupSrvMock = createMockAndAddToDefault(XWikiGroupService.class);
    expect(xwiki.getGroupService(same(context))).andReturn(groupSrvMock).anyTimes();
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    expect(xwiki.getWikiOwner(eq(context.getDatabase()), same(context))).andReturn(
        "xwiki:XWiki.Admin").anyTimes();
    expect(xwiki.getMaxRecursiveSpaceChecks(same(context))).andReturn(0).anyTimes();
    expect(xwiki.isReadOnly()).andReturn(false).anyTimes();
  }

  @Test
  public void testHasAccessLevel_space_edit_false() throws Exception {
    XWikiUser user = new XWikiUser("XWiki.TestUser");
    String spaceName = "MySpace";
    WikiReference wikiRef = new WikiReference(context.getDatabase());
    SpaceReference spaceRef = new SpaceReference(spaceName, wikiRef);
    prepareEmptyGroupMembers(user, Collections.<DocumentReference>emptyList());
    prepareMasterRights();
    XWikiDocument spaceDoc = prepareSpaceRights(spaceRef);
    String docName = "987asdfku2";
    DocumentReference docRef = new DocumentReference(docName, spaceRef);
    expect(randomCompleterMock.randomCompleteSpaceRef(eq(spaceRef))).andReturn(docRef);
    XWikiDocument testDoc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(testDoc).anyTimes();
    String docFN = context.getDatabase() + ":" + spaceName + "." + docName;
    expect(xwiki.getDocument(eq(docFN), same(context))).andReturn(testDoc).anyTimes();
    BaseObject rightsObj = new BaseObject();
    DocumentReference rightsClassRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiGlobalRights");
    rightsObj.setDocumentReference(spaceDoc.getDocumentReference());
    rightsObj.setXClassReference(rightsClassRef);
    rightsObj.setNumber(0);
    rightsObj.setStringValue("users", "");
    rightsObj.setStringValue("groups", "XWiki.Editors");
    rightsObj.setStringValue("levels", "edit");
    rightsObj.setIntValue("allow", 1);
    spaceDoc.addXObject(rightsObj);
    replayDefault();
    assertFalse(rightsAccess.hasAccessLevel("edit", user, spaceRef));
    verifyDefault();
  }

  @Test
  public void testHasAccessLevel_space_edit_true() throws Exception {
    XWikiUser user = new XWikiUser("XWiki.TestUser");
    String spaceName = "MySpace";
    WikiReference wikiRef = new WikiReference(context.getDatabase());
    SpaceReference spaceRef = new SpaceReference(spaceName, wikiRef);
    prepareEmptyGroupMembers(user, Arrays.asList(new DocumentReference(context.getDatabase(),
        "XWiki", "Editors")));
    prepareMasterRights();
    XWikiDocument spaceDoc = prepareSpaceRights(spaceRef);
    String docName = "987asdfku2";
    DocumentReference docRef = new DocumentReference(docName, spaceRef);
    expect(randomCompleterMock.randomCompleteSpaceRef(eq(spaceRef))).andReturn(docRef);
    XWikiDocument testDoc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(testDoc).anyTimes();
    String docFN = context.getDatabase() + ":" + spaceName + "." + docName;
    expect(xwiki.getDocument(eq(docFN), same(context))).andReturn(testDoc).anyTimes();
    BaseObject rightsObj = new BaseObject();
    DocumentReference rightsClassRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiGlobalRights");
    rightsObj.setDocumentReference(spaceDoc.getDocumentReference());
    rightsObj.setXClassReference(rightsClassRef);
    rightsObj.setNumber(0);
    rightsObj.setStringValue("users", "");
    rightsObj.setStringValue("groups", "XWiki.Editors");
    rightsObj.setStringValue("levels", "edit");
    rightsObj.setIntValue("allow", 1);
    spaceDoc.addXObject(rightsObj);
    replayDefault();
    assertTrue(rightsAccess.hasAccessLevel("edit", user, spaceRef));
    verifyDefault();
  }

  private XWikiDocument prepareSpaceRights(SpaceReference spaceRef) throws XWikiException {
    String webPrefDocName = "WebPreferences";
    DocumentReference spacePrefDocRef = new DocumentReference(webPrefDocName, spaceRef);
    XWikiDocument spacePrefDoc = new XWikiDocument(spacePrefDocRef);
    spacePrefDoc.setNew(false);
    expect(xwiki.getDocument(eq(spaceRef.getName()), eq(webPrefDocName), same(context))).andReturn(
        spacePrefDoc).anyTimes();
    return spacePrefDoc;
  }

  private void prepareEmptyGroupMembers(XWikiUser user, List<DocumentReference> additionalGroups)
      throws XWikiException {
    DocumentReference userRef = webUtilsService.resolveDocumentReference(user.getUser());
    if (XWikiRightService.GUEST_USER.equals(userRef.getName())) {
      expect(groupSrvMock.getAllGroupsReferencesForMember(eq(userRef), eq(0), eq(0), same(
          context))).andReturn(Collections.<DocumentReference>emptyList()).anyTimes();
    } else {
      DocumentReference xwikiAllGroupRef = new DocumentReference(context.getDatabase(), "XWiki",
          "XWikiAllGroup");
      List<DocumentReference> groupList = new ArrayList<>(additionalGroups);
      groupList.add(xwikiAllGroupRef);
      expect(groupSrvMock.getAllGroupsReferencesForMember(eq(userRef), eq(0), eq(0), same(
          context))).andReturn(groupList).anyTimes();
      expect(groupSrvMock.getAllGroupsReferencesForMember(eq(xwikiAllGroupRef), eq(0), eq(0), same(
          context))).andReturn(Collections.<DocumentReference>emptyList()).anyTimes();
      for (DocumentReference groupRef : additionalGroups) {
        expect(groupSrvMock.getAllGroupsReferencesForMember(eq(groupRef), eq(0), eq(0), same(
            context))).andReturn(Collections.<DocumentReference>emptyList()).anyTimes();
      }
    }
  }

  private void prepareMasterRights() throws XWikiException {
    DocumentReference wikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument wikiPrefDoc = new XWikiDocument(wikiPrefDocRef);
    wikiPrefDoc.setNew(false);
    expect(xwiki.getDocument(eq("XWiki.XWikiPreferences"), same(context))).andReturn(
        wikiPrefDoc).anyTimes();
    expect(xwiki.getXWikiPreference(eq("authenticate_edit"), eq(""), same(context))).andReturn(
        "yes").anyTimes();
    expect(xwiki.getXWikiPreferenceAsInt(eq("authenticate_edit"), eq(0), same(context))).andReturn(
        1).anyTimes();
    expect(xwiki.getSpacePreference(eq("authenticate_edit"), eq(""), same(context))).andReturn(
        "").anyTimes();
    expect(xwiki.getSpacePreferenceAsInt(eq("authenticate_edit"), eq(0), same(context))).andReturn(
        0).anyTimes();
  }

}
