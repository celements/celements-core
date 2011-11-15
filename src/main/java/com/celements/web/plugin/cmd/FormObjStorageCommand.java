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

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class FormObjStorageCommand {

  private static final int _MAX_OBJ_ON_DOC = 500;
  private static Log mLogger = LogFactory.getFactory().getInstance(
      FormObjStorageCommand.class);

  public BaseObject newObject(XWikiDocument storageDoc, String className,
      XWikiContext context) {
    Vector<BaseObject> objects = storageDoc.getObjects(className);
    if ((objects != null) && (objects.size() > _MAX_OBJ_ON_DOC)) {
      mLogger.warn("PERFORMANCE WARNING! There are more than " + _MAX_OBJ_ON_DOC
          + " objects of the class [" + className + "] on storageDoc ["
          + storageDoc.getFullName() + "].");
    }
    try {
      return storageDoc.newObject(className, context);
    } catch (XWikiException exp) {
      mLogger.error("Failed to create new object [" + className + "] on document [" 
          + storageDoc.getFullName() + "].", exp);
    }
    return null;
  }

}
