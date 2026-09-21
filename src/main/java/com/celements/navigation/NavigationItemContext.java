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
package com.celements.navigation;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import org.xwiki.model.reference.DocumentReference;

public record NavigationItemContext(@Nullable DocumentReference documentReference,
    ContainerCssClasses containerCssClasses, Position position, ChildState childState,
    int itemNumber, ContextualState contextualState) {

  public NavigationItemContext {
    requireNonNull(containerCssClasses);
    requireNonNull(position);
    requireNonNull(childState);
    requireNonNull(contextualState);
  }

  public enum ContainerCssClasses {
    INCLUDE,
    OMIT
  }

  public enum Position {
    ONLY(true, true),
    FIRST(true, false),
    MIDDLE(false, false),
    LAST(false, true);

    private final boolean first;
    private final boolean last;

    Position(boolean first, boolean last) {
      this.first = first;
      this.last = last;
    }

    public boolean isFirst() {
      return first;
    }

    public boolean isLast() {
      return last;
    }
  }

  public enum ChildState {
    LEAF,
    HAS_CHILDREN
  }

  public enum ContextualState {
    INCLUDE,
    OMIT
  }
}
