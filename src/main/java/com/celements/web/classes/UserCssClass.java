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
package com.celements.web.classes;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.single.EnumSingleListField;
import com.celements.web.classes.helper.CssMediaType;

@Component(UserCssClass.CLASS_DEF_HINT)
public class UserCssClass extends AbstractClassDefinition implements CelementsClassDefinition {

  public static final String SPACE_NAME = "Skins";
  public static final String DOC_NAME = "UserCSS";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_CSSNAME = new StringField.Builder(CLASS_DEF_HINT,
      "cssname").size(30).build();

  public static final ClassField<Boolean> FIELD_ALTERNATE = new BooleanField.Builder(CLASS_DEF_HINT,
      "alternate").displayType("yesno").build();

  public static final ClassField<String> FIELD_TITLE = new StringField.Builder(CLASS_DEF_HINT,
      "title").size(30).build();

  public static final ClassField<Boolean> FIELD_IS_RTE_CONTENT = new BooleanField.Builder(
      CLASS_DEF_HINT, "is_rte_content").displayType("yesno").build();

  public static final ClassField<CssMediaType> FIELD_MEDIA = new EnumSingleListField.Builder<>(
      CLASS_DEF_HINT, "media", CssMediaType.class).build();

  @Override
  public String getName() {
    return CLASS_DEF_HINT;
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

  @Override
  protected String getClassSpaceName() {
    return SPACE_NAME;
  }

  @Override
  protected String getClassDocName() {
    return DOC_NAME;
  }

}
