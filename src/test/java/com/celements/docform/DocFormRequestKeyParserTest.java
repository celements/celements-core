package com.celements.docform;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.web.Utils;

public class DocFormRequestKeyParserTest extends AbstractBridgedComponentTestCase {

  private DocFormRequestKeyParser parser;

  @Before
  public void setUp_DocFormRequestKeyParserTest() throws Exception {
    parser = new DocFormRequestKeyParser();
  }

  @Test
  public void testParse_blank() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    String keyString = "";
    try {
      parser.parse(keyString, docRef);
      fail("expecting IllegalArgumentException, blank key is not valid");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_keyword_content() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    String fieldName = "content";
    String keyString = serialize(docRef, false) + "_" + fieldName;
    DocFormRequestKey key = parser.parse(keyString, null);
    assertKey(key, keyString, docRef, null, false, null, fieldName);
  }

  @Test
  public void testParse_keyword_title() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    String fieldName = "title";
    String keyString = serialize(docRef, false) + "_" + fieldName;
    DocFormRequestKey key = parser.parse(keyString, null);
    assertKey(key, keyString, docRef, null, false, null, fieldName);
  }

  @Test
  public void testParse_keyword_noFullName() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    String keyString = "content";
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, null, false, null, keyString);
  }

  @Test
  public void testParse_keyword_other() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    String fieldName = "asdf";
    String keyString = serialize(docRef, false) + "_" + fieldName;
    try {
      parser.parse(keyString, null);
      fail("expecting IllegalArgumentException, 'asdf' is not valid keyword");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_obj() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(docRef, false) + "_" + serialize(classRef, false) + "_" 
        + objNb + "_" + fieldName;
    DocFormRequestKey key = parser.parse(keyString, null);
    assertKey(key, keyString, docRef, classRef, false, objNb, fieldName);
  }

  @Test
  public void testParse_obj_noFullName() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(classRef, false) + "_" + objNb + "_" + fieldName;
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, classRef, false, objNb, fieldName);
  }

  @Test
  public void testParse_obj_local() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(docRef, true) + "_" + serialize(classRef, true) + "_" 
        + objNb + "_" + fieldName;
    DocFormRequestKey key = parser.parse(keyString, null);
    docRef.setWikiReference(new WikiReference("xwikidb"));
    classRef.setWikiReference(new WikiReference("xwikidb"));
    assertKey(key, keyString, docRef, classRef, false, objNb, fieldName);
  }

  @Test
  public void testParse_obj_longFieldName() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf_asdf2";
    String keyString = serialize(classRef, false) + "_" + objNb + "_" + fieldName;
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, classRef, false, objNb, fieldName);
  }

  @Test
  public void testParse_obj_create() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    Integer objNb = -3;
    String fieldName = "asdf";
    String keyString = serialize(classRef, false) + "_" + objNb + "_" + fieldName;
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, classRef, false, objNb, fieldName);
  }

  @Test
  public void testParse_obj_delete() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(classRef, false) + "_^" + objNb + "_" + fieldName;
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, classRef, true, objNb, fieldName);
  }

  @Test
  public void testParse_obj_delete_noFieldName() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    Integer objNb = 3;
    String keyString = serialize(classRef, false) + "_^" + objNb + "_";
    DocFormRequestKey key = parser.parse(keyString, docRef);
    assertKey(key, keyString, docRef, classRef, true, objNb, "");
  }

  @Test
  public void testParse_obj_delete_negative() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    Integer objNb = -3;
    String keyString = serialize(classRef, false) + "_^" + objNb;
    try {
      parser.parse(keyString, docRef);
      fail("expecting IllegalArgumentException, delete and create not allowed together");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_obj_invalid_fullName() {
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = "Wiki:SpaceDoc_" + serialize(classRef, false) + "_" + objNb + "_" 
        + fieldName;
    try {
      parser.parse(keyString, null);
      fail("expecting IllegalArgumentException, invalid fullName");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_obj_invalid_className() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    Integer objNb = 3;
    String fieldName = "asdf";
    String keyString = serialize(docRef, false) + "_Wiki:ClassesClass_"  + objNb + "_" 
        + fieldName;
    try {
      parser.parse(keyString, docRef);
      fail("expecting IllegalArgumentException, invalid fullName");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_obj_invalid_objNb() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    String fieldName = "asdf";
    String keyString = serialize(docRef, false) + "_" + serialize(classRef, false) 
        + "_3a_" + fieldName;
    try {
      parser.parse(keyString, docRef);
      fail("expecting IllegalArgumentException, invalid fullName");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_obj_invalid_fieldName() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    Integer objNb = 3;
    String keyString = serialize(docRef, false) + "_" + serialize(classRef, false) + "_" 
        + objNb + "_";
    try {
      parser.parse(keyString, docRef);
      fail("expecting IllegalArgumentException, invalid fullName");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  public void testParse_multiple() {
    DocumentReference docRef = new DocumentReference("Wiki", "Space", "Doc");    
    DocumentReference classRef = new DocumentReference("Wiki", "Classes", "Class");
    Integer objNb1 = 3;
    String keyString = serialize(classRef, false) + "_^" + objNb1;
    String keyString1 = serialize(classRef, false) + "_" + objNb1 + "_asdf1";
    String keyString2 = serialize(classRef, false) + "_" + objNb1 + "_asdf2";
    String keyString3 = serialize(classRef, false) + "_" + objNb1 + "_asdf3";
    String keyStringContent = "content";
    DocumentReference classRef2 = new DocumentReference("Wiki", "Classes", "OtherClass");
    Integer objNb2 = 5;
    String fieldName2 = "asdf";
    String keyStringOther = serialize(classRef2, false) + "_" + objNb2 + "_" + fieldName2;
    
    Collection<DocFormRequestKey> keys = parser.parse(Arrays.asList(keyString1, 
        keyString2, keyString, keyString3, keyStringContent, keyStringOther), docRef);
    
    assertEquals(3, keys.size());
    Iterator<DocFormRequestKey> iter = keys.iterator();
    assertKey(iter.next(), keyString, docRef, classRef, true, objNb1, "");
    assertKey(iter.next(), keyStringContent, docRef, null, false, null, keyStringContent);
    assertKey(iter.next(), keyStringOther, docRef, classRef2, false, objNb2, fieldName2);
  }

  private void assertKey(DocFormRequestKey key, String keyString, DocumentReference docRef,
      DocumentReference classRef, boolean remove, Integer objNb, String  fieldName) {
    assertEquals(keyString, key.getKeyString());
    assertEquals(docRef, key.getDocRef());
    assertEquals(classRef, key.getClassRef());
    assertSame(remove, key.isRemove());
    assertEquals(objNb, key.getObjNb());
    assertEquals(fieldName, key.getFieldName());
  }

  private String serialize(DocumentReference docRef, boolean local) {
    EntityReferenceSerializer<String> serializer;
    if (local) {
      serializer = Utils.getComponent(IWebUtilsService.class).getRefLocalSerializer();
    } else {
      serializer = Utils.getComponent(IWebUtilsService.class).getRefDefaultSerializer();
    }
    return serializer.serialize(docRef);
  }
  
}
