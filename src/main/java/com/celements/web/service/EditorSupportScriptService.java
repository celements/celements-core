package com.celements.web.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.validation.IFormValidationServiceRole;
import com.celements.validation.ValidationType;
import com.celements.web.plugin.cmd.SuggestListCommand;
import com.celements.web.utils.SuggestBaseClass;
import com.xpn.xwiki.XWikiContext;

@Component("editorsupport")
public class EditorSupportScriptService  implements ScriptService{
    
    @Requirement
    private IFormValidationServiceRole formValidation;
    
    @Requirement
    private Execution execution;

    private XWikiContext getContext() {
      return (XWikiContext)execution.getContext().getProperty("xwikicontext");
    }
    
    public Map<ValidationType, Set<String>> validateField(String className, String fieldName, String value) {
      return formValidation.validateField(className, fieldName, value);
    }
    
    public Map<String, Map<ValidationType, Set<String>>> validateRequest() {
      return formValidation.validateRequest();
    }
    
    public SuggestBaseClass getSuggestBaseClass(DocumentReference classreference,
        String fieldname) {
      return new SuggestBaseClass(classreference, fieldname, getContext());
    }
    
    public List<Object> getSuggestList(DocumentReference classRef, String fieldname,
        String input) {
      return new SuggestListCommand().getSuggestList(classRef, fieldname, null, input, "",
          "", 0, getContext());
    }
    
    public List<Object> getSuggestList(DocumentReference classRef, String fieldname,
        List<String> excludes, String input, String firstCol, String secCol, int limit) {
      return new SuggestListCommand().getSuggestList(classRef, fieldname, excludes,
          input, firstCol, secCol, limit, getContext());
    }
}
