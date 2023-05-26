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
@Component(CellAttributeClass.CLASS_DEF_HINT)
public class CellAttributeClass extends AbstractClassDefinition
    implements CellsClassDefinition, CelementsClassDefinition {

  public static final String DOC_NAME = "CellAttributeClass";
  public static final String CLASS_DEF_HINT = CelementsClassDefinition.SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_NAME = new StringField.Builder(CLASS_REF,
      "name").prettyName("name").build();

  public static final ClassField<String> FIELD_VALUE = new LargeStringField.Builder(
      CLASS_REF, "value").rows(5).prettyName("value (velocity interpreted)").build();

  public CellAttributeClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
