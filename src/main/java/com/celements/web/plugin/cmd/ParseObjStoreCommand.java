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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;

public class ParseObjStoreCommand {
  private static Log mLogger = LogFactory.getFactory().getInstance(
      ParseObjStoreCommand.class);

  /**
   * Get the options (checkbox and radio buttons) saved using the ObjectSorage Action as
   * a Map.
   * @param options The String saved in the store object
   * @return Map containing all the 
   */
  public Map<String, String> getObjStoreOptionsMap(String options, XWikiContext context) {
    String optHash = getHash(options);
    Map<String, String> optMap = (Map<String, String>)context.get(optHash);
    if(optMap == null) {
      optMap = new HashMap<String, String>();
      if(options != null) {
        for(String line : options.split("\n")) {
          line = line.trim();
          if(line.length() > 0) {
            int radioSep = line.indexOf(":");
            int radioFirstIndexSep = line.indexOf(";");
            int radioLastIndexSep = line.lastIndexOf(";");
            if((radioSep >= 0) && (radioFirstIndexSep >= 0) 
                && (radioSep < radioFirstIndexSep)) {
              String key = line.substring(0, radioSep+1);
              key = key + line.substring(radioLastIndexSep+1);
              String value = line.substring(radioSep+1, radioFirstIndexSep);
              optMap.put(key, value);
            } else {
              optMap.put(line, line);
            }
          }
        }
      }
      context.put(optHash, optMap);
    }
    return optMap;
  }
  
  String getHash(String text) {
    String hash = "";
    if(text == null) {
      text = "";
    }
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update(text.getBytes());
      hash = new String(md.digest());
    } catch (NoSuchAlgorithmException e) {
      mLogger.error("SHA-1 algorithm not available", e);
    }
    return hash;
  } 
}
