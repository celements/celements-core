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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.sajson.Builder;
import com.celements.velocity.VelocityContextModifier;
import com.celements.velocity.VelocityService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class ContextMenuItem {

  public static ThreadLocal<AtomicLong> RENDER_TIME = ThreadLocal.withInitial(
      new Supplier<AtomicLong>() {

        @Override
        public AtomicLong get() {
          return new AtomicLong(0L);
        }
      });

  public static String CONTEXTMENUITEM_CLASSNAME = "Celements2.ContextMenuItemClass";

  private static Logger LOGGER = LoggerFactory.getLogger(ContextMenuItem.class);

  private String cmiLink;
  private String cmiText;
  private String cmiIcon;
  private String shortcut;

  private String elemId;
  private String origElemId;

  private String[] elemIdParts;

  /**
   * @deprecated since 2.29 instead use new ContextmenuItem(BaseObject, String)
   */
  @Deprecated
  public ContextMenuItem(BaseObject menuItem, String elemId, XWikiContext context) {
    this(menuItem, elemId);
  }

  public ContextMenuItem(BaseObject menuItem, String elemId) {
    origElemId = elemId;
    elemIdParts = elemId.split(":", -1);
    elemId = elemIdParts[elemIdParts.length - 1];
    this.elemId = elemId;
    cmiLink = menuItem.getLargeStringValue("cmi_link");
    cmiText = menuItem.getStringValue("cmi_text");
    cmiIcon = menuItem.getStringValue("cmi_icon");
    shortcut = menuItem.getStringValue("cmi_shortcut");
    LOGGER.debug("ContextMenuItem created for [{}]: elemId = [{}]", menuItem, elemId);
  }

  private XWikiContext getContext() {
    return (XWikiContext) getExecution().getContext().getProperty("xwikicontext");
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

  private String renderText(String velocityText) {
    String rendered;
    try {
      long time = System.currentTimeMillis();
      rendered = getVelocityService().evaluateVelocityText(getContext().getDoc(), velocityText,
          getVelocityContextModifier());
      RENDER_TIME.get().addAndGet(System.currentTimeMillis() - time);
    } catch (XWikiVelocityException exc) {
      LOGGER.warn("renderText: failed for '{}'", velocityText, exc);
      rendered = velocityText;
    }
    return rendered;
  }

  private VelocityContextModifier getVelocityContextModifier() {
    return new VelocityContextModifier() {

      @Override
      public VelocityContext apply(VelocityContext vContext) {
        vContext.put("elemId", elemId);
        vContext.put("origElemId", origElemId);
        List<String> elemParams = Arrays.asList(elemIdParts).subList(0, elemIdParts.length - 1);
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
    return renderText(cmiLink);
  }

  public String getText() {
    return renderText(cmiText);
  }

  public String getCmiIcon() {
    return renderText(cmiIcon);
  }

  public String getShortcut() {
    return renderText(shortcut);
  }

  @Override
  public String toString() {
    return "ContextMenuItem [origElemId=" + origElemId + ", cmiLink=" + cmiLink + ", cmiText="
        + cmiText + ", cmiIcon=" + cmiIcon + ", shortcut=" + shortcut + "]";
  }

  private VelocityService getVelocityService() {
    return Utils.getComponent(VelocityService.class);
  }

}
