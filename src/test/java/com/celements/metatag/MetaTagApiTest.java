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
    assertImmutable(MetaTagApi.class);
  }

  @Test
  public void testDisplay_oneAttribute() {
    MetaTagApi tag = new MetaTagApi(ETwitterCardType.SUMMARY);
    assertEquals("<meta name=\"twitter:card\" content=\"summary\" />", tag.display());
  }

  @Test
  public void testDisplay_multipleAttributes() {
    String keywords = "test,junit,keyword";
    MetaTagApi tag = new MetaTagApi(ENameStandard.KEYWORDS, keywords);
    assertEquals("<meta name=\"keywords\" property=\"keywords\" content=\"" + keywords + "\" />",
        tag.display());
  }

}
