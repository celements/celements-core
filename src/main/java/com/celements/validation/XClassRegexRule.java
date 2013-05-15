package com.celements.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

@Component("XClassRegexValidation")
public class XClassRegexRule implements IRequestValidationRuleRole,
IFieldValidationRuleRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      XClassRegexRule.class);

  private static final String PROPERTY_FIELD_VAL_REGEX = "validationRegExp";
  private static final String PROPERTY_FIELD_VAL_MSG = "validationMessage";

  private static final MapHandler<String, ValidationType, String> mapHandler =
      new MapHandler<String, ValidationType, String>();

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public Map<String, Map<ValidationType, Set<String>>> validateRequest(
      Map<RequestParameter, String[]> requestMap) {
    Map<String, Map<ValidationType, Set<String>>> ret =
        new HashMap<String, Map<ValidationType, Set<String>>>();
    for (RequestParameter param : requestMap.keySet()) {
      for (String value : requestMap.get(param)) {
        mapHandler.put(param.getParameterName(), validateField(param.getClassName(),
            param.getFieldName(), value), ret);
      }
    }
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Returning validation map: " + ret);
    }
    return ret;
  }

  public Map<ValidationType, Set<String>> validateField(String className,
      String fieldName, String value) {
    Map<ValidationType, Set<String>> ret = null;
    BaseClass bclass = getBaseClass(className);
    if(bclass != null) {
      PropertyClass propertyClass = (PropertyClass) bclass.getField(fieldName);
      String regex = getFieldFromProperty(propertyClass, PROPERTY_FIELD_VAL_REGEX);
      String validationMsg = getFieldFromProperty(propertyClass, PROPERTY_FIELD_VAL_MSG);
      if (!regex.isEmpty() && !matchesRegex(regex, value)) {
        ret = getStringAsMap(validationMsg);
      }
    }
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Returning validation map for field '" + className + "_" + fieldName
          + "' and value '" + value + "': " + ret);
    }
    return ret;
  }

  private BaseClass getBaseClass(String className) {
    BaseClass bclass = null;
    DocumentReference classDocRef = webUtils.resolveDocumentReference(className);
    try {
      bclass = getContext().getWiki().getDocument(classDocRef, getContext()).getXClass();
    } catch (XWikiException exc) {
      LOGGER.error("Cannot get document BaseClass for className '" + className + "'", exc);
    }
    return bclass;
  }

  String getFieldFromProperty(PropertyClass propertyClass, String fieldName) {
    BaseProperty property = (BaseProperty) propertyClass.getField(fieldName);
    String field = "";
    if ((property != null) && (property.getValue() != null)) {
      field = property.getValue().toString();
    }
    return field;
  }

  private boolean matchesRegex(String regex, String str) {
    if ((regex != null) && !regex.trim().equals("")) {
      try {
        return getContext().getUtil().match(regex, str);
      } catch (MalformedPerl5PatternException exc) {
        LOGGER.error("Failed to execute validation regex for string '" + str
            + "' and regex '" + regex + "'", exc);
      }
    }
    return false;
  }

  private Map<ValidationType, Set<String>> getStringAsMap(String str) {
    Map<ValidationType, Set<String>> map = new HashMap<ValidationType, Set<String>>();
    Set<String> validationSet = new HashSet<String>();
    validationSet.add(str);
    map.put(ValidationType.ERROR, validationSet);
    return map;
  }

}
