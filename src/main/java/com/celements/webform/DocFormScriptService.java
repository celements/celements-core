package com.celements.webform;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.DocFormCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("docform")
public class DocFormScriptService implements ScriptService{
  
  private static final String _DOC_FORM_COMMAND_OBJECT = "com.celements.DocFormCommand";
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  public Set<Document> updateDocFromMap(DocumentReference docRef, Map<String, ?> map
      ) throws XWikiException {
    Map<String, String[]> recompMap = new HashMap<String, String[]>();
    for (String key : map.keySet()) {
      if(map.get(key) instanceof String[]) {
        recompMap.put(key, (String[])map.get(key));
      } else if(map.get(key) instanceof String) {
        recompMap.put(key, new String[]{(String)map.get(key)});
      }
    }
    Set<Document> docs = new HashSet<Document>();
    Collection<XWikiDocument> xdocs = getDocFormCommand().updateDocFromMap(docRef,
        recompMap, getContext());
    for (XWikiDocument xdoc : xdocs) {
      docs.add(xdoc.newDocument(getContext()));
    }
    return docs;
  }
  
  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  public Set<Document> updateDocFromRequest() throws XWikiException {
    return updateDocFromRequest(null);
  }
  
  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch
   * and write the exception in a log-file
   */
  @SuppressWarnings("unchecked")
  public Set<Document> updateDocFromRequest(DocumentReference docRef
      ) throws XWikiException {
    Set<Document> docs = new HashSet<Document>();
    Collection<XWikiDocument> xdocs = getDocFormCommand().updateDocFromMap(docRef,
        getContext().getRequest().getParameterMap(), getContext());
    for (XWikiDocument xdoc : xdocs) {
      docs.add(xdoc.newDocument(getContext()));
    }
    return docs;
  }
  
  private DocFormCommand getDocFormCommand() {
    if (getContext().get(_DOC_FORM_COMMAND_OBJECT) == null) {
      getContext().put(_DOC_FORM_COMMAND_OBJECT, new DocFormCommand());
    }
    return (DocFormCommand) getContext().get(_DOC_FORM_COMMAND_OBJECT);
  }
}
