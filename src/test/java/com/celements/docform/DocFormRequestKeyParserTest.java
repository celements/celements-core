package com.celements.docform;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.xpn.xwiki.web.Utils;

public class DocFormRequestKeyParserTest extends AbstractComponentTest {

  private String db;
  private DocFormRequestKeyParser parser;

  @Before
  public void setUp_DocFormRequestKeyParserTest() throws Exception {
    db = getContext().getDatabase();
    parser = new DocFormRequestKeyParser();
  }

  @Test
  public void testParse_whitelist_content() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    String fieldName = "content";
    String keyString = serialize(docRef) + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString, null);
    assertKey(key, keyString, docRef, null, false, null, fieldName);
  }

  @Test
  public void testParse_whitelist_content_noFullName() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    String keyString = "content";
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, null, false, null, keyString);
  }

  @Test
  public void testParse_whitelist_title() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    String fieldName = "title";
    String keyString = serialize(docRef) + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString, null);
    assertKey(key, keyString, docRef, null, false, null, fieldName);
  }

  @Test
  public void testParse_whitelist_title_noFullName() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    String keyString = "title";
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, null, false, null, keyString);
  }

  @Test
  public void testParse_whitelist_other() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    String fieldName = "asdf";
    String keyString = serialize(docRef) + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    try {
      parser.parse(keyString, null);
      fail("expecting IllegalArgumentException, 'asdf' is not valid keyword");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_obj() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(docRef) + DocFormRequestKeyParser.KEY_DELIM + serialize(classRef)
        + DocFormRequestKeyParser.KEY_DELIM + objNb + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString, null);
    assertKey(key, keyString, docRef, classRef, false, objNb, fieldName);
  }

  @Test
  public void testParse_obj_noFullName() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(classRef) + DocFormRequestKeyParser.KEY_DELIM + objNb
        + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, classRef, false, objNb, fieldName);
  }

  @Test
  public void testParse_obj_local() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(docRef) + DocFormRequestKeyParser.KEY_DELIM + serialize(classRef)
        + DocFormRequestKeyParser.KEY_DELIM + objNb + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString, null);
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    docRef = new DocumentReference("xwikidb", docRef.getLastSpaceReference().getName(),
        docRef.getName());
    // IMPORTANT do not use setWikiReference, because it is dropped in xwiki 4.5.4
    classRef = new DocumentReference("xwikidb", classRef.getLastSpaceReference().getName(),
        classRef.getName());
    assertKey(key, keyString, docRef, classRef, false, objNb, fieldName);
  }

  @Test
  public void testParse_obj_longFieldName() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf_asdf2";
    String keyString = serialize(classRef) + DocFormRequestKeyParser.KEY_DELIM + objNb
        + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, classRef, false, objNb, fieldName);
  }

  @Test
  public void testParse_obj_create() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    Integer objNb = -3;
    String fieldName = "asdf";
    String keyString = serialize(classRef) + DocFormRequestKeyParser.KEY_DELIM + objNb
        + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, classRef, false, objNb, fieldName);
  }

  @Test
  public void testParse_obj_delete() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(classRef) + DocFormRequestKeyParser.KEY_DELIM + "^" + objNb
        + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, classRef, true, objNb, fieldName);
  }

  @Test
  public void testParse_obj_delete_noFieldName() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    Integer objNb = 3;
    String keyString = serialize(classRef) + DocFormRequestKeyParser.KEY_DELIM + "^" + objNb
        + DocFormRequestKeyParser.KEY_DELIM;
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, classRef, true, objNb, "");
  }

  @Test
  public void testParse_obj_delete_negative() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    Integer objNb = -3;
    String keyString = serialize(classRef) + DocFormRequestKeyParser.KEY_DELIM + "^" + objNb;
    try {
      parser.parse(keyString, docRef);
      fail("expecting IllegalArgumentException, delete and create not allowed together");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_obj_invalid_fullName() {
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = "SpaceDoc" + DocFormRequestKeyParser.KEY_DELIM + serialize(classRef)
        + DocFormRequestKeyParser.KEY_DELIM + objNb + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    try {
      parser.parse(keyString, null);
      fail("expecting IllegalArgumentException, invalid fullName");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_obj_invalid_className() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(docRef) + DocFormRequestKeyParser.KEY_DELIM + "ClassesClass"
        + DocFormRequestKeyParser.KEY_DELIM + objNb + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    try {
      parser.parse(keyString, docRef);
      fail("expecting IllegalArgumentException, invalid fullName");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_obj_invalid_objNb() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    String fieldName = "asdf";
    String keyString = serialize(docRef) + DocFormRequestKeyParser.KEY_DELIM + serialize(classRef)
        + DocFormRequestKeyParser.KEY_DELIM + "3a" + DocFormRequestKeyParser.KEY_DELIM + fieldName;
    try {
      parser.parse(keyString, docRef);
      fail("expecting IllegalArgumentException, invalid fullName");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_obj_invalid_fieldName() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    Integer objNb = 3;
    String keyString = serialize(docRef) + DocFormRequestKeyParser.KEY_DELIM + serialize(classRef)
        + DocFormRequestKeyParser.KEY_DELIM + objNb + DocFormRequestKeyParser.KEY_DELIM;
    try {
      parser.parse(keyString, docRef);
      fail("expecting IllegalArgumentException, invalid fullName");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_skip_blank() {
    String keyString = "";
    assertNull(parser.parse(keyString, null));
  }

  @Test
  public void testParse_skip_template() {
    String keyString = "template";
    assertNull(parser.parse(keyString, null));
  }

  @Test
  public void testParse_skip_withDelim() {
    String keyString = "template" + DocFormRequestKeyParser.KEY_DELIM + "other";
    assertNull(parser.parse(keyString, null));
  }

  @Test
  public void testParse_skip_withDelims_two() {
    String keyString = "template" + DocFormRequestKeyParser.KEY_DELIM + "other1"
        + DocFormRequestKeyParser.KEY_DELIM + "other2";
    assertNull(parser.parse(keyString, null));
  }

  @Test
  public void testParse_skip_withDelims_multiple() {
    String keyString = "template" + DocFormRequestKeyParser.KEY_DELIM + "other1"
        + DocFormRequestKeyParser.KEY_DELIM + "other2" + DocFormRequestKeyParser.KEY_DELIM
        + "other3" + DocFormRequestKeyParser.KEY_DELIM + "other4"
        + DocFormRequestKeyParser.KEY_DELIM + "other5";
    assertNull(parser.parse(keyString, null));
  }

  @Test
  public void testParse_skip_nb() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    String keyString = serialize(classRef) + DocFormRequestKeyParser.KEY_DELIM + "nb";
    assertNull(parser.parse(keyString, docRef));
  }

  @Test
  public void testParse_multiple() {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    DocumentReference classRef = new DocumentReference(db, "Classes", "Class");
    Integer objNb1 = 3;
    String keyString = serialize(classRef) + DocFormRequestKeyParser.KEY_DELIM + "^" + objNb1;
    String keyString1 = serialize(classRef) + DocFormRequestKeyParser.KEY_DELIM + objNb1
        + DocFormRequestKeyParser.KEY_DELIM + "asdf1";
    String keyString2 = serialize(classRef) + DocFormRequestKeyParser.KEY_DELIM + objNb1
        + DocFormRequestKeyParser.KEY_DELIM + "asdf2";
    String keyString3 = serialize(classRef) + DocFormRequestKeyParser.KEY_DELIM + objNb1
        + DocFormRequestKeyParser.KEY_DELIM + "asdf3";
    String keyStringContent = "content";
    DocumentReference classRef2 = new DocumentReference(db, "Classes", "OtherClass");
    Integer objNb2 = 5;
    String fieldName2 = "asdf";
    String keyStringOther = serialize(classRef2) + DocFormRequestKeyParser.KEY_DELIM + objNb2
        + DocFormRequestKeyParser.KEY_DELIM + fieldName2;

    Collection<DocFormRequestKey> keys = parser.parse(Arrays.asList(keyString1, keyString2,
        keyString, keyString3, keyStringContent, keyStringOther), docRef);

    assertEquals(3, keys.size());
    Iterator<DocFormRequestKey> iter = keys.iterator();
    assertKey(iter.next(), keyString, docRef, classRef, true, objNb1, "");
    assertKey(iter.next(), keyStringContent, docRef, null, false, null, keyStringContent);
    assertKey(iter.next(), keyStringOther, docRef, classRef2, false, objNb2, fieldName2);
  }

  @Test
  public void test_parse_specialFN() {
    String delim = DocFormRequestKeyParser.KEY_DELIM;
    String docName = "2019-07-15";
    String docSpace = "TimeSheets-ebeutler10";
    String docFN = docSpace + "." + docName;
    DocumentReference docRef = RefBuilder.create().wiki(getContext().getDatabase()).doc(
        docName).space(docSpace).build(DocumentReference.class);
    String classFN = "TimeSheetClasses.TimesheetDayClass";
    DocumentReference classRef = RefBuilder.create().wiki(getContext().getDatabase()).space(
        "TimeSheetClasses").doc("TimesheetDayClass").build(DocumentReference.class);
    String fieldName = "dailyComment";
    String keyString = docFN + delim + classFN + delim + "0" + delim + fieldName;
    DocFormRequestKey result = parser.parse(keyString, null);
    assertEquals(docRef, result.getDocRef());
    assertEquals(classRef, result.getClassRef());
    assertEquals(fieldName, result.getFieldName());
    assertEquals(Integer.valueOf(0), result.getObjNb());
  }

  private void assertKey(DocFormRequestKey key, String keyString, DocumentReference docRef,
      DocumentReference classRef, boolean remove, Integer objNb, String fieldName) {
    assertEquals(keyString, key.getKeyString());
    assertEquals(docRef, key.getDocRef());
    assertEquals(classRef, key.getClassRef());
    assertSame(remove, key.isRemove());
    assertEquals(objNb, key.getObjNb());
    assertEquals(fieldName, key.getFieldName());
  }

  private String serialize(DocumentReference docRef) {
    return Utils.getComponent(ModelUtils.class).serializeRefLocal(docRef);
  }

}
