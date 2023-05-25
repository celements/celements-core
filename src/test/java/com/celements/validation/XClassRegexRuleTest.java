package com.celements.validation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.docform.DocFormRequestKeyParser;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

public class XClassRegexRuleTest extends AbstractComponentTest {

  private XClassRegexRule xClassRegexRule;
  private DocumentReference bclassDocRef;
  private DocFormRequestKeyParser parser;

  @Before
  public void prepare() throws Exception {
    xClassRegexRule = (XClassRegexRule) Utils.getComponent(IRequestValidationRule.class,
        "XClassRegexValidation");
    bclassDocRef = new DocumentReference(getContext().getDatabase(), "Test", "TestClass");
    parser = new DocFormRequestKeyParser(new DocumentReference(getContext().getDatabase(),
        "space", "default"));
  }

  @Test
  public void test_validate_empty() throws XWikiException {
    replayDefault();
    List<ValidationResult> result = xClassRegexRule.validate(ImmutableList.of());
    verifyDefault();

    assertTrue("Successful validation should result in an empty map", (result != null)
        && result.isEmpty());
  }

  @Test
  public void test_validate_valid() throws XWikiException {
    BaseClass bclass = getBaseClass("testField");
    DocumentReference bclassDocRef1 = new DocumentReference(getContext().getDatabase(), "Test",
        "TestClass1");
    DocumentReference bclassDocRef2 = new DocumentReference(getContext().getDatabase(), "Test",
        "TestClass2");
    XWikiDocument doc1 = new XWikiDocument(bclassDocRef1);
    XWikiDocument doc2 = new XWikiDocument(bclassDocRef2);
    doc1.setXClass(bclass);
    doc2.setXClass(bclass);

    Map<String, String[]> requestMap = new HashMap<>();
    String param1 = "Test.TestClass1_0_testField";
    String param2 = "Test.TestClass2_0_testField";
    requestMap.put(param1, new String[] { "value1" });
    requestMap.put(param2, new String[] { "value2", "asdf" });

    expect(getWikiMock().getDocument(eq(bclassDocRef1), same(getContext()))).andReturn(doc1).once();
    expect(getWikiMock().getDocument(eq(bclassDocRef2), same(getContext()))).andReturn(doc2)
        .times(2);

    replayDefault();
    List<ValidationResult> result = xClassRegexRule.validate(parser.parseParameterMap(requestMap));
    verifyDefault();

    assertTrue("Successful validation should result in an empty map", (result != null)
        && result.isEmpty());
  }

  @Test
  public void test_validate_invalid() throws XWikiException {
    BaseClass bclass = getBaseClass("testField");
    DocumentReference bclassDocRef1 = new DocumentReference(getContext().getDatabase(), "Test",
        "TestClass1");
    DocumentReference bclassDocRef2 = new DocumentReference(getContext().getDatabase(), "Test",
        "TestClass2");
    XWikiDocument doc1 = new XWikiDocument(bclassDocRef1);
    XWikiDocument doc2 = new XWikiDocument(bclassDocRef2);
    doc1.setXClass(bclass);
    doc2.setXClass(bclass);

    Map<String, String[]> requestMap = new HashMap<>();
    String param1 = "Test.TestClass1_0_testField";
    String param2 = "Test.TestClass2_0_testField";
    requestMap.put(param1, new String[0]);
    requestMap.put(param2, new String[] { "", "" });

    expect(getWikiMock().getDocument(eq(bclassDocRef1), same(getContext()))).andReturn(doc1);
    expect(getWikiMock().getDocument(eq(bclassDocRef2), same(getContext()))).andReturn(doc2);

    replayDefault();
    List<ValidationResult> result = xClassRegexRule.validate(parser.parseParameterMap(requestMap));
    verifyDefault();

    assertNotNull(result);
    assertEquals(2, result.size());
    ValidationResult res1 = result.get(0);
    assertNotNull(res1);
    assertEquals(param1, res1.getName());
    assertEquals("testValMsg", res1.getMessage());
    assertEquals(ValidationType.ERROR, res1.getType());
    ValidationResult res2 = result.get(1);
    assertNotNull(res2);
    assertEquals(param2, res2.getName());
    assertEquals("testValMsg", res2.getMessage());
    assertEquals(ValidationType.ERROR, res2.getType());
  }

  @Test
  public void test_validate_illegalType() throws XWikiException {
    Map<String, String[]> requestMap = new HashMap<>();
    requestMap.put("title", new String[] { "value" }); // Type.DOC_FIELD
    requestMap.put("Test.TestClass_^0_toDel", new String[] { "value" }); // Type.OBJ_REMOVE

    replayDefault();
    List<ValidationResult> result = xClassRegexRule.validate(parser.parseParameterMap(requestMap));
    verifyDefault();

    assertTrue("Successful validation should result in an empty map", (result != null)
        && result.isEmpty());
  }

