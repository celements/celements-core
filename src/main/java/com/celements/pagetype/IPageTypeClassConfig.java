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
package com.celements.pagetype;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface IPageTypeClassConfig {

  public static final String PAGE_TYPE_PROPERTIES_CLASS_SPACE = "Celements2";
  public static final String PAGE_TYPE_PROPERTIES_CLASS_DOC = "PageTypeProperties";
  public static final String PAGE_TYPE_PROPERTIES_CLASS = PAGE_TYPE_PROPERTIES_CLASS_SPACE + "."
      + PAGE_TYPE_PROPERTIES_CLASS_DOC;

  public static final String PAGE_TYPE_CLASS_SPACE = "Celements2";
  public static final String PAGE_TYPE_CLASS_DOC = "PageType";
  public static final String PAGE_TYPE_CLASS = PAGE_TYPE_CLASS_SPACE + "." + PAGE_TYPE_CLASS_DOC;
  public static final String PAGE_TYPE_FIELD = "page_type";
  public static final String PAGE_TYPE_LAYOUT_FIELD = "page_layout";
  public static final String PAGETYPE_PROP_HASPAGETITLE = "haspagetitle";
  public static final String PAGETYPE_PROP_RTE_HEIGHT = "rte_height";
  public static final String PAGETYPE_PROP_RTE_WIDTH = "rte_width";
  public static final String PAGETYPE_PROP_LOAD_RICHTEXT = "load_richtext";
  public static final String PAGETYPE_PROP_SHOW_FRAME = "show_frame";
  public static final String PAGETYPE_PROP_VISIBLE = "visible";
  public static final String PAGETYPE_PROP_PAGE_VIEW = "page_view";
  public static final String PAGETYPE_PROP_PAGE_EDIT = "page_edit";
  public static final String PAGETYPE_PROP_CATEGORY = "category";
  public static final String PAGETYPE_PROP_TYPE_NAME = "type_name";
  public static final String PAGETYPE_PROP_IS_UNCONNECTED_PARENT = "unconnected_parent";
  public static final String PAGETYPE_PROP_TAG_NAME = "tag_name";
  public static final String PAGETYPE_PROP_INLINE_EDITOR_MODE = "inline_editor";

  public DocumentReference getPageTypePropertiesClassRef(WikiReference wikiRef);

  public DocumentReference getPageTypeClassRef();

  public DocumentReference getPageTypeClassRef(WikiReference wikiRef);

}
