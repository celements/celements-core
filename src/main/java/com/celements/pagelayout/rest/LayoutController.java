package com.celements.pagelayout.rest;

import java.util.Objects;

import javax.inject.Inject;

import org.apache.velocity.VelocityContext;
import org.python.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.velocity.VelocityManager;

import com.celements.common.rest.RestPreconditions;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.reference.RefBuilder;
import com.celements.pagelayout.LayoutServiceRole;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;

@RestController
@RequestMapping("/v1/layouts")
public class LayoutController {

  private static final Logger LOGGER = LoggerFactory.getLogger(LayoutController.class);

  private final LayoutServiceRole layoutService;
  private final IModelAccessFacade modelAccess;
  private final VelocityManager velocityManager;
  private final ModelContext context;

  @Inject
  public LayoutController(LayoutServiceRole layoutService, IModelAccessFacade modelAccess,
      VelocityManager velocityManager, ModelContext context) {
    this.layoutService = layoutService;
    this.modelAccess = modelAccess;
    this.velocityManager = velocityManager;
    this.context = context;
  }

  @CrossOrigin(origins = "*")
  @GetMapping(value = "/json/{layoutSpaceName}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public String renderLayoutAsJson(@PathVariable("layoutSpaceName") String layoutSpaceName) {
    return RestPreconditions.checkFound(layoutService.renderLayoutAsJson(
        buildSpaceRef(layoutSpaceName).build(SpaceReference.class)));
  }

  @CrossOrigin(origins = "*")
  @PostMapping(
      value = "/partial",
      produces = MediaType.APPLICATION_XML_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public String renderLayoutPartial(@RequestBody RenderPartialRequest renderPartialRequest) {
    LOGGER.info("partial api contextDoc '{}.{}', startDoc: {}.{}",
        renderPartialRequest.contextDocSpace, renderPartialRequest.contextDocName,
        renderPartialRequest.layoutSpace, renderPartialRequest.startNodeName);
    return modelAccess.getDocumentOpt(
        buildDocRef(renderPartialRequest.contextDocSpace, renderPartialRequest.contextDocName))
        .flatMap((XWikiDocument contextDoc) -> {
          context.setDoc(contextDoc);
          VelocityContext velocityContext = velocityManager.getVelocityContext();
          LOGGER.debug("partial doc in vcontext before is {}", context.getDocRef().orElse(null));
          velocityContext.put("doc",
              new Document(contextDoc, context.getXWikiContext()));
          return layoutService.renderLayoutPartial(
              buildDocRef(renderPartialRequest.layoutSpace,
                  renderPartialRequest.startNodeName));
        }).orElse("");
  }

  private DocumentReference buildDocRef(String space, String docName) {
    return buildSpaceRef(space)
        .doc(Objects.requireNonNull(Strings.emptyToNull(docName)))
        .build(DocumentReference.class);
  }

  private RefBuilder buildSpaceRef(String space) {
    return RefBuilder.from(context.getWikiRef())
        .space(Objects.requireNonNull(Strings.emptyToNull(space)));
  }

}
