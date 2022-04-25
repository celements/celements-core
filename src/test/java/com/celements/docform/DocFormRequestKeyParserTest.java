package com.celements.docform;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.docform.DocFormRequestKeyParser.*;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.docform.DocFormRequestKeyParser.DocFormRequestParseException;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.web.Utils;

public class DocFormRequestKeyParserTest extends AbstractComponentTest {

  private String db;
  private DocumentReference defaultDocRef;
  private DocFormRequestKeyParser parser;

  @Before
  public void prepare() throws Exception {
    db = getContext().getDatabase();
    defaultDocRef = RefBuilder.create().wiki(db).space("space").doc("DefaultDoc")
        .build(DocumentReference.class);
    parser = new DocFormRequestKeyParser(defaultDocRef);
  }

  @Test
  public void test_parse_whitelist_content() throws Exception {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    String fieldName = "content";
    String keyString = serialize(docRef) + KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    assertKey(key, keyString, docRef, null, 0, false, fieldName);
  }

  @Test
  public void test_parse_whitelist_content_noFullName() throws Exception {
    String keyString = "content";
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    assertKey(key, keyString, defaultDocRef, null, 0, false, keyString);
  }

  @Test
  public void test_parse_whitelist_title() throws Exception {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    String fieldName = "title";
    String keyString = serialize(docRef) + KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    assertKey(key, keyString, docRef, null, 0, false, fieldName);
  }

  @Test
  public void test_parse_whitelist_title_noFullName() throws Exception {
    String keyString = "title";
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    assertKey(key, keyString, defaultDocRef, null, 0, false, keyString);
  }

  @Test
  public void test_parse_language() throws Exception {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    String fieldName = "language";
    String keyString = serialize(docRef) + KEY_DELIM + fieldName;
    assertFalse(parser.parse(keyString).isPresent());
  }

