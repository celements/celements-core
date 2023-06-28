package com.celements.nextfreedoc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class NextFreeDocScriptServiceTest extends AbstractComponentTest {

  private NextFreeDocScriptService nextFreeDoc;

  @Before
  public void prepareTest() throws Exception {
    nextFreeDoc = (NextFreeDocScriptService) Utils.getComponent(ScriptService.class);
  }

  @Test
  public void test_getNextRandomPageDocRef() {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("mywiki"));
    Integer lengthOfRandomAlphanumeric = 10;
    String prefix = "";

    DocumentReference docRef = nextFreeDoc.getNextRandomPageDocRef(spaceRef,
        lengthOfRandomAlphanumeric, prefix);

    assertNotNull(docRef);
    assertEquals(spaceRef, docRef.getLastSpaceReference());
    assertEquals(10, docRef.getName().length());
  }

  @Test
  public void test_getNextRandomPageDocRef_lengthOfRandomAlphanumericNull() {
    SpaceReference spaceRef = new SpaceReference("mySpace", new WikiReference("mywiki"));
    Integer lengthOfRandomAlphanumeric = null;
    String prefix = "";

    DocumentReference docRef = nextFreeDoc.getNextRandomPageDocRef(spaceRef,
        lengthOfRandomAlphanumeric, prefix);

    assertNotNull(docRef);
    assertEquals(12, docRef.getName().length());
  }

}
