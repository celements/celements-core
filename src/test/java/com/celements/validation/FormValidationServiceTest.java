package com.celements.validation;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class FormValidationServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;

  private FormValidationService formValidationService;

  private IRequestValidationRuleRole requestValidationRuleMock1;
  private IRequestValidationRuleRole requestValidationRuleMock2;
  private IFieldValidationRuleRole fieldValidationRuleMock1;
  private IFieldValidationRuleRole fieldValidationRuleMock2;

  @Before
  public void setUp_DocFormCommandTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    formValidationService = (FormValidationService) Utils.getComponent(
        IFormValidationRole.class);
    requestValidationRuleMock1 = createMock(IRequestValidationRuleRole.class);
    requestValidationRuleMock2 = createMock(IRequestValidationRuleRole.class);
    fieldValidationRuleMock1 = createMock(IFieldValidationRuleRole.class);
    fieldValidationRuleMock2 = createMock(IFieldValidationRuleRole.class);
    Map<String, IRequestValidationRuleRole> requestValidationRules =
        new HashMap<String, IRequestValidationRuleRole>();
    Map<String, IFieldValidationRuleRole> fieldValidationRules =
        new HashMap<String, IFieldValidationRuleRole>();
    requestValidationRules.put("mock1", requestValidationRuleMock1);
    requestValidationRules.put("mock2", requestValidationRuleMock2);
    fieldValidationRules.put("mock1", fieldValidationRuleMock1);
    fieldValidationRules.put("mock2", fieldValidationRuleMock2);
    formValidationService.injectValidationRules(requestValidationRules,
        fieldValidationRules);
  }

  @Test
  public void testConvertRequestMap() {
    Map<Object, Object> requestMap = new HashMap<Object, Object>();
    requestMap.put("asdf", "1");
    requestMap.put("qwer", new String[] { "2", "3" });

    Map<String, String[]> convMap = formValidationService.convertRequestMap(requestMap);

    assertNotNull(convMap);
    assertEquals(2, convMap.size());

    String[] arr1 = convMap.get("asdf");
    assertNotNull(arr1);
    assertEquals(1, arr1.length);
    assertEquals("1", arr1[0]);

    String[] arr2 = convMap.get("qwer");
    assertNotNull(arr2);
    assertEquals(2, arr2.length);
    assertEquals("2", arr2[0]);
    assertEquals("3", arr2[1]);
  }

  @Test
  public void testValidateMap_valid() {
    Map<String, String[]> requestMap = new HashMap<String, String[]>();
    requestMap.put("asdf", new String[] { "1" });
    requestMap.put("qwer", new String[] { "2", "3" });

    expect(requestValidationRuleMock1.validateRequest(
        formValidationService.convertMapKeys(requestMap))).andReturn(
            new HashMap<String, Set<String>>()).once();
    expect(requestValidationRuleMock2.validateRequest(
        formValidationService.convertMapKeys(requestMap))).andReturn(
            new HashMap<String, Set<String>>()).once();

    replayAll();
    Map<String, Set<String>> validationMap = formValidationService.validateMap(requestMap);
    verifyAll();
    assertNotNull(validationMap);
    assertEquals(0, validationMap.size());
  }

  @Test
  public void testValidateMap_invalid() {
    Map<String, String[]> requestMap = new HashMap<String, String[]>();
    requestMap.put("asdf", new String[] { "1" });
    requestMap.put("qwer", new String[] { "2", "3" });
    Map<String, Set<String>> map = new HashMap<String, Set<String>>();
    Set<String> set = new HashSet<String>();
    set.add("invalid");
    map.put("asdf", set);

    expect(requestValidationRuleMock1.validateRequest(
        formValidationService.convertMapKeys(requestMap))).andReturn(map).once();
    expect(requestValidationRuleMock2.validateRequest(
        formValidationService.convertMapKeys(requestMap))).andReturn(
            new HashMap<String, Set<String>>()).once();

    replayAll();
    Map<String, Set<String>> validationMap = formValidationService.validateMap(requestMap);
    verifyAll();

    assertNotNull(validationMap);
    assertEquals(1, validationMap.size());
    Set<String> setResult = validationMap.get("asdf");
    assertNotNull(setResult);
    assertEquals(1, setResult.size());
    assertTrue(setResult.contains("invalid"));
  }

  @Test
  public void testValidateMap_invalid_both() {
    Map<String, String[]> requestMap = new HashMap<String, String[]>();
    requestMap.put("asdf", new String[] { "1" });
    requestMap.put("qwer", new String[] { "2", "3" });
    Map<String, Set<String>> map1 = new HashMap<String, Set<String>>();
    Set<String> set1 = new HashSet<String>();
    set1.add("invalid1");
    map1.put("asdf", set1);
    Map<String, Set<String>> map2 = new HashMap<String, Set<String>>();
    Set<String> set2 = new HashSet<String>();
    set2.add("invalid2");
    map2.put("asdf", set2);
    Set<String> set3 = new HashSet<String>();
    set3.add("invalid3");
    set3.add("invalid4");
    map2.put("qwer", set3);

    expect(requestValidationRuleMock1.validateRequest(
        formValidationService.convertMapKeys(requestMap))).andReturn(map1).once();
    expect(requestValidationRuleMock2.validateRequest(
        formValidationService.convertMapKeys(requestMap))).andReturn(map2).once();

    replayAll();
    Map<String, Set<String>> validationMap = formValidationService.validateMap(requestMap);
    verifyAll();

    assertNotNull(validationMap);
    assertEquals(2, validationMap.size());
    Set<String> setResult1 = validationMap.get("asdf");
    assertNotNull(setResult1);
    assertEquals(2, setResult1.size());
    assertTrue(setResult1.contains("invalid1"));
    assertTrue(setResult1.contains("invalid2"));
    Set<String> setResult2 = validationMap.get("qwer");
    assertNotNull(setResult2);
    assertEquals(2, setResult2.size());
    assertTrue(setResult2.contains("invalid3"));
    assertTrue(setResult2.contains("invalid4"));
  }

  @Test
  public void testMergeMaps() {
    Map<String, Set<String>> mergeMap = new HashMap<String, Set<String>>();
    mergeMap.put("asdf", new HashSet<String>(Arrays.asList(new String[] { "1", "2" })));
    mergeMap.put("qwer", new HashSet<String>(Arrays.asList(new String[] { "3" })));
    Map<String, Set<String>> toMap = new HashMap<String, Set<String>>();
    mergeMap.put("yxcv", new HashSet<String>(Arrays.asList(new String[] { "4", "5" })));

    formValidationService.mergeMaps(mergeMap, toMap);

    assertEquals(3, toMap.size());

    Set<String> set1 = toMap.get("yxcv");
    assertNotNull(set1);
    assertEquals(2, set1.size());
    assertTrue(set1.contains("4"));
    assertTrue(set1.contains("5"));

    Set<String> set2 = toMap.get("asdf");
    assertNotNull(set2);
    assertEquals(2, set2.size());
    assertTrue(set2.contains("1"));
    assertTrue(set2.contains("2"));

    Set<String> set3 = toMap.get("qwer");
    assertNotNull(set3);
    assertEquals(1, set3.size());
    assertTrue(set3.contains("3"));
  }

  @Test
  public void testValidateField_valid() {
    String className = "myClassName";
    String fieldName = "myFieldName";
    String value = "myValue";

    expect(fieldValidationRuleMock1.validateField(eq(className), eq(fieldName),
        eq(value))).andReturn(new HashSet<String>()).once();
    expect(fieldValidationRuleMock2.validateField(eq(className), eq(fieldName),
        eq(value))).andReturn(new HashSet<String>()).once();

    replayAll();
    Set<String> validationSet = formValidationService.validateField(className, fieldName,
        value);
    verifyAll();
    assertNotNull(validationSet);
    assertEquals(0, validationSet.size());
  }

  @Test
  public void testValidateField_invalid() {
    String className = "myClassName";
    String fieldName = "myFieldName";
    String value = "myValue";
    Set<String> set = new HashSet<String>();
    set.add("invalid");

    expect(fieldValidationRuleMock1.validateField(eq(className), eq(fieldName),
        eq(value))).andReturn(set).once();
    expect(fieldValidationRuleMock2.validateField(eq(className), eq(fieldName),
        eq(value))).andReturn(new HashSet<String>()).once();

    replayAll();
    Set<String> validationSet = formValidationService.validateField(className, fieldName,
        value);
    verifyAll();
    assertNotNull(validationSet);
    assertEquals(1, validationSet.size());
    assertTrue(validationSet.contains("invalid"));
  }

  @Test
  public void testValidateField_invalid_both() {
    String className = "myClassName";
    String fieldName = "myFieldName";
    String value = "myValue";
    Set<String> set1 = new HashSet<String>();
    set1.add("invalid1");
    Set<String> set2 = new HashSet<String>();
    set2.add("invalid2");
    set2.add("invalid3");

    expect(fieldValidationRuleMock1.validateField(eq(className), eq(fieldName),
        eq(value))).andReturn(set1).once();
    expect(fieldValidationRuleMock2.validateField(eq(className), eq(fieldName),
        eq(value))).andReturn(set2).once();

    replayAll();
    Set<String> validationSet = formValidationService.validateField(className, fieldName,
        value);
    verifyAll();
    assertNotNull(validationSet);
    assertEquals(3, validationSet.size());
    assertTrue(validationSet.contains("invalid1"));
    assertTrue(validationSet.contains("invalid2"));
    assertTrue(validationSet.contains("invalid3"));
  }

  private void replayAll(Object... mocks) {
    replay(xwiki, requestValidationRuleMock1, requestValidationRuleMock2,
        fieldValidationRuleMock1, fieldValidationRuleMock2);
    replay(mocks);
  }

  private void verifyAll(Object... mocks) {
    verify(xwiki, requestValidationRuleMock1, requestValidationRuleMock2,
        fieldValidationRuleMock1, fieldValidationRuleMock2);
    verify(mocks);
  }

}
