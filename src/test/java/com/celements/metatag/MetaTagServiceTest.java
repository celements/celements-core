package com.celements.metatag;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.metatag.enums.ECharset;
import com.celements.metatag.enums.ENameStandard;
import com.celements.metatag.enums.twitter.ETwitterCardType;
import com.xpn.xwiki.web.Utils;

public class MetaTagServiceTest extends AbstractComponentTest {

  private MetaTagServiceRole metaTag;
  private MetaTagProviderRole headerTag;

  @Before
  public void prepareTest() throws Exception {
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
  public void testLoadHeaderTags() {
    String keywords = "test,junit,keyword";
    List<MetaTag> tags = Arrays.asList(new MetaTag(ENameStandard.KEYWORDS, keywords), new MetaTag(
        ETwitterCardType.SUMMARY));
    expect(headerTag.getHeaderMetaTags()).andReturn(tags);
    replayDefault();
    metaTag.collectHeaderTags();
    verifyDefault();
    assertEquals("<meta name=\"keywords\" property=\"keywords\" content=\"" + keywords + "\" />"
        + "\n<meta name=\"twitter:card\" content=\"summary\" />\n",
        metaTag.displayCollectedMetaTags());
  }

  @Test
  public void testLoadBodyTags() {
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
