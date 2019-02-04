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
import java.util.List;

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

  private String link;
  private final String vLink;
  private String text;
  private final String vText;
  private String icon;
  private final String vIcon;
  private String shortcut;
  private final String vShortcut;

  private final String elemId;
  private final String origElemId;

  private final List<String> elemIdParts;

  /**
   * @deprecated since 2.29 instead use new ContextmenuItem(BaseObject, String)
   */
  @Deprecated
  public ContextMenuItem(BaseObject menuItem, String elemId, XWikiContext context) {
    this(menuItem, elemId);
  }

  public ContextMenuItem(BaseObject menuItem, String elemId) {
    origElemId = elemId;
    elemIdParts = Arrays.asList(elemId.split(":", -1));
    elemId = elemIdParts.get(elemIdParts.size() - 1);
    this.elemId = elemId;
    vLink = menuItem.getLargeStringValue("cmi_link");
    vText = menuItem.getStringValue("cmi_text");
    vIcon = menuItem.getStringValue("cmi_icon");
    vShortcut = menuItem.getStringValue("cmi_shortcut");
    LOGGER.debug("ContextMenuItem created for [{}]: elemId = [{}]", menuItem, elemId);
  }

  private String renderText(String velocityText) {
    String ret;
    try {
      long time = System.currentTimeMillis();
      ret = getVelocityService().evaluateVelocityText(velocityText, getVelocityContextModifier());
      if ((System.currentTimeMillis() - time) > 1) {
        LOGGER.error("renderText: took {}ms for '{}'", (System.currentTimeMillis() - time),
            origElemId);
      }
    } catch (XWikiVelocityException exc) {
      LOGGER.warn("renderText: failed for '{}'", velocityText, exc);
      ret = velocityText;
    }
    return Strings.nullToEmpty(ret);
  }

  private VelocityContextModifier getVelocityContextModifier() {
    return new VelocityContextModifier() {

      @Override
      public VelocityContext apply(VelocityContext vContext) {
        vContext.put("elemId", elemId);
        vContext.put("origElemId", origElemId);
        List<String> elemParams = elemIdParts.subList(0, elemIdParts.size() - 1);
        vContext.put("elemParams", elemParams);
        return vContext;
      }
    };
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
    if (link == null) {
      link = renderText(vLink);
    }
    return link;
  }

  public String getText() {
    if (text == null) {
      text = renderText(vText);
    }
    return text;
  }

  public String getCmiIcon() {
    if (icon == null) {
      icon = renderText(vIcon);
    }
    return icon;
  }

  public String getShortcut() {
    if (shortcut == null) {
      shortcut = renderText(vShortcut);
    }
    return shortcut;
  }

  @Override
  public String toString() {
    return "ContextMenuItem [" + origElemId + "]";
  }

  private static VelocityService getVelocityService() {
    return Utils.getComponent(VelocityService.class);
  }

}