  @Test
  public void test_validateField_valid() throws XWikiException {
    BaseClass bclass = getBaseClass("testField");
    XWikiDocument doc = new XWikiDocument(bclassDocRef);
    doc.setXClass(bclass);

    expect(getWikiMock().getDocument(eq(bclassDocRef), same(getContext()))).andReturn(doc).once();

    replayDefault();
    Map<ValidationType, Set<String>> result = xClassRegexRule.validateField("Test.TestClass",
        "testField", "value");
    verifyDefault();

    assertNull("Successful validation should result in null", result);
  }

  @Test
  public void test_validateField_invalid() throws XWikiException {
    BaseClass bclass = getBaseClass("testField");
    XWikiDocument doc = new XWikiDocument(bclassDocRef);
    doc.setXClass(bclass);

    expect(getWikiMock().getDocument(eq(bclassDocRef), same(getContext()))).andReturn(doc).once();

    replayDefault();
    Map<ValidationType, Set<String>> result = xClassRegexRule.validateField("Test.TestClass",
        "testField", "");
    verifyDefault();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.containsKey(ValidationType.ERROR));
    assertEquals(1, result.get(ValidationType.ERROR).size());
    assertEquals("testValMsg", result.get(ValidationType.ERROR).iterator().next());
  }

  @Test
  public void test_validateField_invalidKey_class() throws XWikiException {
    XWikiDocument doc = new XWikiDocument(bclassDocRef);

    expect(getWikiMock().getDocument(eq(bclassDocRef), same(getContext()))).andReturn(doc).once();

    replayDefault();
    Map<ValidationType, Set<String>> result = xClassRegexRule.validateField("Test.TestClass",
        "testField", "");
    verifyDefault();

    assertNull("Not existing class should be ignored by default", result);
  }

  @Test
  public void test_validateField_invalidKey_field() throws XWikiException {
    BaseClass bclass = getBaseClass("testFieldOther");
    XWikiDocument doc = new XWikiDocument(bclassDocRef);
    doc.setXClass(bclass);

    expect(getWikiMock().getDocument(eq(bclassDocRef), same(getContext()))).andReturn(doc).once();

    replayDefault();
    Map<ValidationType, Set<String>> result = xClassRegexRule.validateField("Test.TestClass",
        "testField", "");
    verifyDefault();

    assertNull("Not existing field should be ignored by default", result);
  }

  @Test
  public void test_validateField_invalidKey_notIgnore() throws XWikiException {
    xClassRegexRule.configSrc = createDefaultMock(ConfigurationSource.class);
    XWikiDocument doc = new XWikiDocument(bclassDocRef);

    expect(getWikiMock().getDocument(eq(bclassDocRef), same(getContext()))).andReturn(doc).once();
    expect(xClassRegexRule.configSrc.getProperty(eq(
        "celements.validation.xClassRegex.ignoreInvalidKey"), eq(true))).andReturn(false).once();

    replayDefault();
    Map<ValidationType, Set<String>> result = xClassRegexRule.validateField("Test.TestClass",
        "testField", "");
    verifyDefault();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.containsKey(ValidationType.ERROR));
    assertEquals(1, result.get(ValidationType.ERROR).size());
    assertEquals("cel_validation_xclassregex_invalidkey", result.get(
        ValidationType.ERROR).iterator().next());

    xClassRegexRule.configSrc = Utils.getComponent(ConfigurationSource.class);
  }

  @Test
  public void test_validate_docField() throws XWikiException {
    Map<String, String[]> requestMap = new HashMap<>();
    requestMap.put("title", new String[] { "value1" });

    replayDefault();
    List<ValidationResult> result = xClassRegexRule.validate(parser.parseParameterMap(requestMap));
    verifyDefault();

    assertTrue("Successful validation should result in an empty map", (result != null)
        && result.isEmpty());
  }

  private BaseClass getBaseClass(String fieldName) {
    BaseProperty regexProp = new StringProperty();
    regexProp.setValue("/.+/");
    BaseProperty msgProp = new StringProperty();
    msgProp.setValue("testValMsg");
    Map<String, BaseProperty> propfields = new HashMap<>();
    PropertyClass property = new PropertyClass();
    propfields.put("validationRegExp", regexProp);
    propfields.put("validationMessage", msgProp);
    property.setFields(propfields);
    Map<String, PropertyClass> fields = new HashMap<>();
    fields.put(fieldName, property);
    BaseClass bclass = new BaseClass();
    bclass.setFields(fields);
    return bclass;
  }

  @Test
  public void testGetFieldFromProperty_null() throws XWikiException {
    PropertyClass propclass = new PropertyClass();
    assertEquals("", xClassRegexRule.getFieldFromProperty(propclass, "test"));
    propclass.put("test", null);
    assertEquals("", xClassRegexRule.getFieldFromProperty(propclass, "test"));
  }

  @Test
  public void testGetFieldFromProperty() throws XWikiException {
    BaseProperty prop = new StringProperty();
    prop.setValue("value");
    PropertyClass propclass = new PropertyClass();
    propclass.put("test", prop);
    assertEquals("value", xClassRegexRule.getFieldFromProperty(propclass, "test"));
  }

}
