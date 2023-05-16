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
package com.celements.cells.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.StringField;
import com.celements.web.classes.CelementsClassDefinition;
import com.google.errorprone.annotations.Immutable;

@Singleton
@Immutable
@Component(CellClass.CLASS_DEF_HINT)
public class CellClass extends AbstractClassDefinition
    implements CellsClassDefinition, CelementsClassDefinition {

  public static final String DOC_NAME = "CellClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_TAG_NAME = new StringField.Builder(CLASS_REF,
      "tagname").prettyName("tag name").build();

  public static final ClassField<String> FIELD_ID_NAME = new StringField.Builder(CLASS_REF,
      "idname").prettyName("id attribute").build();

  public static final ClassField<String> FIELD_CSS_CLASSES = new StringField.Builder(CLASS_REF,
      "css_classes").size(80).build();

  public static final ClassField<String> FIELD_CSS_STYLES = new LargeStringField.Builder(CLASS_REF,
      "css_styles").rows(20).size(15).build();

  public static final ClassField<String> FIELD_EVENT_DATA_ATTR = new LargeStringField.Builder(
      CLASS_REF, "event_data_attr").rows(20).size(15).prettyName("celEventJS data attribute")
          .build();

  public CellClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
