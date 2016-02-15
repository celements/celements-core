package com.celements.filebase.matcher;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class AttNameWithoutExtMatcherTest extends AbstractBridgedComponentTestCase {

  private AttNameWithoutExtMatcher attNameWOextMatcher;

  @Before
  public void setUp_AttNameWithoutExtMatcherTest() {
    attNameWOextMatcher = (AttNameWithoutExtMatcher) Utils.getComponent(
        IAttFileNameMatcherRole.class,
        AttNameWithoutExtMatcher.ATT_NAME_WITHOUT_EXT_MATCHER);
  }

  @Test
  public void test_ATT_NAME_MATCH_WITHOUT_EXT() {
    Pattern defaultPattern = AttNameWithoutExtMatcher.ATT_NAME_MATCH_WITHOUT_EXT;
    String[] nameSplit = defaultPattern.split("image.jpg.zip");
    assertEquals(1, nameSplit.length);
    assertEquals("image.jpg", nameSplit[0]);
  }

//TODO implement "accept"
//  @Test
//  public void testAccept_match() {
//    DocumentReference docRef = new DocumentReference(getContext().getDatabase(),
//        "mySpace", "myDoc");
//    XWikiDocument doc = new XWikiDocument(docRef);
//    XWikiAttachment imgAtt = new XWikiAttachment(doc, "image.jpg");
//    attNameWOextMatcher.setFileNamePattern("image");
//    replayDefault();
//    assertTrue(attNameWOextMatcher.accept(imgAtt));
//    verifyDefault();
//  }
//
//  @Test
//  public void testAccept_noMatch() {
//    DocumentReference docRef = new DocumentReference(getContext().getDatabase(),
//        "mySpace", "myDoc");
//    XWikiDocument doc = new XWikiDocument(docRef);
//    XWikiAttachment imgAtt = new XWikiAttachment(doc, "image.jpg.zip");
//    attNameWOextMatcher.setFileNamePattern("image");
//    replayDefault();
//    assertFalse(attNameWOextMatcher.accept(imgAtt));
//    verifyDefault();
//  }
//
}
