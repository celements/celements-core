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
package com.celements.javascript;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;

public class JsLoadModeTest extends AbstractComponentTest {

  @Test
  public void valueOfStore_empty() {
    replayDefault();
    assertEquals(JsLoadMode.SYNC, JsLoadMode.convertStoreValue(""));
    verifyDefault();
  }

  @Test
  public void valueOfStore_SYNC() {
    replayDefault();
    assertEquals(JsLoadMode.SYNC, JsLoadMode.convertStoreValue("SYNC"));
    verifyDefault();
  }

  @Test
  public void valueOfStore_DEFER() {
    replayDefault();
    assertEquals(JsLoadMode.DEFER, JsLoadMode.convertStoreValue("DEFER"));
    verifyDefault();
  }

  @Test
  public void valueOfStore_ASYNC() {
    replayDefault();
    assertEquals(JsLoadMode.ASYNC, JsLoadMode.convertStoreValue("ASYNC"));
    verifyDefault();
  }

  @Test
  public void valueOfStore_SYNC_lower() {
    replayDefault();
    assertEquals(JsLoadMode.SYNC, JsLoadMode.convertStoreValue("sync"));
    verifyDefault();
  }

  @Test
  public void valueOfStore_DEFER_lower() {
    replayDefault();
    assertEquals(JsLoadMode.DEFER, JsLoadMode.convertStoreValue("defer"));
    verifyDefault();
  }

  @Test
  public void valueOfStore_ASYNC_lower() {
    replayDefault();
    assertEquals(JsLoadMode.ASYNC, JsLoadMode.convertStoreValue("async"));
    verifyDefault();
  }

  @Test
  public void valueOfStore_null() {
    replayDefault();
    assertEquals(JsLoadMode.SYNC, JsLoadMode.convertStoreValue(null));
    verifyDefault();
  }

}
