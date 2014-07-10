package com.celements.rteConfig.classes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.rteConfigClasses")
public class RTEConfigClasses extends AbstractClassCollection {

  private static Log LOGGER = LogFactory.getFactory().getInstance(RTEConfigClasses.class);

  public static final String RTE_CONFIG_TYPE_PRPOP_CLASS_DOC =
      "RTEConfigTypePropertiesClass";
  public static final String RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE = "Classes";
  public static final String RTE_CONFIG_TYPE_PRPOP_CLASS =
      RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE + "." + RTE_CONFIG_TYPE_PRPOP_CLASS_DOC;

  public static final String RTE_CONFIG_TEMPLATE_PRPOP_CLASS_SPACE = "RTEConfigClasses";
  public static final String RTE_CONFIG_TEMPLATE_PRPOP_CLASS_DOC = "RTEConfigTemplate";

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

  public String getConfigName() {
    return "rteConfigClasses";
  }

  @Override
  protected void initClasses() throws XWikiException {
    getRTEConfigTemplateClass();
  }

  public DocumentReference getRTEConfigTemplateClassRef(String wikiName) {
    return new DocumentReference(wikiName, RTE_CONFIG_TEMPLATE_PRPOP_CLASS_SPACE,
        RTE_CONFIG_TEMPLATE_PRPOP_CLASS_DOC);
  }

  private BaseClass getRTEConfigTemplateClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getRTEConfigTemplateClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + getRTEConfigTemplateClassRef(getContext(
          ).getDatabase()) + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("templateName", "RichTextEditor Template Name",
        30);
    needsUpdate |= bclass.addTextField("templateUrl", "RichTextEditor Template URL",
        30);
    needsUpdate |= bclass.addTextField("templateDesc", "RichTextEditor Template"
        + " Description", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

}
