package com.celements.navigation.presentation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.celements.navigation.INavigation;
import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component
public class DefaultPresentationType implements IPresentationTypeRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DefaultPresentationType.class);

  private static final String _CEL_CM_NAV_MI_DEFAULT_CSSCLASS =
    "cel_cm_navigation_menuitem";

  @Requirement("local")
  EntityReferenceSerializer<String> serializer;

  @Requirement
  Execution execution;

  MultilingualMenuNameCommand menuNameCmd = new MultilingualMenuNameCommand();

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem,
      boolean isLastItem, DocumentReference docRef, boolean isLeaf,
      INavigation navigation) {
    try {
      appendMenuItemLink(outStream, isFirstItem, isLastItem, docRef, isLeaf, navigation);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to writeNodeContent for docRef [" + docRef + "].", exp);
    }
  }

  void appendMenuItemLink(StringBuilder outStream, boolean isFirstItem,
      boolean isLastItem, DocumentReference docRef, boolean isLeaf, INavigation nav
      ) throws XWikiException {
    String fullName = serializer.serialize(docRef);
    String tagName;
    if (nav.hasLink()) {
      tagName = "a";
    } else {
      tagName = "span";
    }
    String menuItemHTML = "<" + tagName;
    if (nav.hasLink()) {
      menuItemHTML += " href=\"" + nav.getMenuLink(docRef) + "\"";
    }
    if (nav.useImagesForNavigation()) {
      menuItemHTML += " " + menuNameCmd.addNavImageStyle(fullName, nav.getNavLanguage(),
          getContext());
    }
    String tooltip = menuNameCmd.addToolTip(fullName, nav.getNavLanguage(),
        getContext());
    if (!"".equals(tooltip)) {
      menuItemHTML += " " + tooltip;
    }
    String menuName = menuNameCmd.getMultilingualMenuName(fullName, nav.getNavLanguage(),
        getContext());
    menuItemHTML += nav.addCssClasses(docRef, true, isFirstItem, isLastItem, isLeaf);
    menuItemHTML += " " + nav.addUniqueElementId(docRef)
      + ">" + menuName + "</" + tagName + ">";
    outStream.append(menuItemHTML);
  }

  public String getDefaultCssClass() {
    return _CEL_CM_NAV_MI_DEFAULT_CSSCLASS;
  }

}
