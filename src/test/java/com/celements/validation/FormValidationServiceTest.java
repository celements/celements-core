package com.celements.validation;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

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
  public void setUp_FormValidationServiceTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    formValidationService = (FormValidationService) Utils.getComponent(
        IFormValidationServiceRole.class);
    requestValidationRuleMock1 = createMock(IRequestValidationRuleRole.class);
    requestValidationRuleMock2 = createMock(IRequestValidationRuleRole.class);
    fieldValidationRuleMock1 = createMock(IFieldValidationRuleRole.class);
    fieldValidationRuleMock2 = createMock(IFieldValidationRuleRole.class);
    Map<String, IRequestValidationRuleRole> requestValidationRules = new HashMap<>();
    Map<String, IFieldValidationRuleRole> fieldValidationRules = new HashMap<>();
    requestValidationRules.put("mock1", requestValidationRuleMock1);
    requestValidationRules.put("mock2", requestValidationRuleMock2);
    fieldValidationRules.put("mock1", fieldValidationRuleMock1);
    fieldValidationRules.put("mock2", fieldValidationRuleMock2);
    formValidationService.injectValidationRules(requestValidationRules, fieldValidationRules);
  }

  @Test
  public void testValidateMap_valid() {
    Map<String, String[]> requestMap = new HashMap<>();
    requestMap.put("asdf", new String[] { "1" });
    requestMap.put("qwer", new String[] { "2", "3" });

    expect(requestValidationRuleMock1.validateRequest(formValidationService.convertMapKeys(
        requestMap))).andReturn(getEmptyRetMap()).once();
    expect(requestValidationRuleMock2.validateRequest(formValidationService.convertMapKeys(
        requestMap))).andReturn(getEmptyRetMap()).once();

    replayAll();
    Map<String, Map<ValidationType, Set<String>>> validationMap = formValidationService.validateMap(
        requestMap);
    verifyAll();
    assertNotNull(validationMap);
    assertEquals(0, validationMap.size());
  }

  @Test
  public void testValidateMap_invalid() {
    Map<String, String[]> requestMap = new HashMap<>();
    requestMap.put("asdf", new String[] { "1" });
    requestMap.put("qwer", new String[] { "2", "3" });
    Map<String, Map<ValidationType, Set<String>>> map = getEmptyRetMap();
    Map<ValidationType, Set<String>> innerMap = new HashMap<>();
    Set<String> set = new HashSet<>();
    set.add("invalid");
    innerMap.put(ValidationType.ERROR, set);
    map.put("asdf", innerMap);

    expect(requestValidationRuleMock1.validateRequest(formValidationService.convertMapKeys(
        requestMap))).andReturn(map).once();
    expect(requestValidationRuleMock2.validateRequest(formValidationService.convertMapKeys(
        requestMap))).andReturn(getEmptyRetMap()).once();

    replayAll();
    Map<String, Map<ValidationType, Set<String>>> validationMap = formValidationService.validateMap(
        requestMap);
    verifyAll();

    assertNotNull(validationMap);
    assertEquals(1, validationMap.size());
    Map<ValidationType, Set<String>> mapResult = validationMap.get("asdf");
    assertNotNull(mapResult);
    assertEquals(1, mapResult.size());
    Set<String> setResult = mapResult.get(ValidationType.ERROR);
    assertNotNull(setResult);
    assertEquals(1, setResult.size());
    assertTrue(setResult.contains("invalid"));
  }

  @Test
  public void testValidateMap_invalid_both() {
    Map<String, String[]> requestMap = new HashMap<>();
    requestMap.put("asdf", new String[] { "1" });
    requestMap.put("qwer", new String[] { "2", "3" });

    Map<String, Map<ValidationType, Set<String>>> map1 = getEmptyRetMap();
    Map<ValidationType, Set<String>> innerMap = new HashMap<>();
    Set<String> set = new HashSet<>();
    set.add("invalid1");
    innerMap.put(ValidationType.ERROR, set);
    map1.put("asdf", innerMap);
    innerMap = new HashMap<>();
    set = new HashSet<>();
    set.add("invalid2");
    innerMap.put(ValidationType.ERROR, set);
    map1.put("qwer", innerMap);

    Map<String, Map<ValidationType, Set<String>>> map2 = getEmptyRetMap();
    innerMap = new HashMap<>();
    set = new HashSet<>();
    set.add("invalid3");
    innerMap.put(ValidationType.ERROR, set);
    map2.put("qwer", innerMap);

    expect(requestValidationRuleMock1.validateRequest(formValidationService.convertMapKeys(
        requestMap))).andReturn(map1).once();
    expect(requestValidationRuleMock2.validateRequest(formValidationService.convertMapKeys(
        requestMap))).andReturn(map2).once();

    replayAll();
    Map<String, Map<ValidationType, Set<String>>> validationMap = formValidationService.validateMap(
        requestMap);
    verifyAll();

    assertNotNull(validationMap);
    assertEquals(2, validationMap.size());
    Map<ValidationType, Set<String>> mapResult = validationMap.get("asdf");
    assertNotNull(mapResult);
    assertEquals(1, mapResult.size());
    Set<String> setResult = mapResult.get(ValidationType.ERROR);
    assertNotNull(setResult);
    assertEquals(1, setResult.size());
    assertTrue(setResult.contains("invalid1"));
    mapResult = validationMap.get("qwer");
    assertNotNull(mapResult);
    assertEquals(1, mapResult.size());
    setResult = mapResult.get(ValidationType.ERROR);
    assertNotNull(setResult);
    assertEquals(2, setResult.size());
    assertTrue(setResult.contains("invalid2"));
    assertTrue(setResult.contains("invalid3"));
  }

  @Test
  public void testValidateField_valid() {
    String className = "myClassName";
    String fieldName = "myFieldName";
    String value = "myValue";

    expect(fieldValidationRuleMock1.validateField(eq(className), eq(fieldName), eq(
        value))).andReturn(new HashMap<ValidationType, Set<String>>()).once();
    expect(fieldValidationRuleMock2.validateField(eq(className), eq(fieldName), eq(
        value))).andReturn(new HashMap<ValidationType, Set<String>>()).once();

    replayAll();
    Map<ValidationType, Set<String>> validationSet = formValidationService.validateField(className,
        fieldName, value);
    verifyAll();
    assertNotNull(validationSet);
    assertEquals(0, validationSet.size());
  }

  @Test
  public void testValidateField_invalid() {
    String className = "myClassName";
    String fieldName = "myFieldName";
    String value = "myValue";
    Map<ValidationType, Set<String>> map = new HashMap<>();
    Set<String> set = new HashSet<>();
    set.add("invalid");
    map.put(ValidationType.ERROR, set);

    expect(fieldValidationRuleMock1.validateField(eq(className), eq(fieldName), eq(
        value))).andReturn(map).once();
    expect(fieldValidationRuleMock2.validateField(eq(className), eq(fieldName), eq(
        value))).andReturn(new HashMap<ValidationType, Set<String>>()).once();

    replayAll();
    Map<ValidationType, Set<String>> validationMap = formValidationService.validateField(className,
        fieldName, value);
    verifyAll();

    assertNotNull(validationMap);
    assertEquals(1, validationMap.size());
    set = validationMap.get(ValidationType.ERROR);
    assertEquals(1, set.size());
    assertTrue(set.contains("invalid"));
  }

  @Test
  public void testValidateField_invalid_both() {
    String className = "myClassName";
    String fieldName = "myFieldName";
    String value = "myValue";
    Map<ValidationType, Set<String>> map1 = new HashMap<>();
    Set<String> set = new HashSet<>();
    set.add("invalid1");
    map1.put(ValidationType.ERROR, set);
    Map<ValidationType, Set<String>> map2 = new HashMap<>();
    set = new HashSet<>();
    set.add("invalid2");
    set.add("invalid3");
    map2.put(ValidationType.ERROR, set);

    expect(fieldValidationRuleMock1.validateField(eq(className), eq(fieldName), eq(
        value))).andReturn(map1).once();
    expect(fieldValidationRuleMock2.validateField(eq(className), eq(fieldName), eq(
        value))).andReturn(map2).once();

    replayAll();
    Map<ValidationType, Set<String>> validationMap = formValidationService.validateField(className,
        fieldName, value);
    verifyAll();

    assertNotNull(validationMap);
    assertEquals(1, validationMap.size());
    set = validationMap.get(ValidationType.ERROR);
    assertEquals(3, set.size());
    assertTrue(set.contains("invalid1"));
    assertTrue(set.contains("invalid2"));
    assertTrue(set.contains("invalid3"));
  }

  private Map<String, Map<ValidationType, Set<String>>> getEmptyRetMap() {
    return new HashMap<>();
  }

  private void replayAll(Object... mocks) {
    replay(xwiki, requestValidationRuleMock1, requestValidationRuleMock2, fieldValidationRuleMock1,
        fieldValidationRuleMock2);
    replay(mocks);
  }

  private void verifyAll(Object... mocks) {
    verify(xwiki, requestValidationRuleMock1, requestValidationRuleMock2, fieldValidationRuleMock1,
        fieldValidationRuleMock2);
    verify(mocks);
  }

}
