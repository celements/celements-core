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
package com.celements.pagetype.classes;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.number.IntField;

@Component(PageTypePropertiesClass.CLASS_DEF_HINT)
public class PageTypePropertiesClass extends AbstractClassDefinition implements
    PageTypeClassDefinition {

  public static final String SPACE_NAME = "Celements2";
  public static final String DOC_NAME = "PageTypeProperties";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> PAGETYPE_PROP_TYPE_NAME = new StringField.Builder(
      CLASS_REF, "type_name").size(30).prettyName("Type Pretty Name").build();
  public static final ClassField<String> PAGETYPE_PROP_CATEGORY = new StringField.Builder(
      CLASS_REF, "category").size(30).prettyName("Category").build();
  public static final ClassField<String> PAGETYPE_PROP_PAGE_EDIT = new StringField.Builder(
      CLASS_REF, "page_edit").size(30).prettyName("Type Edit Template").build();
  public static final ClassField<String> PAGETYPE_PROP_PAGE_VIEW = new StringField.Builder(
      CLASS_REF, "page_view").size(30).prettyName("Type View Template").build();
  public static final ClassField<Boolean> PAGETYPE_PROP_VISIBLE = new BooleanField.Builder(
      CLASS_REF, "visible").prettyName("Visible").displayType("yesno").build();
  public static final ClassField<Boolean> PAGETYPE_PROP_SHOW_FRAME = new BooleanField.Builder(
      CLASS_REF, "show_frame").prettyName("Show Frame").displayType("yesno").build();
  public static final ClassField<Boolean> PAGETYPE_PROP_LOAD_RICHTEXT = new BooleanField.Builder(
      CLASS_REF, "load_richtext").prettyName("Load Richtext Editor").displayType(
          "yesno").build();
  public static final ClassField<Integer> PAGETYPE_PROP_RTE_WIDTH = new IntField.Builder(
      CLASS_REF, "rte_width").prettyName("Richtext Editor Width").size(30).build();
  public static final ClassField<Integer> PAGETYPE_PROP_RTE_HEIGHT = new IntField.Builder(
      CLASS_REF, "rte_height").prettyName("Richtext Editor Height").size(30).build();
  public static final ClassField<Boolean> PAGETYPE_PROP_HASPAGETITLE = new BooleanField.Builder(
      CLASS_REF, "haspagetitle").prettyName("Has Page Title").displayType("yesno").build();
  public static final ClassField<Boolean> PAGETYPE_PROP_IS_UNCONNECTED_PARENT = new BooleanField.Builder(
      CLASS_REF, "unconnected_parent").prettyName("Is Unconnected Parent").displayType(
          "yesno").build();
  public static final ClassField<String> PAGETYPE_PROP_TAG_NAME = new StringField.Builder(
      CLASS_REF, "tag_name").size(30).prettyName("Tag Name").build();
  public static final ClassField<Boolean> PAGETYPE_PROP_INLINE_EDITOR_MODE = new BooleanField.Builder(
      CLASS_REF, "inline_editor").prettyName("Use Inline Editor Mode").displayType(
          "yesno").build();

  public PageTypePropertiesClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }
}
