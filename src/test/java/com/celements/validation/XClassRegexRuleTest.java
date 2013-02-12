package com.celements.validation;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

public class XClassRegexRuleTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private XClassRegexRule xClassRegexRule;

  @Before
  public void setUp_XClassRegeRuleTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    xClassRegexRule = (XClassRegexRule) Utils.getComponent(
        IRequestValidationRuleRole.class, "XClassRegexValidation");
  }

  @Test
  public void testValidate_empty() throws XWikiException {
    Map<RequestParameter, String[]> requestMap = new HashMap<RequestParameter, String[]>();

    replayAll();
    Map<String, Set<String>> result = xClassRegexRule.validateRequest(requestMap);
    verifyAll();

    assertTrue("Successful validation should result in an empty map",
        (result != null) && result.isEmpty());
  }

  @Test
  public void testValidate_valid() throws XWikiException {
    BaseClass bclass = getBaseClass("testField");
    DocumentReference bclassDocRef1 = new DocumentReference(context.getDatabase(), "Test",
        "TestClass1");
    DocumentReference bclassDocRef2 = new DocumentReference(context.getDatabase(), "Test",
        "TestClass2");
    XWikiDocument doc1 = new XWikiDocument(bclassDocRef1);
    XWikiDocument doc2 = new XWikiDocument(bclassDocRef2);
    doc1.setXClass(bclass);
    doc2.setXClass(bclass);

    Map<RequestParameter, String[]> requestMap = new HashMap<RequestParameter, String[]>();
    String param1 = "Test.TestClass1_0_testField";
    String param2 = "Test.TestClass2_0_testField";
    requestMap.put(RequestParameter.create(param1), new String[]{"value1"});
    requestMap.put(RequestParameter.create(param2), new String[]{"value2", "asdf"});

    expect(xwiki.getDocument(eq(bclassDocRef1), same(context))).andReturn(doc1).once();
    expect(xwiki.getDocument(eq(bclassDocRef2), same(context))).andReturn(doc2).times(2);

    replayAll();
    Map<String, Set<String>> result = xClassRegexRule.validateRequest(requestMap);
    verifyAll();

    assertTrue("Successful validation should result in an empty map",
        (result != null) && result.isEmpty());
  }

  @Test
  public void testValidate_invalid() throws XWikiException {
    BaseClass bclass = getBaseClass("testField");
    DocumentReference bclassDocRef1 = new DocumentReference(context.getDatabase(), "Test",
        "TestClass1");
    DocumentReference bclassDocRef2 = new DocumentReference(context.getDatabase(), "Test",
        "TestClass2");
    XWikiDocument doc1 = new XWikiDocument(bclassDocRef1);
    XWikiDocument doc2 = new XWikiDocument(bclassDocRef2);
    doc1.setXClass(bclass);
    doc2.setXClass(bclass);

    Map<RequestParameter, String[]> requestMap = new HashMap<RequestParameter, String[]>();
    String param1 = "Test.TestClass1_0_testField";
    String param2 = "Test.TestClass2_0_testField";
    requestMap.put(RequestParameter.create(param1), new String[]{""});
    requestMap.put(RequestParameter.create(param2), new String[]{"", ""});

    expect(xwiki.getDocument(eq(bclassDocRef1), same(context))).andReturn(doc1).once();
    expect(xwiki.getDocument(eq(bclassDocRef2), same(context))).andReturn(doc2).times(2);

    replayAll();
    Map<String, Set<String>> result = xClassRegexRule.validateRequest(requestMap);
    verifyAll();

    assertNotNull(result);
    assertEquals(2, result.size());
    Set<String> set1 = result.get(param1);
    assertNotNull(set1);
    assertEquals(1, set1.size());
    assertTrue(set1.contains("is empty"));
    Set<String> set2 = result.get(param1);
    assertNotNull(set2);
    assertEquals(1, set2.size());
    assertTrue(set2.contains("is empty"));
  }

  @Test
  public void testValidateField_valid() throws XWikiException {
    BaseClass bclass = getBaseClass("testField");
    DocumentReference bclassDocRef = new DocumentReference(context.getDatabase(), "Test",
        "TestClass");
    XWikiDocument doc = new XWikiDocument(bclassDocRef);
    doc.setXClass(bclass);

    expect(xwiki.getDocument(eq(bclassDocRef), same(context))).andReturn(doc).once();

    replayAll();
    Set<String> result = xClassRegexRule.validateField("Test.TestClass", "testField",
        "value");
    verifyAll();

    assertTrue("Successful validation should result in an empty set",
        (result != null) && result.isEmpty());
  }

  @Test
  public void testValidateField_invalid() throws XWikiException {
    BaseClass bclass = getBaseClass("testField");
    DocumentReference bclassDocRef = new DocumentReference(context.getDatabase(), "Test",
        "TestClass");
    XWikiDocument doc = new XWikiDocument(bclassDocRef);
    doc.setXClass(bclass);

    expect(xwiki.getDocument(eq(bclassDocRef), same(context))).andReturn(doc).once();

    replayAll();
    Set<String> result = xClassRegexRule.validateField("Test.TestClass", "testField",
        "");
    verifyAll();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("is empty", result.iterator().next());
  }

  private BaseClass getBaseClass(String fieldName) {
    BaseProperty regexProp = new StringProperty();
    regexProp.setValue("/.+/");
    BaseProperty msgProp = new StringProperty();
    msgProp.setValue("is empty");
    Map<String, BaseProperty> propfields = new HashMap<String, BaseProperty>();
    PropertyClass property = new PropertyClass();
    propfields.put("validationRegExp", regexProp);
    propfields.put("validationMessage", msgProp);
    property.setFields(propfields);
    Map<String, PropertyClass> fields = new HashMap<String, PropertyClass>();
    fields.put(fieldName, property);
    BaseClass bclass = new BaseClass();
    bclass.setFields(fields);
    return bclass;
  }

  @Test
  public void testGetFieldFromProperty_null() throws XWikiException{
    PropertyClass propclass = new PropertyClass();
    assertEquals("", xClassRegexRule.getFieldFromProperty(propclass, "test"));
    propclass.put("test", null);
    assertEquals("", xClassRegexRule.getFieldFromProperty(propclass, "test"));
  }

  @Test
  public void testGetFieldFromProperty() throws XWikiException{
    BaseProperty prop = new StringProperty();
    prop.setValue("value");
    PropertyClass propclass = new PropertyClass();
    propclass.put("test", prop);
    assertEquals("value", xClassRegexRule.getFieldFromProperty(propclass, "test"));
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki);
    verify(mocks);
  }

}
