package com.celements.metatag;

import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.metatag.enums.ENameStandard;
import com.celements.metatag.enums.twitter.ETwitterCardType;

public class MetaTagApiTest extends AbstractComponentTest {

  @Before
  public void prepareTest() throws Exception {
  }

  @Test
  public void test_immutability() {
    assertImmutable(MetaTag.class);
  }

  @Test
  public void testDisplay_oneAttribute() {
    MetaTag tag = new MetaTag(ETwitterCardType.SUMMARY);
    assertEquals("<meta name=\"twitter:card\" content=\"summary\" />", tag.display());
  }

  @Test
  public void testDisplay_multipleAttributes() {
    String keywords = "test,junit,keyword";
    MetaTag tag = new MetaTag(ENameStandard.KEYWORDS, keywords);
    assertEquals("<meta name=\"keywords\" property=\"keywords\" content=\"" + keywords + "\" />",
        tag.display());
  }

}
