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
package com.celements.web.plugin.cmd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

public class ImageMapCommand {

  public static final String IMG_MAP_CONFIG_SET = "imageMapConfigSet";

  private static Log mLogger = LogFactory.getFactory().getInstance(
      ImageMapCommand.class);
  
  private XWikiContext context;
  private Set<String> imageMapSet;

  private Pattern useMapsPattern = Pattern.compile("<img [^>]*?usemap=\"([^\"]*)\"",
      Pattern.CASE_INSENSITIVE);
  
  public ImageMapCommand(XWikiContext context) {
    this.context = context;
    imageMapSet = new HashSet<String>();
  }

  public void addMapConfig(String mapId) {
    String hql = "select m.map, m.lang from Classes.ImageMapConfigClass as m " +
        "where m.map_id='" + mapId + "'";
    List<Object[]> mapList = null;
    try {
      mapList = context.getWiki().search(hql, context);
    } catch (XWikiException e) {
      mLogger.error("Error searching for image map config", e);
    }
    if(mapList != null) {
      if(mapList.size() == 1) {
        imageMapSet.add((String)mapList.get(0)[0]);
      } else if(mapList.size() > 1) {
        String mapValue = "";
        for (Object[] map : mapList) {
          if(context.getLanguage().equals(map[1])) {
            mapValue = (String)map[0];
            break;
          } else if(context.getWiki().getWebPreference("default_language", context)
              .equals(map[1])) {
            mapValue = (String)map[0];
          }
        }
        if(mapValue.trim().length() > 0) {
          imageMapSet.add(mapValue);
        }
      }
    }
  }
  
  public String displayAllImageMapConfigs() {
    String maps = "";
    for (String map : imageMapSet) {
      maps += map;
    }
    return maps;
  }

  public List<String> getImageUseMaps(String rteContent) {
    List<String> useMaps = new ArrayList<String>();
    if (rteContent != null) {
      Matcher theMatcher = useMapsPattern.matcher(rteContent);
      while (theMatcher.find()) {
        String useMapStr = theMatcher.group(1);
        if ((useMapStr != null) && (!"".equals(useMapStr))) {
          useMaps.add(useMapStr.replace("#", ""));
        }
      }
    }
    return useMaps;
  }
}