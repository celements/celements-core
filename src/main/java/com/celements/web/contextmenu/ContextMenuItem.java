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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.sajson.Builder;
import com.celements.velocity.VelocityContextModifier;
import com.celements.velocity.VelocityService;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class ContextMenuItem {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContextMenuItem.class);

  private final BaseObject menuItemObj;
  private final Map<String, String> renderCache;

  private final String elemId;
  private final String origElemId;
  private final List<String> elemIdParts;

  /**
   * @deprecated since 2.29 instead use new ContextmenuItem(BaseObject, String)
   */
  @Deprecated
  public ContextMenuItem(BaseObject menuItemObj, String elemId, XWikiContext context) {
    this(menuItemObj, elemId);
  }

  public ContextMenuItem(BaseObject menuItemObj, String elemId) {
    this.menuItemObj = menuItemObj;
    this.renderCache = new HashMap<>();
    this.origElemId = elemId;
    this.elemIdParts = Arrays.asList(elemId.split(":"));
    this.elemId = elemIdParts.get(elemIdParts.size() - 1);
    LOGGER.debug("ContextMenuItem created for [{}]: elemId = [{}]", menuItemObj, elemId);
  }

  public void generateJSON(Builder builder) {
    builder.openDictionary();
    builder.addStringProperty("link", getLink());
    builder.addStringProperty("text", getText());
    builder.addStringProperty("icon", getCmiIcon());
    addShortCutDictionary(builder);
    builder.closeDictionary();
  }

  private void addShortCutDictionary(Builder builder) {
    builder.openProperty("shortcut");
    builder.openDictionary();
    for (String sc_condition : getShortcut().split("\\|")) {
      if (sc_condition.startsWith("keyCode") && (sc_condition.indexOf(':') > 0)) {
        String[] parts = sc_condition.split(":");
        builder.openProperty(parts[0]);
        builder.addNumber(new BigDecimal(Integer.parseInt(parts[1])));
      } else if (sc_condition.startsWith("charCode") && (sc_condition.indexOf(':') > 0)) {
        String[] parts = sc_condition.split(":");
        builder.addStringProperty(parts[0], parts[1]);
      } else {
        builder.openProperty(sc_condition);
        builder.addBoolean(true);
      }
    }
    builder.closeDictionary();
  }

  public String getElemId() {
    return elemId;
  }

  public String getLink() {
    return renderVelocityTextFromObject("cmi_link");
  }

  public String getText() {
    return renderVelocityTextFromObject("cmi_text");
  }

  public String getCmiIcon() {
    return renderVelocityTextFromObject("cmi_icon");
  }

  public String getShortcut() {
    return renderVelocityTextFromObject("cmi_shortcut");
  }

  private String renderVelocityTextFromObject(String name) {
    String text = renderCache.get(name);
    if (text == null) {
      text = menuItemObj.getStringValue(name);
      try {
        text = getVelocityService().evaluateVelocityText(text, getVelocityContextModifier());
      } catch (XWikiVelocityException exc) {
        LOGGER.warn("renderText: failed for '{}'", text, exc);
      }
      renderCache.put(name, text = Strings.nullToEmpty(text));
    }
    return text;
  }

  private VelocityContextModifier getVelocityContextModifier() {
    return new VelocityContextModifier() {

      @Override
      public VelocityContext apply(VelocityContext vContext) {
        vContext.put("elemId", elemId);
        vContext.put("origElemId", origElemId);
        vContext.put("elemParams", elemIdParts.subList(0, elemIdParts.size() - 1));
        return vContext;
      }
    };
  }

  @Override
  public String toString() {
    return "ContextMenuItem [" + origElemId + "]";
  }

  private static VelocityService getVelocityService() {
    return Utils.getComponent(VelocityService.class);
  }

}
