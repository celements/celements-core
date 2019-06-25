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
package com.celements.metatag;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface MetaTagRole {

  boolean getOverridable();

  void setOverridable(@Nullable Boolean overridable);

  /**
   * Use discouraged, but Needed for BeanClassDefConverter to work. Please use {@link getKeyOpt()}
   */
  @Nullable
  String getKey();

  @NotNull
  Optional<String> getKeyOpt();

  void setKey(@Nullable String attributeKey);

  /**
   * Use discouraged, but Needed for BeanClassDefConverter to work. Please use {@link getValueOpt()}
   */
  @Nullable
  String getValue();

  @NotNull
  Optional<String> getValueOpt();

  void setValue(@Nullable String content);

  /**
   * Use discouraged, but Needed for BeanClassDefConverter to work. Please use {@link getLangOpt()}
   */
  @Nullable
  String getLang();

  @NotNull
  Optional<String> getLangOpt();

  void setLang(@Nullable String language);

  @NotNull
  String display();

}
