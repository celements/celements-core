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

import com.celements.pagetype.classes.PageTypeClass;
import com.celements.pagetype.classes.PageTypePropertiesClass;

/**
 * @deprecated use {@link PageTypeClass} and {@link PageTypePropertiesClass}
 */
@Deprecated
@ComponentRole
public interface IPageTypeClassConfig {

  String PAGE_TYPE_PROPERTIES_CLASS_SPACE = "Celements2";
  String PAGE_TYPE_PROPERTIES_CLASS_DOC = "PageTypeProperties";
  String PAGE_TYPE_PROPERTIES_CLASS = PAGE_TYPE_PROPERTIES_CLASS_SPACE + "."
      + PAGE_TYPE_PROPERTIES_CLASS_DOC;

  String PAGE_TYPE_CLASS_SPACE = "Celements2";
  String PAGE_TYPE_CLASS_DOC = "PageType";
  String PAGE_TYPE_CLASS = PAGE_TYPE_CLASS_SPACE + "." + PAGE_TYPE_CLASS_DOC;
  String PAGE_TYPE_FIELD = "page_type";
  String PAGE_TYPE_LAYOUT_FIELD = "page_layout";
  String PAGETYPE_PROP_HASPAGETITLE = "haspagetitle";
  String PAGETYPE_PROP_RTE_HEIGHT = "rte_height";
  String PAGETYPE_PROP_RTE_WIDTH = "rte_width";
  String PAGETYPE_PROP_LOAD_RICHTEXT = "load_richtext";
  String PAGETYPE_PROP_SHOW_FRAME = "show_frame";
  String PAGETYPE_PROP_VISIBLE = "visible";
  String PAGETYPE_PROP_PAGE_VIEW = "page_view";
  String PAGETYPE_PROP_PAGE_EDIT = "page_edit";
  String PAGETYPE_PROP_CATEGORY = "category";
  String PAGETYPE_PROP_TYPE_NAME = "type_name";
  String PAGETYPE_PROP_IS_UNCONNECTED_PARENT = "unconnected_parent";
  String PAGETYPE_PROP_TAG_NAME = "tag_name";
  String PAGETYPE_PROP_INLINE_EDITOR_MODE = "inline_editor";

  DocumentReference getPageTypePropertiesClassRef(WikiReference wikiRef);

  DocumentReference getPageTypeClassRef();

  DocumentReference getPageTypeClassRef(WikiReference wikiRef);

}
