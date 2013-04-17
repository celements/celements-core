package com.celements.emptycheck.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.emptycheck.internal.NextNonEmptyChildrenCommand;
import com.xpn.xwiki.XWikiContext;

@Component
@Singleton
public class EmptyCheckService implements IEmptyCheckRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      EmptyCheckService.class);

  @Requirement
  Map<String, IEmptyDocStrategyRole> emptyDocStrategies;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty(
        "xwikicontext");
  }

  public DocumentReference getNextNonEmptyChildren(DocumentReference documentRef) {
    DocumentReference nonEmptyChildRef = new NextNonEmptyChildrenCommand(
        ).getNextNonEmptyChildren(documentRef);
    if (nonEmptyChildRef != null) {
      return nonEmptyChildRef;
    }
    return documentRef;
  }

  public boolean isEmptyRTEDocument(DocumentReference docRef) {
    boolean isEmptyRTEdoc = true;
    for (String checkImplName : getCheckImplNamesConfig()) {
      if (emptyDocStrategies.keySet().contains(checkImplName)) {
        if (isEmptyRTEdoc) {
          isEmptyRTEdoc &= emptyDocStrategies.get(checkImplName).isEmptyRTEDocument(
              docRef);
        }
      } else if (!"".equals(checkImplName)) {
        LOGGER.warn("wrong checkImpleNames configuration in ["
            + getContext().getDatabase() + "] skipping implName [" + checkImplName
            + "].");
      }
    }
    return isEmptyRTEdoc;
  }

  public boolean isEmptyDocument(DocumentReference docRef) {
    boolean isEmptyRTEdoc = true;
    for (String checkImplName : getCheckImplNamesConfig()) {
      if (emptyDocStrategies.keySet().contains(checkImplName)) {
        if (isEmptyRTEdoc) {
          isEmptyRTEdoc &= emptyDocStrategies.get(checkImplName).isEmptyDocument(docRef);
        }
      } else if (!"".equals(checkImplName)) {
        LOGGER.warn("wrong checkImpleNames configuration in ["
            + getContext().getDatabase() + "] skipping implName [" + checkImplName
            + "].");
      }
    }
    return isEmptyRTEdoc;
  }

  List<String> getCheckImplNamesConfig() {
    String implConfigNames = getContext().getWiki().getXWikiPreference(
        "ceL_emptycheck_moduls", "celements.emptycheckModuls", "default", getContext());
    if ((implConfigNames != null) && (!"".equals(implConfigNames))) {
      return Arrays.asList(implConfigNames.split("[;,]"));
    } else {
      return Arrays.asList("default");
    }
  }

}
