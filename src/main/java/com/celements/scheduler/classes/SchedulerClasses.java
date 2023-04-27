package com.celements.scheduler.classes;

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

  @Requirement
  private ISchedulerClassConfig classConf;

  @Override
  public String getConfigName() {
    return "scheduler";
  }

  @Override
  protected void initClasses() throws XWikiException {
    getSchedulerJobClass();
  }

  private BaseClass getSchedulerJobClass() throws XWikiException {
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
    bclass.setXClassReference(docRef);
    needsUpdate |= bclass.addTextField(ISchedulerClassConfig.PROP_JOB_NAME, "Job Name", 60);
    needsUpdate |= bclass.addTextAreaField(ISchedulerClassConfig.PROP_JOB_DESCRIPTION,
        "Job Description", 45, 10);
    needsUpdate |= bclass.addTextField(ISchedulerClassConfig.PROP_JOB_CLASS, "Job Class", 60);
    needsUpdate |= bclass.addTextField(ISchedulerClassConfig.PROP_STATUS, "Status", 30);
    needsUpdate |= bclass.addTextField(ISchedulerClassConfig.PROP_CRON, "Cron Expression", 30);
    needsUpdate |= bclass.addTextAreaField(ISchedulerClassConfig.PROP_SCRIPT, "Job Script", 60, 10);
    // make sure that the script field is of type pure text so that wysiwyg editor is
    // never used for it
    TextAreaClass scriptField = (TextAreaClass) bclass.getField(ISchedulerClassConfig.PROP_SCRIPT);
    // get editor returns lowercase but the values are actually camelcase
    if (scriptField.getEditor() != "puretext") {
      scriptField.setStringValue("editor", "PureText");
      needsUpdate = true;
    }
    needsUpdate |= bclass.addTextField(ISchedulerClassConfig.PROP_CONTEXT_USER,
        "Job execution context user", 30);
    needsUpdate |= bclass.addTextField(ISchedulerClassConfig.PROP_CONTEXT_LANG,
        "Job execution context lang", 30);
    needsUpdate |= bclass.addTextField(ISchedulerClassConfig.PROP_CONTEXT_DATABASE,
        "Job execution context database", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

}
