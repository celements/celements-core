package com.celements.navigation.presentation;

import static com.celements.model.util.ReferenceSerializationMode.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.ICellWriter;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.INavigation;
import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component
public class DefaultPresentationType implements IPresentationTypeRole<INavigation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPresentationType.class);

  private static final String _CEL_CM_NAV_MI_DEFAULT_CSSCLASS = "cel_cm_navigation_menuitem";

  @Requirement
  Execution execution;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  private ModelUtils modelUtils;

  MultilingualMenuNameCommand menuNameCmd = new MultilingualMenuNameCommand();

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public void writeNodeContent(ICellWriter writer, DocumentReference docRef,
      INavigation navigation) {
    writeNodeContent(writer.getAsStringBuilder(), false, false, docRef, true, 0, navigation);
  }

  @Override
  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem, INavigation navigation) {
    try {
      LOGGER.debug("writeNodeContent for [{}].", docRef);
      appendMenuItemLink(outStream, isFirstItem, isLastItem, docRef, isLeaf, numItem, navigation);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to writeNodeContent for docRef [{}].", docRef, exp);
    }
  }

  protected void appendMenuItemLink(StringBuilder outStream, boolean isFirstItem,
      boolean isLastItem, DocumentReference docRef, boolean isLeaf, int numItem, INavigation nav)
      throws XWikiException {
    String fullName = modelUtils.serializeRef(docRef, LOCAL);
    String tagName = (nav.hasLink() ? "a" : "span");
    String menuItemHTML = "<" + tagName;
    if (nav.hasLink()) {
      menuItemHTML += " href=\"" + nav.getMenuLink(docRef) + "\""
          + nav.getMenuLinkTarget(docRef).map(target -> " target=\"" + target + "\"").orElse("");
    }
    if (nav.useImagesForNavigation()) {
      menuItemHTML += " " + menuNameCmd.addNavImageStyle(fullName, nav.getNavLanguage(),
          getContext());
    }
    String tooltip = menuNameCmd.addToolTip(fullName, nav.getNavLanguage(), getContext());
    if (!"".equals(tooltip)) {
      menuItemHTML += " " + tooltip;
    }
    String menuName = menuNameCmd.getMultilingualMenuName(fullName, nav.getNavLanguage(),
        getContext());
    menuItemHTML += nav.addCssClasses(docRef, true, isFirstItem, isLastItem, isLeaf, numItem);
    menuItemHTML += " " + nav.addUniqueElementId(docRef) + ">" + menuName + "</" + tagName + ">";
    outStream.append(menuItemHTML);
  }

  @Override
  public String getDefaultCssClass() {
    return _CEL_CM_NAV_MI_DEFAULT_CSSCLASS;
  }

  @Override
  public String getEmptyDictionaryKey() {
    return "cel_nav_nomenuitems";
  }

  @Override
  public SpaceReference getPageLayoutForDoc(DocumentReference docRef) {
    return null;
  }

}
