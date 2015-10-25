package com.celements.rights.access;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.model.access.IModelAccessFacade;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class EntityReferenceRandomCompleterTest extends AbstractBridgedComponentTestCase {

  private EntityReferenceRandomCompleter randomCompleter;
  private XWikiContext context;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void setUp_EntityReferenceRandomCompleterTest() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    context = getContext();
    randomCompleter = (EntityReferenceRandomCompleter) Utils.getComponent(
        IEntityReferenceRandomCompleterRole.class);
  }

  @Test
  public void testRandomCompleteSpaceRef_entityType_Document() {
    DocumentReference entityRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myTestDoc");
    replayDefault();
    assertSame(entityRef, randomCompleter.randomCompleteSpaceRef(entityRef));
    verifyDefault();
  }

  @Test
  public void testRandomCompleteSpaceRef_entityType_Wiki() {
    WikiReference entityRef = new WikiReference(context.getDatabase());
    replayDefault();
    assertSame(entityRef, randomCompleter.randomCompleteSpaceRef(entityRef));
    verifyDefault();
  }

  @Test
  public void testRandomCompleteSpaceRef_entityType_Space() {
    WikiReference wikiRef = new WikiReference(context.getDatabase());
    SpaceReference entityRef = new SpaceReference("mySpace", wikiRef);
    Capture<DocumentReference> randomDocRefCapture = new Capture<>();
    expect(modelAccessMock.exists(capture(randomDocRefCapture))).andReturn(false).once();
    replayDefault();
    DocumentReference randomDocRef =
        (DocumentReference) randomCompleter.randomCompleteSpaceRef(entityRef);
    assertEquals(entityRef, randomDocRef.getLastSpaceReference());
    assertEquals(randomDocRefCapture.getValue(), randomDocRef);
    verifyDefault();
  }

  @Test
  public void testRandomCompleteSpaceRef_entityType_Space_twoLookups() {
    WikiReference wikiRef = new WikiReference(context.getDatabase());
    SpaceReference entityRef = new SpaceReference("mySpace", wikiRef);
    Capture<DocumentReference> randomDocRefCapture = new Capture<>();
    expect(modelAccessMock.exists(capture(randomDocRefCapture))).andReturn(true).once();
    Capture<DocumentReference> randomDocRefCapture2 = new Capture<>();
    expect(modelAccessMock.exists(capture(randomDocRefCapture2))).andReturn(false).once();
    replayDefault();
    EntityReference randomDocRef = randomCompleter.randomCompleteSpaceRef(entityRef);
    verifyDefault();
    DocumentReference docRefFirst = randomDocRefCapture.getValue();
    DocumentReference docRefSecond = randomDocRefCapture2.getValue();
    assertNotNull(docRefFirst);
    assertNotNull(docRefSecond);
    assertEquals(entityRef, docRefFirst.getLastSpaceReference());
    assertFalse("first and second capture may not be equal", docRefFirst.equals(
        randomDocRef));
    assertEquals(entityRef, docRefSecond.getLastSpaceReference());
    assertEquals(docRefSecond, randomDocRef);
  }

}
