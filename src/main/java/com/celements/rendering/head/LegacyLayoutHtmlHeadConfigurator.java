package com.celements.rendering.head;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.javascript.ExtJsFileParameter;
import com.celements.javascript.JsLoadMode;
import com.celements.model.context.ModelContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWikiContext;

import one.util.streamex.StreamEx;

@Component(LegacyLayoutHtmlHeadConfigurator.NAME)
public class LegacyLayoutHtmlHeadConfigurator implements HtmlHeadConfiguratorRole {

  public static final String NAME = "legacyLayout";

  @Requirement
  private List<LegacyLayoutInitalJavaScriptLoaderRole> moduleJsLoaderList;

  @Requirement
  private ModelContext context;

  private static final ExtJsFileParameter.Builder extJSParam = new ExtJsFileParameter.Builder()
      .setAction("file");
  private static final ExtJsFileParameter.Builder extDeferJSParam = new ExtJsFileParameter.Builder()
      .setAction("file").setLoadMode(JsLoadMode.DEFER);

  private static final Set<ExtJsFileParameter> DEFAULT_JS_FILES = ImmutableSet.of(
      extJSParam.setJsFileRef(":celJS/prototype.js").build(),
      extJSParam.setJsFileRef(":celJS/jquery.min.js").build(),
      extJSParam.setJsFileRef(":celJS/jquery-noconflict.js").build(),
      extJSParam.setJsFileRef(":celJS/initCelements.min.js").build(),
      extDeferJSParam.setJsFileRef(":celJS/mobile/MobileSupport.js").build(),
      extDeferJSParam.setJsFileRef(":celJS/scriptaculous/effects.js").build(),
      extDeferJSParam.setJsFileRef(":celJS/validation.js").build(),
      extDeferJSParam.setJsFileRef(":celJS/bootstrap/bootstrap.min.js").build(),
      extDeferJSParam.setJsFileRef(":celJS/bootstrap/bootstrap-multiselect.js").build(),
      extDeferJSParam
          .setJsFileRef(":celJS/jquery-datetimepicker/2.5/jquery.datetimepicker.full.min.js").build(),
      extDeferJSParam.setJsFileRef(":celJS/dateTimePicker/generateDateTimePicker.js").build(),
      extDeferJSParam.setJsFileRef(":celJS/bootstrap/bootstrap-multiselect.js").build());

  private static final ExtJsFileParameter SWF_OBJECT_JS = extDeferJSParam
      .setJsFileRef(":celJS/SWFObject-2.2/swfobject.js").build();

  @Override
  public List<ExtJsFileParameter> getAllInitialJavaScriptFiles() {
    Stream<ExtJsFileParameter> jsFileParamStream = StreamEx.of(DEFAULT_JS_FILES)
        .append(getSwfJavaScript())
        .append(moduleJsLoaderList.stream()
            .flatMap(LegacyLayoutInitalJavaScriptLoaderRole::getModuleInitialJavaScriptFiles));
    return jsFileParamStream.collect(ImmutableList.toImmutableList());
  }

  private Stream<ExtJsFileParameter> getSwfJavaScript() {
    if (getXContext().getWiki().getXWikiPreferenceAsInt("cel_disable_swfobject", 0,
        getXContext()) == 0) {
      return Stream.of(SWF_OBJECT_JS);
    }
    return Stream.empty();
  }

  private XWikiContext getXContext() {
    return context.getXWikiContext();
  }

}
