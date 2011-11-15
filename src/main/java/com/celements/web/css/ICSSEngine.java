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
package com.celements.web.css;

import java.util.List;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

public interface ICSSEngine {

  /**
   * 
   * @param css
   * @param field
   * @param baseCSSList
   * @param context
   * @return the returned list is XWikiContext dependent and therefore may not be cached
   *         or similar. The list is as a consequence too not thread safe.
   *         TODO: Fix mix of API and backend. Extract business objects (controller)
   *         from CSS classes and use them here.
   */
  public List<CSS> includeCSS(String css, String field, List<BaseObject> baseCSSList, XWikiContext context);

}