  @Test
  public void test_parse_obj() throws Exception {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    ClassReference classRef = new ClassReference("Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(docRef) + KEY_DELIM + serialize(classRef)
        + KEY_DELIM + objNb + KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    assertKey(key, keyString, docRef, classRef, objNb, false, fieldName);
  }

  @Test
  public void test_parse_obj_noFullName() throws Exception {
    ClassReference classRef = new ClassReference("Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(classRef) + KEY_DELIM + objNb
        + KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    assertKey(key, keyString, defaultDocRef, classRef, objNb, false, fieldName);
  }

  @Test
  public void test_parse_obj_local() throws Exception {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    ClassReference classRef = new ClassReference("Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(docRef) + KEY_DELIM + serialize(classRef)
        + KEY_DELIM + objNb + KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    docRef = RefBuilder.from(docRef).wiki("xwikidb").build(DocumentReference.class);
    assertKey(key, keyString, docRef, classRef, objNb, false, fieldName);
  }

  @Test
  public void test_parse_obj_longFieldName() throws Exception {
    ClassReference classRef = new ClassReference("Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf_asdf2";
    String keyString = serialize(classRef) + KEY_DELIM + objNb
        + KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    assertKey(key, keyString, defaultDocRef, classRef, objNb, false, fieldName);
  }

  @Test
  public void test_parse_obj_create() throws Exception {
    ClassReference classRef = new ClassReference("Classes", "Class");
    Integer objNb = -3;
    String fieldName = "asdf";
    String keyString = serialize(classRef) + KEY_DELIM + objNb
        + KEY_DELIM + fieldName;
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    assertKey(key, keyString, defaultDocRef, classRef, objNb, false, fieldName);
  }

  @Test
  public void test_parse_obj_remove() throws Exception {
    ClassReference classRef = new ClassReference("Classes", "Class");
    Integer objNb = 3;
    String keyString = serialize(classRef) + KEY_DELIM + "^" + objNb + KEY_DELIM + "asdf";
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    assertKey(key, keyString, defaultDocRef, classRef, objNb, true, "");
  }

  @Test
  public void test_parse_obj_remove_noFieldName() throws Exception {
    ClassReference classRef = new ClassReference("Classes", "Class");
    Integer objNb = 3;
    String keyString = serialize(classRef) + KEY_DELIM + "^" + objNb;
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    assertKey(key, keyString, defaultDocRef, classRef, objNb, true, "");
  }

  @Test(expected = DocFormRequestParseException.class)
  public void test_parse_obj_remove_negative() throws Exception {
    ClassReference classRef = new ClassReference("Classes", "Class");
    Integer objNb = -3;
    String keyString = serialize(classRef) + KEY_DELIM + "^" + objNb;
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    fail("expecting DocFormRequestParseException, delete and create not allowed together: " + key);
  }

  @Test(expected = DocFormRequestParseException.class)
  public void test_parse_obj_noFieldName() throws Exception {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    ClassReference classRef = new ClassReference("Classes", "Class");
    Integer objNb = 3;
    String keyString = serialize(docRef) + KEY_DELIM + serialize(classRef)
        + KEY_DELIM + objNb;
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    fail("expecting DocFormRequestParseException, obj with positive nb must have field: " + key);
  }

  @Test
  public void test_parse_skip_doc_invalidField() throws Exception {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    String fieldName = "asdf";
    String keyString = serialize(docRef) + KEY_DELIM + fieldName;
    assertFalse(parser.parse(keyString).isPresent());
  }

  @Test
  public void test_parse_skip_invalidFullName() throws Exception {
    ClassReference classRef = new ClassReference("Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = "SpaceDoc" + KEY_DELIM + serialize(classRef)
        + KEY_DELIM + objNb + KEY_DELIM + fieldName;
    assertFalse(parser.parse(keyString).isPresent());
  }

  @Test
  public void test_parse_skip_invalidClassName() throws Exception {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(docRef) + KEY_DELIM + "ClassesClass"
        + KEY_DELIM + objNb + KEY_DELIM + fieldName;
    assertFalse(parser.parse(keyString).isPresent());
  }

  @Test
  public void test_parse_skip_noObjNb() throws Exception {
    DocumentReference docRef = new DocumentReference(db, "Space", "Doc");
    ClassReference classRef = new ClassReference("Classes", "Class");
    String fieldName = "asdf";
    String keyString = serialize(docRef) + KEY_DELIM + serialize(classRef)
        + KEY_DELIM + "3a" + KEY_DELIM + fieldName;
    assertFalse(parser.parse(keyString).isPresent());
  }

  @Test
  public void test_parse_skip_blank() throws Exception {
    String keyString = "";
    assertFalse(parser.parse(keyString).isPresent());
  }

  @Test
  public void test_parse_skip_template() throws Exception {
    String keyString = "template";
    assertFalse(parser.parse(keyString).isPresent());
  }

  @Test
  public void test_parse_skip_withDelim() throws Exception {
    String keyString = "template" + KEY_DELIM + "other";
    assertFalse(parser.parse(keyString).isPresent());
  }

  @Test
  public void test_parse_skip_withDelims_two() throws Exception {
    String keyString = "template" + KEY_DELIM + "other1"
        + KEY_DELIM + "other2";
    assertFalse(parser.parse(keyString).isPresent());
  }

  @Test
  public void test_parse_skip_withDelims_multiple() throws Exception {
    String keyString = "template" + KEY_DELIM + "other1" + KEY_DELIM + "other2" + KEY_DELIM
        + "other3" + KEY_DELIM + "other4" + KEY_DELIM + "other5";
    assertFalse(parser.parse(keyString).isPresent());
  }

  @Test
  public void test_parse_skip_nb() throws Exception {
    ClassReference classRef = new ClassReference("Classes", "Class");
    String keyString = serialize(classRef) + KEY_DELIM + "nb";
    assertFalse(parser.parse(keyString).isPresent());
  }

  @Test
  public void test_parse_multiple() throws Exception {
    ClassReference classRef = new ClassReference("Classes", "Class");
    Integer objNb1 = 3;
    String keyStringRemove = serialize(classRef) + KEY_DELIM + "^" + objNb1;
    String keyString1 = serialize(classRef) + KEY_DELIM + objNb1 + KEY_DELIM + "asdf1";
    String keyString2 = serialize(classRef) + KEY_DELIM + objNb1 + KEY_DELIM + "asdf2";
    String keyStringContent = "content";
    ClassReference classRef2 = new ClassReference("Classes", "OtherClass");
    Integer objNb2 = 5;
    String fieldName2 = "asdf";
    String keyStringClass2 = serialize(classRef2) + KEY_DELIM + objNb2 + KEY_DELIM + fieldName2;
    Map<String, ?> requestMap = ImmutableMap.<String, Object>builder()
        .put(keyString2, "val2")
        .put(keyStringRemove, "valRemove")
        .put(keyString1, "val1")
        .put(keyStringContent, "valContent")
        .put(keyStringClass2, "val3")
        .build();
    List<DocFormRequestParam> params = parser.parseParameterMap(requestMap);

    assertEquals(5, params.size());
    Iterator<DocFormRequestParam> iter = params.iterator();
    assertParam(iter.next(), keyString1, defaultDocRef,
        classRef, objNb1, false, "asdf1", requestMap);
    assertParam(iter.next(), keyString2, defaultDocRef,
        classRef, objNb1, false, "asdf2", requestMap);
    assertParam(iter.next(), keyStringRemove, defaultDocRef,
        classRef, objNb1, true, "", requestMap);
    assertParam(iter.next(), keyStringClass2, defaultDocRef,
        classRef2, objNb2, false, fieldName2, requestMap);
    assertParam(iter.next(), keyStringContent, defaultDocRef,
        null, 0, false, keyStringContent, requestMap);
  }

  @Test
  public void test_parse_specialFN() throws Exception {
    String delim = KEY_DELIM;
    String docName = "2019-07-15";
    String docSpace = "TimeSheets-ebeutler10";
    String docFN = docSpace + "." + docName;
    DocumentReference docRef = RefBuilder.create().wiki(getContext().getDatabase()).doc(
        docName).space(docSpace).build(DocumentReference.class);
    String classFN = "TimeSheetClasses.TimesheetDayClass";
    ClassReference classRef = new ClassReference("TimeSheetClasses", "TimesheetDayClass");
    String fieldName = "dailyComment";
    String keyString = docFN + delim + classFN + delim + "0" + delim + fieldName;
    DocFormRequestKey key = parser.parse(keyString).orElse(null);
    assertKey(key, keyString, docRef, classRef, 0, false, fieldName);
  }

  private void assertKey(DocFormRequestKey key, String keyString, DocumentReference docRef,
      ClassReference classRef, int objNb, boolean remove, String fieldName) {
    assertNotNull(key);
    assertEquals(keyString, key.getKeyString());
    assertEquals(docRef, key.getDocRef());
    assertEquals(classRef, key.getClassRef());
    assertSame(remove, key.isRemove());
    assertEquals(objNb, key.getObjNb());
    assertEquals(fieldName, key.getFieldName());
  }

  private void assertParam(DocFormRequestParam param, String keyString, DocumentReference docRef,
      ClassReference classRef, int objNb, boolean remove, String fieldName,
      Map<String, ?> requestMap) {
    assertKey(param.getKey(), keyString, docRef, classRef, objNb, remove, fieldName);
    assertEquals(param.getValuesAsString(), requestMap.get(keyString));
  }

  private String serialize(EntityReference ref) {
    return Utils.getComponent(ModelUtils.class).serializeRefLocal(ref);
  }

}
