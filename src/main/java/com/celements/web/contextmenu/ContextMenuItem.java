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

import javax.annotation.concurrent.Immutable;

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

/**
 * NOTE: the current state of ContextMenuItem is only suitable for request based caching. It cannot
 * be used in system wide caches due to it's renderedX' fields.
 */
@Immutable
public class ContextMenuItem {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContextMenuItem.class);

  private final String elemId;
  private final String origElemId;
  private final List<String> elemIdParts;

  private final String link;
  private final String text;
  private final String icon;
  private final String shortcut;
  private String renderedLink;
  private String renderedText;
  private String renderedIcon;
  private String renderedShortcut;

  /**
   * @deprecated since 2.29 instead use new ContextmenuItem(BaseObject, String)
   */
  @Deprecated
  public ContextMenuItem(BaseObject menuItemObj, String elemId, XWikiContext context) {
    this(menuItemObj, elemId);
  }

  /**
   * @deprecated since 3.5
   */
  @Deprecated
  public ContextMenuItem(BaseObject menuItemObj, String elemId) {
    this(elemId, menuItemObj.getStringValue("cmi_link"), menuItemObj.getStringValue("cmi_text"),
        menuItemObj.getStringValue("cmi_icon"), menuItemObj.getStringValue("cmi_shortcut"));
  }

  public ContextMenuItem(String elemId, String link, String text, String icon, String shortcut) {
    this.origElemId = elemId;
    this.elemIdParts = Arrays.asList(elemId.split(":", -1));
    this.elemId = elemIdParts.get(elemIdParts.size() - 1);
    this.link = link;
    this.text = text;
    this.icon = icon;
    this.shortcut = shortcut;
    LOGGER.debug("ContextMenuItem created for elemId [{}]", elemId);
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
    if (renderedLink == null) {
      renderedLink = renderVelocityText(link);
    }
    return renderedLink;
  }

  public String getText() {
    if (renderedText == null) {
      renderedText = renderVelocityText(text);
    }
    return renderedText;
  }

  public String getCmiIcon() {
    if (renderedIcon == null) {
      renderedIcon = renderVelocityText(icon);
    }
    return renderedIcon;
  }

  public String getShortcut() {
    if (renderedShortcut == null) {
      renderedShortcut = renderVelocityText(shortcut);
    }
    return renderedShortcut;
  }

  private String renderVelocityText(String velocityText) {
    String text;
    try {
      text = getVelocityService().evaluateVelocityText(velocityText, getVelocityContextModifier());
    } catch (XWikiVelocityException exc) {
      LOGGER.warn("renderText: failed for '{}'", velocityText, exc);
      text = velocityText;
    }
    return Strings.nullToEmpty(text);
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
