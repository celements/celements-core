package com.celements.metatag;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.metatag.enums.ECharset;
import com.celements.metatag.enums.ENameStandard;
import com.celements.metatag.enums.twitter.ETwitterCardType;
import com.xpn.xwiki.web.Utils;

public class MetaTagCollectorTest extends AbstractComponentTest {

  private MetaTagCollectorRole metaTag;

  @Before
  public void prepareTest() throws Exception {
    metaTag = Utils.getComponent(MetaTagCollectorRole.class);
  }

  @Test
  public void testDisplayAllMetaTags_empty() {
    assertEquals("", metaTag.displayAllMetaTags());
  }

  @Test
  public void testDisplayAllMetaTags_callOnce() {
    String keywords = "test,junit,keyword";
    metaTag.addMetaTag(new MetaTagApi(ENameStandard.KEYWORDS, keywords));
    metaTag.addMetaTag(new MetaTagApi(ETwitterCardType.SUMMARY));
    assertEquals("<meta name=\"keywords\" property=\"keywords\" content=\"" + keywords + "\" />"
        + "\n<meta name=\"twitter:card\" content=\"summary\" />\n", metaTag.displayAllMetaTags());
  }

  @Test
  public void testDisplayAllMetaTags_callRepeated() {
    String keywords = "test,junit,keyword";
    metaTag.addMetaTag(new MetaTagApi(ENameStandard.KEYWORDS, keywords));
    metaTag.addMetaTag(new MetaTagApi(ETwitterCardType.SUMMARY));
    assertEquals("<meta name=\"keywords\" property=\"keywords\" content=\"" + keywords + "\" />"
        + "\n<meta name=\"twitter:card\" content=\"summary\" />\n", metaTag.displayAllMetaTags());
    assertEquals("", metaTag.displayAllMetaTags());
    metaTag.addMetaTag(new MetaTagApi(ECharset.UTF8));
    assertEquals("<meta charset=\"UTF-8\" />\n", metaTag.displayAllMetaTags());
  }

}
