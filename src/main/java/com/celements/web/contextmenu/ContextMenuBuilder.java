/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.contextmenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.codehaus.jackson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.sajson.AbstractEventHandler;
import com.celements.sajson.Builder;
import com.celements.sajson.Parser;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;

public class ContextMenuBuilder {

  public class CMRequestHandler extends AbstractEventHandler<ERequestLiteral> {

    private String cssClassName;
    private ERequestLiteral currentLiteral;
    private Map<String, List<ContextMenuItem>> contextMenus;
    private XWikiContext context;

    public CMRequestHandler(Map<String, List<ContextMenuItem>> outputCMmap, XWikiContext context) {
      this.context = context;
      this.contextMenus = outputCMmap;
    }

    @Override
    public void closeEvent(ERequestLiteral literal) {
      switch (literal) {
        case CSS_CLASS:
          cssClassName = null;
          break;
        default:
          break;
      }
      currentLiteral = null;
    }

    @Override
    public void openEvent(ERequestLiteral literal) {
      switch (literal) {
        case REQUEST_ARRAY:
          break;
        default:
          break;
      }
      currentLiteral = literal;
    }

    @Override
    public void readPropertyKey(String key) {
    }

    @Override
    public void stringEvent(String value) {
      LOGGER.debug("stringEvent: " + currentLiteral + ", " + value);
      switch (currentLiteral) {
        case CSS_CLASS_NAME_VALUE:
          cssClassName = value;
          break;
        case ELEMENT_ID:
          LOGGER.debug("before adding cmi for " + cssClassName + ", " + value + ": "
              + getCurrentMenu(value).size());
          getCurrentMenu(value).addAll(getCMItemsForClassAndId(cssClassName, value, context));
          LOGGER.debug("after adding cmi for " + cssClassName + ", " + value + ": "
              + getCurrentMenu(value).size());
          break;
        default:
          break;
      }
    }

    private List<ContextMenuItem> getCurrentMenu(String elemId) {
      if (!contextMenus.containsKey(elemId)) {
        contextMenus.put(elemId, new ArrayList<ContextMenuItem>());
      }
      return contextMenus.get(elemId);
    }

    public Map<String, List<ContextMenuItem>> getContextMenus() {
      return contextMenus;
    }

  }

  /**
   * class ContextMenuBuilder
   */

  private static Logger LOGGER = LoggerFactory.getLogger(ContextMenuBuilder.class);

  private Map<String, List<ContextMenuItem>> contextMenus = new HashMap<>();

  List<ContextMenuItem> getCMItemsForClassAndId(String className, String elemId,
      XWikiContext context) {
    ArrayList<ContextMenuItem> contextMenuItemList = new ArrayList<>();
    try {
      for (Object theobj : getCMIobjects(className, context)) {
        if (theobj instanceof BaseObject) {
          BaseObject menuItem = (BaseObject) theobj;
          ContextMenuItem cmItem = new ContextMenuItem(menuItem, elemId, context);
          if (!"".equals(cmItem.getLink().trim()) && !"".equals(cmItem.getText().trim())) {
            contextMenuItemList.add(cmItem);
          }
        }
      }
    } catch (XWikiException e) {
      LOGGER.error("getCMItemsForClassAndId: failed to evaluate CMItems for className [" + className
          + "] and elemId [" + elemId + "].", e);
    }
    return contextMenuItemList;
  }

  @SuppressWarnings("unchecked")
  private List getCMIobjects(String className, XWikiContext context) throws XWikiException {
    String fullName = "CelementsContextMenu." + className;
    Vector cmiObjects = new Vector();
    if (context.getWiki().exists("celements2web:" + fullName, context)) {
      cmiObjects.addAll(context.getWiki().getDocument("celements2web:" + fullName,
          context).getObjects(ContextMenuItem.CONTEXTMENUITEM_CLASSNAME));
    }
    if (context.getWiki().exists(fullName, context)) {
      cmiObjects.addAll(context.getWiki().getDocument(fullName, context).getObjects(
          ContextMenuItem.CONTEXTMENUITEM_CLASSNAME));
    }
    return cmiObjects;
  }

  private void addJSONforCM(String elemId, Builder jsonBuilder) {
    LOGGER.error("addJSONforCM: '{}' start", elemId);
    long time = System.currentTimeMillis();
    jsonBuilder.openDictionary();
    jsonBuilder.addStringProperty("elemId", elemId);
    jsonBuilder.openProperty("cmItems");
    jsonBuilder.openArray();
    for (ContextMenuItem cmi : contextMenus.get(elemId)) {
      cmi.generateJSON(jsonBuilder);
    }
    jsonBuilder.closeArray();
    jsonBuilder.closeDictionary();
    LOGGER.error("addJSONforCM: '{}' took {}ms", elemId, (System.currentTimeMillis() - time));
  }

  public void addElementsCMforClassNames(String jsonDictionary, XWikiContext context) {
    if ((jsonDictionary != null) && !"".equals(jsonDictionary)) {
      LOGGER.error("addElementsCMforClassNames: start");
      long time = System.currentTimeMillis();
      CMRequestHandler requestHandler = new CMRequestHandler(contextMenus, context);
      Parser cmReqParser = Parser.createLexicalParser(ERequestLiteral.REQUEST_ARRAY,
          requestHandler);
      try {
        cmReqParser.parse(jsonDictionary);
      } catch (JsonParseException exp) {
        LOGGER.error("addElementsCMforClassNames: failed to parse [" + jsonDictionary + "].", exp);
      } catch (IOException exp) {
        LOGGER.error("Failed to parse json.", exp);
      } finally {
        LOGGER.error("addElementsCMforClassNames: took {}s", (System.currentTimeMillis() - time)
            / 1000d);
      }
    }
  }

  @Deprecated
  public String getCMIjson(XWikiContext context) {
    return getJson();
  }

  public String getJson() {
    LOGGER.error("getJson: start");
    long time = System.currentTimeMillis();
    try {
      Builder jsonBuilder = new Builder();
      jsonBuilder.openArray();
      for (String elemId : contextMenus.keySet()) {
        if (!"".equals(elemId)) {
          addJSONforCM(elemId, jsonBuilder);
        }
      }
      jsonBuilder.closeArray();
      return jsonBuilder.getJSON();
    } finally {
      LOGGER.error("getJson: took {}s", (System.currentTimeMillis() - time) / 1000d);
      LOGGER.error("{}", contextMenus);
    }
  }

}
