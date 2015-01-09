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
package com.celements.pagetype.xobject.listener;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.xobject.listener.XObjectPageTypeDocumentCreatedListener;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeDocumentCreatedListenerTest
    extends AbstractBridgedComponentTestCase {
  
  private static final String _COMPONENT_NAME = "XObjectPageTypeDocumentCreatedListener";
  private XObjectPageTypeDocumentCreatedListener eventListener;

  @Before
  public void setUp_XObjectPageTypeDocumentCreatedListenerTest() throws Exception {
    eventListener = getXObjectPageTypeDocumentCreatedListener();
  }

  @Test
  public void testComponentSingleton() {
    assertSame(eventListener, getXObjectPageTypeDocumentCreatedListener());
  }

  @Test
  public void testGetName() {
    assertEquals(_COMPONENT_NAME, eventListener.getName());
  }

  @Test
  public void testGetEvents() {
    List<String> expectedEventClassList = Arrays.asList(new DocumentCreatedEvent(
        ).getClass().getName());
    replayDefault();
    List<Event> actualEventList = eventListener.getEvents();
    assertEquals(expectedEventClassList.size(), actualEventList.size());
    for (Event actualEvent : actualEventList) {
      assertTrue("Unexpected Event [" + actualEvent.getClass().getName() + "] found.",
          expectedEventClassList.contains(actualEvent.getClass().getName()));
    }
    verifyDefault();
  }

  private XObjectPageTypeDocumentCreatedListener getXObjectPageTypeDocumentCreatedListener() {
    return (XObjectPageTypeDocumentCreatedListener) Utils.getComponent(EventListener.class,
        _COMPONENT_NAME);
  }

}
