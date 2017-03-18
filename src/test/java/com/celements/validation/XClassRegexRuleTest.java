package com.celements.validation;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

public class XClassRegexRuleTest extends AbstractBridgedComponentTestCase {

  private XClassRegexRule xClassRegexRule;
  private DocumentReference bclassDocRef;

  @Before
  public void setUp_XClassRegeRuleTest() throws Exception {
    xClassRegexRule = (XClassRegexRule) Utils.getComponent(IRequestValidationRuleRole.class,
        "XClassRegexValidation");
    bclassDocRef = new DocumentReference(getContext().getDatabase(), "Test", "TestClass");
  }

  @Test
  public void testValidate_empty() throws XWikiException {
    Map<RequestParameter, String[]> requestMap = new HashMap<>();

    replayDefault();
    Map<String, Map<ValidationType, Set<String>>> result = xClassRegexRule.validateRequest(
        requestMap);
    verifyDefault();

    assertTrue("Successful validation should result in an empty map", (result != null)
        && result.isEmpty());
  }

  @Test
  public void testValidate_valid() throws XWikiException {
    BaseClass bclass = getBaseClass("testField");
    DocumentReference bclassDocRef1 = new DocumentReference(getContext().getDatabase(), "Test",
        "TestClass1");
    DocumentReference bclassDocRef2 = new DocumentReference(getContext().getDatabase(), "Test",
        "TestClass2");
    XWikiDocument doc1 = new XWikiDocument(bclassDocRef1);
    XWikiDocument doc2 = new XWikiDocument(bclassDocRef2);
    doc1.setXClass(bclass);
    doc2.setXClass(bclass);

    Map<RequestParameter, String[]> requestMap = new HashMap<>();
    String param1 = "Test.TestClass1_0_testField";
    String param2 = "Test.TestClass2_0_testField";
    requestMap.put(RequestParameter.create(param1), new String[] { "value1" });
    requestMap.put(RequestParameter.create(param2), new String[] { "value2", "asdf" });

    expect(getWikiMock().getDocument(eq(bclassDocRef1), same(getContext()))).andReturn(doc1).once();
    expect(getWikiMock().getDocument(eq(bclassDocRef2), same(getContext()))).andReturn(doc2).times(
        2);

    replayDefault();
    Map<String, Map<ValidationType, Set<String>>> result = xClassRegexRule.validateRequest(
        requestMap);
    verifyDefault();

    assertTrue("Successful validation should result in an empty map", (result != null)
        && result.isEmpty());
  }

  @Test
  public void testValidate_invalid() throws XWikiException {
    BaseClass bclass = getBaseClass("testField");
    DocumentReference bclassDocRef1 = new DocumentReference(getContext().getDatabase(), "Test",
        "TestClass1");
    DocumentReference bclassDocRef2 = new DocumentReference(getContext().getDatabase(), "Test",
        "TestClass2");
    XWikiDocument doc1 = new XWikiDocument(bclassDocRef1);
    XWikiDocument doc2 = new XWikiDocument(bclassDocRef2);
    doc1.setXClass(bclass);
    doc2.setXClass(bclass);

    Map<RequestParameter, String[]> requestMap = new HashMap<>();
    String param1 = "Test.TestClass1_0_testField";
    String param2 = "Test.TestClass2_0_testField";
    requestMap.put(RequestParameter.create(param1), new String[] { "" });
    requestMap.put(RequestParameter.create(param2), new String[] { "", "" });

    expect(getWikiMock().getDocument(eq(bclassDocRef1), same(getContext()))).andReturn(doc1).once();
    expect(getWikiMock().getDocument(eq(bclassDocRef2), same(getContext()))).andReturn(doc2).times(
        2);

    replayDefault();
    Map<String, Map<ValidationType, Set<String>>> result = xClassRegexRule.validateRequest(
        requestMap);
    verifyDefault();

    assertNotNull(result);
    assertEquals(2, result.size());
    Set<String> set1 = result.get(param1).get(ValidationType.ERROR);
    assertNotNull(set1);
    assertEquals(1, set1.size());
    assertTrue(set1.contains("testValMsg"));
    Set<String> set2 = result.get(param1).get(ValidationType.ERROR);
    assertNotNull(set2);
    assertEquals(1, set2.size());
    assertTrue(set2.contains("testValMsg"));
  }

  @Test
  public void testValidateField_valid() throws XWikiException {
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
  public void testValidateField_invalid() throws XWikiException {
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
  public void testValidateField_invalidKey_class() throws XWikiException {
    XWikiDocument doc = new XWikiDocument(bclassDocRef);

    expect(getWikiMock().getDocument(eq(bclassDocRef), same(getContext()))).andReturn(doc).once();

    replayDefault();
    Map<ValidationType, Set<String>> result = xClassRegexRule.validateField("Test.TestClass",
        "testField", "");
    verifyDefault();

    assertNull("Not existing class should be ignored by default", result);
  }

  @Test
  public void testValidateField_invalidKey_field() throws XWikiException {
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
  public void testValidateField_invalidKey_notIgnore() throws XWikiException {
    xClassRegexRule.configSrc = createMockAndAddToDefault(ConfigurationSource.class);
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
