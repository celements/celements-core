package com.celements.velocity;

import static com.google.common.base.Preconditions.*;

import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.velocity.VelocityManager;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class CelementsVelocityService implements VelocityService {

  private static Logger LOGGER = LoggerFactory.getLogger(CelementsVelocityService.class);

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  @Requirement
  private VelocityManager velocityManager;

  @Override
  public String evaluateVelocityText(String text) throws XWikiVelocityException {
    return evaluateVelocityText(context.getDoc(), text, getVelocityContextClone());
  }

  @Override
  public String evaluateVelocityText(XWikiDocument templateDoc, String text,
      VelocityContextModifier contextModifier) throws XWikiVelocityException {
    checkNotNull(templateDoc);
    VelocityContext vContext = getVelocityContextClone();
    if (contextModifier != null) {
      vContext = contextModifier.apply(vContext);
    }
    return evaluateVelocityText(templateDoc, text, vContext);
  }

  private VelocityContext getVelocityContextClone() {
    return (VelocityContext) velocityManager.getVelocityContext().clone();
  }

  private String evaluateVelocityText(XWikiDocument templateDoc, String text,
      VelocityContext vContext) throws XWikiVelocityException {
    StringWriter writer = new StringWriter();
    velocityManager.getVelocityEngine().evaluate(vContext, writer, modelUtils.serializeRef(
        checkNotNull(templateDoc).getDocumentReference()), Strings.nullToEmpty(text));
    String result = writer.toString();
    LOGGER.debug("evaluateVelocityText - for [{}], [{}]: {}", templateDoc, text, result);
    return result;
  }

}
