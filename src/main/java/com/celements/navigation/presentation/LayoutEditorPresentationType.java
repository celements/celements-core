package com.celements.navigation.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.CellsClasses;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.navigation.INavigation;
import com.celements.navigation.cmd.MultilingualMenuNameCommand;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("layoutEditor")
public class LayoutEditorPresentationType extends DefaultPresentationType {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      LayoutEditorPresentationType.class);

  private static final String _CEL_CM_CELLEDITOR_MENUITEM = "cel_cm_celleditor_menuitem";

  @Requirement("celements.celCellsClasses")
  IClassCollectionRole cellsClasses;

  MultilingualMenuNameCommand menuNameCmd = new MultilingualMenuNameCommand();

  CellsClasses getCellsClasses() {
    return (CellsClasses) cellsClasses;
  }

  @Override
  public String getDefaultCssClass() {
    return _CEL_CM_CELLEDITOR_MENUITEM;
  }

  @Override
  public String getEmptyDictionaryKey() {
    return "cel_layout_nocells";
  }

  @Override
  protected void appendMenuItemLink(StringBuilder outStream, boolean isFirstItem,
      boolean isLastItem, DocumentReference docRef, boolean isLeaf, int numItem, INavigation nav)
      throws XWikiException {
    StringBuilder menuItemHTML = new StringBuilder();
    String fullName = webUtilsService.getRefLocalSerializer().serialize(docRef);
    menuItemHTML.append("<");
    String tagName;
    if (nav.hasLink()) {
      tagName = "a";
    } else {
      tagName = "span";
    }
    menuItemHTML.append(tagName);
    if (nav.hasLink()) {
      menuItemHTML.append(" href=\"");
      menuItemHTML.append(nav.getMenuLink(docRef));
      menuItemHTML.append("\"");
    }
    if (nav.useImagesForNavigation()) {
      menuItemHTML.append(" ");
      menuItemHTML.append(menuNameCmd.addNavImageStyle(fullName, nav.getNavLanguage(),
          getContext()));
    }
    String tooltip = menuNameCmd.addToolTip(fullName, nav.getNavLanguage(), getContext());
    if (!Strings.isNullOrEmpty(tooltip)) {
      menuItemHTML.append(" ");
      menuItemHTML.append(tooltip);
    }
    String menuName = menuNameCmd.getMultilingualMenuName(fullName, nav.getNavLanguage(),
        getContext());
    menuItemHTML.append(nav.addCssClasses(docRef, true, isFirstItem, isLastItem, isLeaf, numItem));
    menuItemHTML.append(" ");
    menuItemHTML.append(nav.addUniqueElementId(docRef));
    menuItemHTML.append(">");
    menuItemHTML.append(menuName);
    menuItemHTML.append(getIdName(docRef));
    menuItemHTML.append("</");
    menuItemHTML.append(tagName);
    menuItemHTML.append(">");
    outStream.append(menuItemHTML);
  }

  BaseObject getCellProperties(DocumentReference docRef) {
    XWikiDocument theDoc;
    try {
      theDoc = getContext().getWiki().getDocument(docRef, getContext());
      return theDoc.getXObject(getCellsClasses().getCellClassRef(getContext().getDatabase()));
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get document [" + docRef + "].", exp);
    }
    return null;
  }

  String getIdName(DocumentReference docRef) {
    BaseObject cellPropertiesObj = getCellProperties(docRef);
    if (cellPropertiesObj != null) {
      String idName = cellPropertiesObj.getStringValue(CellsClasses.CELLCLASS_IDNAME_FIELD);
      if (!StringUtils.isEmpty(idName)) {
        return " (#" + idName + ")";
      }
    }
    return "";
  }

}
