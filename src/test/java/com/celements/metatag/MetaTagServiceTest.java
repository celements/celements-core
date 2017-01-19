package com.celements.metatag;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.metatag.enums.ECharset;
import com.celements.metatag.enums.ENameStandard;
import com.celements.metatag.enums.twitter.ETwitterCardType;
import com.xpn.xwiki.web.Utils;

public class MetaTagServiceTest extends AbstractComponentTest {

  private MetaTagServiceRole metaTag;

  @Before
  public void prepareTest() throws Exception {
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

}
