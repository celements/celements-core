package com.celements.metatag;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.metatag.enums.ECharset;
import com.celements.metatag.enums.ENameStandard;
import com.celements.metatag.enums.twitter.ETwitterCardType;
import com.celements.model.access.IModelAccessFacade;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class MetaTagServiceTest extends AbstractComponentTest {

  private IModelAccessFacade modelAccess;
  private MetaTagServiceRole metaTag;
  private MetaTagProviderRole headerTag;

  @Before
  public void prepareTest() throws Exception {
    modelAccess = registerComponentMock(IModelAccessFacade.class);
    headerTag = registerComponentMock(MetaTagProviderRole.class);
    metaTag = Utils.getComponent(MetaTagServiceRole.class);
  }

  @Test
  public void testDisplayCollectedMetaTags_empty() {
    assertEquals("", metaTag.displayCollectedMetaTags());
  }

  @Test
  public void testDisplayCollectedMetaTags_callOnce() {
    String keywords = "test,junit,keyword";
    metaTag.addMetaTagToCollector(new MetaTag(ENameStandard.KEYWORDS, keywords));
    metaTag.addMetaTagToCollector(new MetaTag(ETwitterCardType.SUMMARY));
    assertEquals("<meta name=\"keywords\" property=\"keywords\" content=\"" + keywords + "\" />"
        + "\n<meta name=\"twitter:card\" content=\"summary\" />\n",
        metaTag.displayCollectedMetaTags());
  }

  @Test
  public void testDisplayCollectedMetaTags_callRepeated() {
    String keywords = "test,junit,keyword";
    metaTag.addMetaTagToCollector(new MetaTag(ENameStandard.KEYWORDS, keywords));
    metaTag.addMetaTagToCollector(new MetaTag(ETwitterCardType.SUMMARY));
    assertEquals("<meta name=\"keywords\" property=\"keywords\" content=\"" + keywords + "\" />"
        + "\n<meta name=\"twitter:card\" content=\"summary\" />\n",
        metaTag.displayCollectedMetaTags());
    assertEquals("", metaTag.displayCollectedMetaTags());
    metaTag.addMetaTagToCollector(new MetaTag(ECharset.UTF8));
    assertEquals("<meta charset=\"UTF-8\" />\n", metaTag.displayCollectedMetaTags());
  }

  @Test
  public void testCollectHeaderTags() throws Exception {
    String keywords = "test,junit,keyword";
    List<MetaTag> tags = Arrays.asList(new MetaTag(ENameStandard.KEYWORDS, keywords), new MetaTag(
        ETwitterCardType.SUMMARY));
    expect(headerTag.getHeaderMetaTags()).andReturn(tags);
    expect(getWikiMock().exists((DocumentReference) anyObject(), same(getContext()))).andReturn(
        false).anyTimes();
    expect(modelAccess.getOrCreateDocument((DocumentReference) anyObject())).andReturn(
        new XWikiDocument(new DocumentReference(getContext().getDatabase(), "Any", "Any")))
        .anyTimes();
    replayDefault();
    metaTag.collectHeaderTags();
    verifyDefault();
    assertEquals("<meta name=\"keywords\" property=\"keywords\" content=\"" + keywords + "\" />"
        + "\n<meta name=\"twitter:card\" content=\"summary\" />\n",
        metaTag.displayCollectedMetaTags());
  }

  @Test
  public void testCollectBodyTags() {
    String keywords = "test,junit,keyword";
    List<MetaTag> tags = Arrays.asList(new MetaTag(ENameStandard.KEYWORDS, keywords), new MetaTag(
        ETwitterCardType.SUMMARY));
    expect(headerTag.getBodyMetaTags()).andReturn(tags);
    replayDefault();
    metaTag.collectBodyTags();
    verifyDefault();
    assertEquals("<meta name=\"keywords\" property=\"keywords\" content=\"" + keywords + "\" />"
        + "\n<meta name=\"twitter:card\" content=\"summary\" />\n",
        metaTag.displayCollectedMetaTags());
  }

}
