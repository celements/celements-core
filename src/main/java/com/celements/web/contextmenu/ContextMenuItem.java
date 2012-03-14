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

import com.celements.sajson.Builder;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

public class ContextMenuItem {
  
  public static String CONTEXTMENUITEM_CLASSNAME = "Celements2.ContextMenuItemClass";
  
  private String cmiLink;
  private String cmiText;
  private String cmiIcon;
  private String shortcut;

  private String elemId;

  public ContextMenuItem(BaseObject menuItem, String elemId,
      XWikiContext context) {
    String[] elemIdParts = elemId.split(":", -1);
    elemId = elemIdParts[elemIdParts.length - 1];
    VelocityContext vcontext = (VelocityContext) context.get("vcontext");
    vcontext.put("elemId", elemId);
    this.elemId = elemId;
    List<String> elemParams = Arrays.asList(elemIdParts).subList(0,
          elemIdParts.length - 1);
    vcontext.put("elemParams", elemParams);
    context.put("vcontext", vcontext.clone());
    cmiLink = renderText(menuItem.getLargeStringValue("cmi_link"), context);
    cmiText = renderText(menuItem.getStringValue("cmi_text"), context);
    cmiIcon = renderText(menuItem.getStringValue("cmi_icon"), context);
    shortcut = renderText(menuItem.getStringValue("cmi_shortcut"), context);
    context.put("vcontext", vcontext);
  }

  private String renderText(String velocityText, XWikiContext context) {
    if (context.getWiki() != null) {
      return context.getWiki().getRenderingEngine().interpretText(velocityText,
          context.getDoc(), context);
    } else {
      return velocityText;
    }
  }

  public void generateJSON(Builder builder) {
    builder.openDictionary();
    builder.addStringProperty("link", cmiLink);
    builder.addStringProperty("text", cmiText);
    builder.addStringProperty("icon", cmiIcon);
    addShortCutDictionary(builder);
    builder.closeDictionary();
  }

  private void addShortCutDictionary(Builder builder) {
    builder.openProperty("shortcut");
    builder.openDictionary();
    for (String sc_condition: shortcut.split("\\|")) {
      if (sc_condition.startsWith("keyCode")
          && (sc_condition.indexOf(':') > 0)) {
        String[] parts = sc_condition.split(":");
        builder.openProperty(parts[0]);
        builder.addNumber(new BigDecimal(Integer.parseInt(parts[1])));
      } else if (sc_condition.startsWith("charCode")
          && (sc_condition.indexOf(':') > 0)) {
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
    return cmiLink;
  }

  public String getText() {
    return cmiText;
  }

}
