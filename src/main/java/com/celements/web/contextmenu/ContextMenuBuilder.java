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

import javax.annotation.concurrent.NotThreadSafe;

import org.codehaus.jackson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.sajson.AbstractEventHandler;
import com.celements.sajson.Builder;
import com.celements.sajson.Parser;
import com.celements.web.classcollections.OldCoreClasses;
import com.google.common.collect.FluentIterable;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@NotThreadSafe
public class ContextMenuBuilder {

  public class CMRequestHandler extends AbstractEventHandler<ERequestLiteral> {

    private String cssClassName;
    private ERequestLiteral currentLiteral;
    private final Map<String, List<ContextMenuItem>> contextMenus;

    public CMRequestHandler(Map<String, List<ContextMenuItem>> outputCMmap) {
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
          getCurrentMenu(value).addAll(getCMItems(cssClassName, value));
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

  private static final Logger LOGGER = LoggerFactory.getLogger(ContextMenuBuilder.class);

  private final Map<String, List<ContextMenuItem>> contextMenus = new HashMap<>();

  List<ContextMenuItem> getCMItems(String className, String elemId) {
    ArrayList<ContextMenuItem> contextMenuItemList = new ArrayList<>();
    for (BaseObject menuItem : getCMObjects(className)) {
      ContextMenuItem cmItem = new ContextMenuItem(menuItem, elemId);
      if (!"".equals(cmItem.getLink().trim()) && !"".equals(cmItem.getText().trim())) {
        contextMenuItemList.add(cmItem);
      }
    }
    return contextMenuItemList;
  }

  private FluentIterable<BaseObject> getCMObjects(String className) {
    FluentIterable<BaseObject> objs = FluentIterable.of();
    RefBuilder refBuilder = new RefBuilder().doc(className).space("CelementsContextMenu");
    objs = objs.append(getObjectFetcher(refBuilder.wiki("celements2web").build(
        DocumentReference.class)).iter());
    objs = objs.append(getObjectFetcher(refBuilder.with(getContext().getWikiRef()).build(
        DocumentReference.class)).iter());
    return objs;
  }

  public void addElementsCMforClassNames(String jsonDictionary) {
    long time = System.currentTimeMillis();
    if ((jsonDictionary != null) && !"".equals(jsonDictionary)) {
      CMRequestHandler requestHandler = new CMRequestHandler(contextMenus);
      Parser cmReqParser = Parser.createLexicalParser(ERequestLiteral.REQUEST_ARRAY,
          requestHandler);
      try {
        cmReqParser.parse(jsonDictionary);
      } catch (JsonParseException exp) {
        LOGGER.error("addElementsCMforClassNames: failed to parse [" + jsonDictionary + "].", exp);
      } catch (IOException exp) {
        LOGGER.error("Failed to parse json.", exp);
      } finally {
        LOGGER.debug("addElementsCMforClassNames: took {}ms", (System.currentTimeMillis() - time));
      }
    }
  }

  public String getJson() {
    long time = System.currentTimeMillis();
    try {
      Builder jsonBuilder = new Builder();
      jsonBuilder.openArray();
      for (String elemId : contextMenus.keySet()) {
        if (!"".equals(elemId)) {
          jsonBuilder.openDictionary();
          jsonBuilder.addStringProperty("elemId", elemId);
          jsonBuilder.openProperty("cmItems");
          jsonBuilder.openArray();
          for (ContextMenuItem cmi : contextMenus.get(elemId)) {
            cmi.generateJSON(jsonBuilder);
          }
          jsonBuilder.closeArray();
          jsonBuilder.closeDictionary();
        }
      }
      jsonBuilder.closeArray();
      return jsonBuilder.getJSON();
    } finally {
      LOGGER.debug("getJson: took {}ms", (System.currentTimeMillis() - time));
    }
  }

  private XWikiObjectFetcher getObjectFetcher(DocumentReference docRef) {
    return XWikiObjectFetcher.on(getModelAccess().getOrCreateDocument(docRef)).filter(
        OldCoreClasses.getContextMenuItemClassRef());
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  private ModelContext getContext() {
    return Utils.getComponent(ModelContext.class);
  }

}
