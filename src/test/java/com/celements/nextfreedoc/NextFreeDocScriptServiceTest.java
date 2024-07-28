package com.celements.nextfreedoc;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.xpn.xwiki.web.Utils;

public class NextFreeDocScriptServiceTest extends AbstractComponentTest {

  private NextFreeDocScriptService nextFreeDoc;

  @Before
  public void prepareTest() throws ComponentRepositoryException {
    registerComponentMocks(IModelAccessFacade.class);
    nextFreeDoc = (NextFreeDocScriptService) Utils.getComponent(ScriptService.class, "nextfreedoc");
  }

  @Test
  public void test_getNextRandomPageDocRef() {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("mywiki"));
    Integer lengthOfRandomAlphanumeric = 10;
    String prefix = "";

    expect(getMock(IModelAccessFacade.class).exists(anyObject(DocumentReference.class)))
        .andReturn(false).anyTimes();

    replayDefault();
    DocumentReference docRef = nextFreeDoc.getNextRandomPageDocRef(spaceRef,
        lengthOfRandomAlphanumeric, prefix);
    verifyDefault();

    assertNotNull(docRef);
    assertEquals(spaceRef, docRef.getLastSpaceReference());
    assertEquals(10, docRef.getName().length());
  }

  @Test
  public void test_getNextRandomPageDocRef_lengthOfRandomAlphanumericNull() {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("mywiki"));
    Integer lengthOfRandomAlphanumeric = null;
    String prefix = "";

    expect(getMock(IModelAccessFacade.class).exists(anyObject(DocumentReference.class)))
        .andReturn(false).anyTimes();

    replayDefault();
    DocumentReference docRef = nextFreeDoc.getNextRandomPageDocRef(spaceRef,
        lengthOfRandomAlphanumeric, prefix);
    verifyDefault();

    assertNotNull(docRef);
    assertEquals(12, docRef.getName().length());
  }

}
