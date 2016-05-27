package com.celements.parents;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class XDocParentsTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private XDocParents xdocParents;

  @Before
  public void setUp_WebUtilsServiceTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    xdocParents = (XDocParents) Utils.getComponent(IDocParentProviderRole.class,
        XDocParents.DOC_PROVIDER_NAME);
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
  }

  @Test
  public void testGetDocumentParentsList() throws XWikiException {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    DocumentReference parentRef1 = new DocumentReference(context.getDatabase(), "mySpace",
        "parent1");
    DocumentReference parentRef2 = new DocumentReference(context.getDatabase(), "mySpace",
        "parent2");
    XWikiDocument doc = new XWikiDocument(docRef);
    XWikiDocument docP1 = new XWikiDocument(parentRef1);
    XWikiDocument docP2 = new XWikiDocument(parentRef2);
    docP1.setParentReference(parentRef2.extractReference(EntityType.DOCUMENT));
    doc.setParentReference(parentRef1.extractReference(EntityType.DOCUMENT));
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.getDocument(eq(parentRef1), same(context))).andReturn(docP1).once();
    expect(xwiki.exists(eq(parentRef1), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(parentRef2), same(context))).andReturn(docP2).once();
    expect(xwiki.exists(eq(parentRef2), same(context))).andReturn(true).anyTimes();
    List<DocumentReference> docParentsList = Arrays.asList(parentRef1, parentRef2);
    replayDefault();
    assertEquals(docParentsList, xdocParents.getDocumentParentsList(docRef));
    verifyDefault();
  }

}
