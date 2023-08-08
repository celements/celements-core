package com.celements.velocity;

import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Strings.*;

import java.io.StringWriter;
import java.util.Optional;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.velocity.VelocityManager;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class DefaultVelocityService implements VelocityService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultVelocityService.class);

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  @Requirement
  private VelocityManager velocityManager;

  @Override
  public Optional<String> evaluate(String text) {
    return evaluate(text, null);
  }

  @Override
  public Optional<String> evaluate(String text, VelocityContextModifier contextModifier) {
    return context.getCurrentDoc().toJavaUtil()
        .flatMap(doc -> evaluateOpt(doc, text, contextModifier));
  }

  @Override
  public Optional<String> evaluate(XWikiDocument templateDoc) {
    return evaluate(templateDoc, null);
  }

  @Override
  public Optional<String> evaluate(XWikiDocument templateDoc,
      VelocityContextModifier contextModifier) {
    return evaluateOpt(templateDoc, templateDoc.getContent(), contextModifier);
  }

  private Optional<String> evaluateOpt(XWikiDocument templateDoc, String text,
      VelocityContextModifier contextModifier) {
    try {
      return Optional.ofNullable(emptyToNull(evaluateVelocity(templateDoc, text, contextModifier)));
    } catch (XWikiVelocityException exc) {
      LOGGER.warn("evaluate - failed", exc);
      return Optional.empty();
    }
  }

  @Override
  public String evaluateVelocityText(String text) throws XWikiVelocityException {
    return evaluateVelocityText(text, null);
  }

  @Override
  public String evaluateVelocityText(String text, VelocityContextModifier contextModifier)
      throws XWikiVelocityException {
    return context.getCurrentDoc().toJavaUtil()
        .map(rethrowFunction(doc -> evaluateVelocityText(doc, text, contextModifier)))
        .orElse("");
  }

  @Override
  public String evaluateVelocityText(XWikiDocument templateDoc, String text,
      VelocityContextModifier contextModifier) throws XWikiVelocityException {
    return evaluateVelocity(templateDoc, text, contextModifier);
  }

  private String evaluateVelocity(XWikiDocument templateDoc, String text,
      VelocityContextModifier contextModifier) throws XWikiVelocityException {
    checkNotNull(templateDoc);
    text = nullToEmpty(text).trim();
    if (text.isEmpty()) {
      return text;
    }
    VelocityContext vContext = (VelocityContext) velocityManager.getVelocityContext().clone();
    if (contextModifier != null) {
      vContext = contextModifier.apply(vContext);
    }
    StringWriter writer = new StringWriter();
    String templateName = modelUtils.serializeRef(templateDoc.getDocumentReference());
    velocityManager.getVelocityEngine().evaluate(vContext, writer, templateName, text);
    String result = writer.toString().trim();
    LOGGER.debug("evaluateVelocityText - for [{}], [{}]: {}", templateDoc, text, result);
    return result;
  }

}
