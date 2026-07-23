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
package com.celements.cells;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.celements.cells.attribute.CellAttribute;

@NotThreadSafe
public abstract class AbstractWriter implements ICellWriter {

  /**
   * Entry: tagName (String), hasContent (Boolean)
   */
  protected final Deque<Entry<String, Boolean>> openLevels = new LinkedList<>();

  protected Optional<Entry<String, Boolean>> getCurrentLevel() {
    return Optional.ofNullable(openLevels.peek());
  }

  protected Optional<Boolean> hasLevelContentOptional() {
    return getCurrentLevel().map(Entry::getValue);
  }

  @Override
  public void openLevel(@Nullable String tagName) {
    openLevel(tagName, Collections.<CellAttribute>emptyList());
  }

  @Override
  public final Stream<String> getOpenLevels() {
    return openLevels.stream().map(Entry::getKey);
  }

  @Override
  public void clear() {
    openLevels.clear();
  }

}
