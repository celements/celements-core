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

import com.celements.common.classes.IClassesCompositorComponent;
import com.celements.web.classcollections.OldCoreClasses;
import com.celements.web.classcollections.OldCoreLegacyClasses;
import com.xpn.xwiki.web.Utils;

/**
 * @deprecated instead use {@link IClassesCompositorComponent}
 */
@Deprecated
public class CheckClassesCommand {

  // private static Log LOGGER = LogFactory.getFactory().getInstance(
  // CheckClassesCommand.class);

  @Deprecated
  public static final String CLASS_PANEL_CONFIG_CLASS = OldCoreClasses.PANEL_CONFIG_CLASS;

  @Deprecated
  public static final String MEDIALIB_CONFIG_CLASS_SPACE = OldCoreLegacyClasses.MEDIALIB_CONFIG_CLASS_SPACE;
  @Deprecated
  public static final String MEDIALIB_CONFIG_CLASS_NAME = OldCoreLegacyClasses.MEDIALIB_CONFIG_CLASS_DOC;
  @Deprecated
  public static final String MEDIALIB_CONFIG_CLASS = OldCoreLegacyClasses.MEDIALIB_CONFIG_CLASS;

  /**
   * @deprecated instead use
   *             {@link IClassesCompositorComponent#checkAllClassCollections()}
   */
  @Deprecated
  public void checkClasses() {
    IClassesCompositorComponent compComponent = Utils.getComponent(
        IClassesCompositorComponent.class);
    if (compComponent != null) {
      compComponent.checkClasses();
    }
  }

}
