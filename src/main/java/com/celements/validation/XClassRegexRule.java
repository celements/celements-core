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
public class XClassRegexRule implements IValidationRuleRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      XClassRegexRule.class);

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public Map<String, Set<String>> validate(Map<RequestParameter, String[]> requestMap) {
    Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();
    for (RequestParameter param : requestMap.keySet()) {
      Set<String> resultSet = new HashSet<String>();
      for (String value : requestMap.get(param)) {
        resultSet.addAll(validateField(param.getClassName(), param.getFieldName(), value));
      }
      if (!resultSet.isEmpty()) {
        resultMap.put(param.getParameterName(), resultSet);
      }
    }
    return resultMap;
  }

  public Set<String> validateField(String className, String fieldName, String value) {
    Set<String> validationSet = new HashSet<String>();
    BaseClass bclass = getBaseClass(className);
    if(bclass != null) {
      PropertyClass propertyClass = (PropertyClass) bclass.getField(fieldName);
      String regex = getFieldFromProperty(propertyClass, "validationRegExp");
      String validationMsg = getFieldFromProperty(propertyClass, "validationMessage");
      try {
        if (!matchesRegex(regex, value)) {
          validationSet.add(validationMsg);
        }
      } catch (MalformedPerl5PatternException exc) {
        LOGGER.error("Failed to execute validation regex for field '" + fieldName
            + "' in class '" + className + "'", exc);
        validationSet.add(validationMsg);
      }
    }
    return validationSet;
  }

  private boolean matchesRegex(String regex, String str)
      throws MalformedPerl5PatternException {
    if ((regex != null) && !regex.trim().equals("")) {
      return getContext().getUtil().match(regex, str);
    }
    return false;
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
    if ((property != null) && (property.getValue() != null)) {
      return property.getValue().toString();
    }
    return "";
  }

}
