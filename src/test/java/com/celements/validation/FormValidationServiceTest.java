package com.celements.validation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class FormValidationServiceTest extends AbstractComponentTest {

  private FormValidationService formValidationService;

  private IRequestValidationRule reqRule1;
  private IRequestValidationRule reqRule2;
  private IRequestValidationRuleRole legReqRule1;
  private IRequestValidationRuleRole legReqRule2;
  private IFieldValidationRuleRole fieldRule1;
  private IFieldValidationRuleRole fieldRUle2;

  @Before
  public void prepare() throws Exception {
    getContext().setDoc(new XWikiDocument(new DocumentReference(
        getContext().getDatabase(), "space", "doc")));
    reqRule1 = createDefaultMock(IRequestValidationRule.class);
    reqRule2 = createDefaultMock(IRequestValidationRule.class);
    legReqRule1 = createDefaultMock(IRequestValidationRuleRole.class);
    legReqRule2 = createDefaultMock(IRequestValidationRuleRole.class);
    fieldRule1 = createDefaultMock(IFieldValidationRuleRole.class);
    fieldRUle2 = createDefaultMock(IFieldValidationRuleRole.class);
    formValidationService = (FormValidationService) Utils.getComponent(
        IFormValidationServiceRole.class);
    formValidationService.injectValidationRules(
        ImmutableMap.of("1", reqRule1, "2", reqRule2),
        ImmutableMap.of("1", legReqRule1, "2", legReqRule2),
        ImmutableMap.of("1", fieldRule1, "2", fieldRUle2));
  }

  @Test
  public void testValidateMap_valid() {
    Map<String, String[]> requestMap = new HashMap<>();
    requestMap.put("A.B_0_asdf", new String[] { "1" });
    requestMap.put("A.B_0_qwer", new String[] { "2", "3" });

    expect(reqRule1.validate(anyObject(List.class))).andReturn(Collections.emptyList());
    expect(reqRule2.validate(anyObject(List.class))).andReturn(Collections.emptyList());
    expect(legReqRule1.validateRequest(anyObject(Map.class))).andReturn(getEmptyRetMap());
    expect(legReqRule2.validateRequest(anyObject(Map.class))).andReturn(getEmptyRetMap());

    replayDefault();
    Map<String, Map<ValidationType, Set<String>>> validationMap = formValidationService
        .validateMap(requestMap);
    verifyDefault();
    assertNotNull(validationMap);
    assertEquals(0, validationMap.size());
  }

  @Test
  public void testValidateMap_invalid() {
    Map<String, String[]> requestMap = new HashMap<>();
    requestMap.put("A.B_0_asdf", new String[] { "1" });
    requestMap.put("A.B_0_qwer", new String[] { "2", "3" });
    Map<String, Map<ValidationType, Set<String>>> map = getEmptyRetMap();
    Map<ValidationType, Set<String>> innerMap = new HashMap<>();
    Set<String> set = new HashSet<>();
    set.add("invalid");
    innerMap.put(ValidationType.ERROR, set);
    map.put("asdf", innerMap);

    expect(reqRule1.validate(anyObject(List.class))).andReturn(ImmutableList.of(
        new ValidationResult(ValidationType.ERROR, "asdf", "invalid")));
    expect(reqRule2.validate(anyObject(List.class))).andReturn(Collections.emptyList());
    expect(legReqRule1.validateRequest(anyObject(Map.class))).andReturn(getEmptyRetMap());
    expect(legReqRule2.validateRequest(anyObject(Map.class))).andReturn(getEmptyRetMap());

    replayDefault();
    Map<String, Map<ValidationType, Set<String>>> validationMap = formValidationService
        .validateMap(requestMap);
    verifyDefault();

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
    requestMap.put("A.B_0_asdf", new String[] { "1" });
    requestMap.put("A.B_0_qwer", new String[] { "2", "3" });

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

    expect(reqRule1.validate(anyObject(List.class))).andReturn(Collections.emptyList());
    expect(reqRule2.validate(anyObject(List.class))).andReturn(Collections.emptyList());
    expect(legReqRule1.validateRequest(anyObject(Map.class))).andReturn(map1);
    expect(legReqRule2.validateRequest(anyObject(Map.class))).andReturn(map2);

    replayDefault();
    Map<String, Map<ValidationType, Set<String>>> validationMap = formValidationService
        .validateMap(requestMap);
    verifyDefault();

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

    expect(fieldRule1.validateField(eq(className), eq(fieldName), eq(value)))
        .andReturn(new HashMap<ValidationType, Set<String>>());
    expect(fieldRUle2.validateField(eq(className), eq(fieldName), eq(value)))
        .andReturn(new HashMap<ValidationType, Set<String>>());

    replayDefault();
    Map<ValidationType, Set<String>> validationSet = formValidationService
        .validateField(className, fieldName, value);
    verifyDefault();
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

    expect(fieldRule1.validateField(eq(className), eq(fieldName), eq(value))).andReturn(map);
    expect(fieldRUle2.validateField(eq(className), eq(fieldName), eq(value)))
        .andReturn(new HashMap<ValidationType, Set<String>>());

    replayDefault();
    Map<ValidationType, Set<String>> validationMap = formValidationService
        .validateField(className, fieldName, value);
    verifyDefault();

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

    expect(fieldRule1.validateField(eq(className), eq(fieldName), eq(value))).andReturn(map1);
    expect(fieldRUle2.validateField(eq(className), eq(fieldName), eq(value))).andReturn(map2);

    replayDefault();
    Map<ValidationType, Set<String>> validationMap = formValidationService
        .validateField(className, fieldName, value);
    verifyDefault();

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

}
