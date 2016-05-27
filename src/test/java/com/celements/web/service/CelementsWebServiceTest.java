package com.celements.web.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.xpn.xwiki.web.Utils;

public class CelementsWebServiceTest extends AbstractComponentTest {

  private CelementsWebService celementsweb;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void setUp_CelementsWebServiceTest() throws Exception {
    celementsweb = (CelementsWebService) Utils.getComponent(ICelementsWebServiceRole.class);
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    celementsweb.modelAccess = modelAccessMock;
  }

  @Test
  public void testGetNewRandomXWikiUserName_randomString() {
    expect(modelAccessMock.exists((DocumentReference) anyObject())).andReturn(false);
    replayDefault();
    String result = celementsweb.getNewRandomXWikiUserName(null);
    verifyDefault();
    assertEquals(12, result.length());
    assertTrue("Expected alpha numeric result", result.matches("^[a-zA-Z0-9]{12}$"));
  }

  @Test
  public void testGetNewRandomXWikiUserName_hasNew() {
    String inputName = "name123";
    expect(modelAccessMock.exists(eq(new DocumentReference(getContext().getDatabase(), "XWiki",
        inputName)))).andReturn(false);
    replayDefault();
    assertEquals(inputName, celementsweb.getNewRandomXWikiUserName(inputName));
    verifyDefault();
  }

  @Test
  public void testGetNewRandomXWikiUserName_existingAsNew() {
    String inputName = "name123";
    expect(modelAccessMock.exists(eq(new DocumentReference(getContext().getDatabase(), "XWiki",
        inputName)))).andReturn(true);
    expect(modelAccessMock.exists((DocumentReference) anyObject())).andReturn(false);
    replayDefault();
    String result = celementsweb.getNewRandomXWikiUserName(inputName);
    verifyDefault();
    assertEquals(12, result.length());
    assertTrue("Expected alpha numeric result", result.matches("^[a-zA-Z0-9]{12}$"));
  }
}
