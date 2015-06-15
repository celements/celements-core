package com.celements.scheduler.classes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;

@Component("SchedulerClasses")
public class SchedulerClasses extends AbstractClassCollection {

  private static Log LOGGER = LogFactory.getFactory().getInstance(SchedulerClasses.class);

  @Requirement
  private ISchedulerClassConfig classConf;

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

  @Override
  public String getConfigName() {
    return "scheduler";
  }

  @Override
  protected void initClasses() throws XWikiException {
    getSchedulerJobClass();
  }

  protected BaseClass getSchedulerJobClass() throws XWikiException {
    DocumentReference docRef = classConf.getSchedulerJobClassRef();
    XWikiDocument doc;
    boolean needsUpdate = false;
    
    try {
      doc = getContext().getWiki().getDocument(docRef, getContext());
    } catch (XWikiException xwe) {
      LOGGER.error("Exception while getting doc for ClassRef '" + docRef + "'", xwe);
      doc = new XWikiDocument(docRef);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(docRef);  
    needsUpdate |= bclass.addTextField("jobName", "Job Name", 60);
    needsUpdate |= bclass.addTextAreaField("jobDescription", "Job Description", 45, 10);
    needsUpdate |= bclass.addTextField("jobClass", "Job Class", 60);
    needsUpdate |= bclass.addTextField("status", "Status", 30);
    needsUpdate |= bclass.addTextField("cron", "Cron Expression", 30);
    needsUpdate |= bclass.addTextAreaField("script", "Job Script", 60, 10);
    // make sure that the script field is of type pure text so that wysiwyg editor is 
    // never used for it
    TextAreaClass scriptField = (TextAreaClass) bclass.getField("script");
    // get editor returns lowercase but the values are actually camelcase
    if (scriptField.getEditor() != "puretext") {
        scriptField.setStringValue("editor", "PureText");
        needsUpdate = true;
    }
    needsUpdate |= bclass.addTextField("contextUser", "Job execution context user", 30);
    needsUpdate |= bclass.addTextField("contextLang", "Job execution context lang", 30);
    needsUpdate |= bclass.addTextField("contextDatabase", "Job execution context database",
        30);
    
    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

}
