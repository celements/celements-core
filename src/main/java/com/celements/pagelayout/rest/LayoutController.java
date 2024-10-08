package com.celements.pagelayout.rest;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.velocity.VelocityManager;

import com.celements.common.rest.RestPreconditions;
import com.celements.javascript.JsLoadMode;
import com.celements.metatag.MetaTag;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.reference.RefBuilder;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.web.plugin.cmd.CssCommand;
import com.celements.web.plugin.cmd.DocHeaderTitleCommand;
import com.celements.web.plugin.cmd.ExternalJavaScriptFilesCommand;
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
  @GetMapping(value = "/head", produces = MediaType.APPLICATION_JSON_VALUE)
  public HeaderData getHead(
      @RequestParam("space") String space,
      @RequestParam("doc") String doc)
      throws DocumentNotExistsException {
    var docRef = buildDocRef(space, doc);
    context.setDoc(modelAccess.getDocument(docRef));
    var h = new HeaderData();
    h.title = new DocHeaderTitleCommand().getDocHeaderTitle(docRef);
    h.language = context.getLanguage().orElseGet(() -> context.getDefaultLanguage(docRef));
    h.stylesheets = collectStylesheets().collect(Collectors.toList());
    h.scripts = collectScripts().collect(Collectors.toList());
    return h;
  }

  public class HeaderData {

    public String title;
    public String description;
    public String language;
    public URL favicon;
    public List<CssEntry> stylesheets;
    public List<JsEntry> scripts;
    public List<MetaTag> metaTags;

  }

  private Stream<CssEntry> collectStylesheets() {
    var cssCmd = new CssCommand();
    return CSS_FILES.stream()
        .flatMap(css -> cssCmd.includeCSSPage(css, context.getXWikiContext()).stream())
        .map(css -> {
          var cssEntry = new CssEntry();
          cssEntry.path = css.getCSS(context.getXWikiContext());
          cssEntry.alternate = css.isAlternate();
          cssEntry.title = css.getTitle();
          cssEntry.media = css.getMedia();
          return cssEntry;
        });
  }

  public class CssEntry {

    public String path;
    public boolean alternate;
    public String title;
    public String media;

  }

  private static final List<String> CSS_FILES = List.of(
      ":celRes/celements2.css",
      ":celRes/login.css",
      ":celJS/bootstrap/bootstrap-multiselect.css",
      ":celJS/jquery-datetimepicker/2.5/jquery.datetimepicker.min.css");

  private Stream<JsEntry> collectScripts() {
    var jsCmd = new ExternalJavaScriptFilesCommand();
    JS_FILES.forEach(jsCmd::addExtJSfileOnce);
    return jsCmd.collectJsFiles()
        .map(jsFile -> {
          var jsEntry = new JsEntry();
          jsEntry.path = jsFile.getFilepath();
          jsEntry.module = jsFile.isModule();
          jsEntry.mode = jsFile.getLoadMode();
          return jsEntry;
        });
  }

  // @JsonInclude(NON_EMPTY)
  public class JsEntry {

    public String path;
    public boolean module;
    public JsLoadMode mode;

  }

  private static final List<String> JS_FILES = List.of(
      ":celJS/prototype.js",
      ":celJS/jquery.min.js",
      ":celJS/jquery-noconflict.js",
      ":celJS/initCelements.min.js",
      ":celJS/validation.js",
      ":celJS/mobile/MobileSupport.js",
      ":celJS/scriptaculous/effects.js",
      ":celJS/bootstrap/bootstrap.min.js",
      ":celJS/bootstrap/bootstrap-multiselect.js",
      ":celJS/jquery-datetimepicker/2.5/jquery.datetimepicker.full.min.js",
      ":celJS/dateTimePicker/generateDateTimePicker.js",
      ":celJS/adminUi/overlayResize.js",
      ":celDynJS/DynamicLoader/celLazyLoader.mjs");

  @CrossOrigin(origins = "*")
  @GetMapping(value = "/json/{layoutSpaceName}", produces = MediaType.APPLICATION_JSON_VALUE)
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
