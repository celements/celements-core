package com.celements.mandatory;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.common.classes.CompositorComponent;
import com.xpn.xwiki.XWikiContext;

@Component
public class MandatoryDocumentCompositor implements IMandatoryDocumentCompositorRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CompositorComponent.class);
  
  @Requirement
  private Map<String, IMandatoryDocumentRole> classCollectionMap;

  @Requirement
  private Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public void checkAllMandatoryDocuments() {
    LOGGER.debug("checkAllMandatoryDocuments for wiki [" + "].");
  }

}
