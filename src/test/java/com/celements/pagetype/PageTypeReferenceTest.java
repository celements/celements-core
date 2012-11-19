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
package com.celements.pagetype;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PageTypeReferenceTest {

  private static final String PROVIDER_HIND = "providerHind";
  private final static List<String> CATEGORIES = Arrays.asList("cellType",
      "presentationType");
  private final static String CONFIG_NAME = "testPageType";

  private PageTypeReference pageTypeRef;

  @Before
  public void setUp() throws Exception {
    pageTypeRef = new PageTypeReference(CONFIG_NAME, PROVIDER_HIND, CATEGORIES);
  }

  @Test
  public void testGetConfigName() {
    assertEquals(CONFIG_NAME, pageTypeRef.getConfigName());
  }

  @Test
  public void testGetCategories() {
    assertEquals(CATEGORIES, pageTypeRef.getCategories());
  }

  @Test
  public void testGetCategories_unmodifiable_input() {
    List<String> categories = new ArrayList<String>();
    categories.add("cat1");
    PageTypeReference pageTypeRef2 = new PageTypeReference(CONFIG_NAME, PROVIDER_HIND,
        categories);
    categories.add("cat2");
    assertEquals("category list must be unmodifiable after creation",
        Arrays.asList("cat1"), pageTypeRef2.getCategories());
  }

  @Test
  public void testGetCategories_unmodifiable_output() {
    List<String> categories = new ArrayList<String>();
    categories.add("cat1");
    PageTypeReference pageTypeRef2 = new PageTypeReference(CONFIG_NAME, PROVIDER_HIND,
        categories);
    categories = pageTypeRef2.getCategories();
    categories.add("cat2");
    assertEquals("category list must not be modifiable throught getCategories output",
        Arrays.asList("cat1"), pageTypeRef2.getCategories());
  }

  @Test
  public void testHashCode_equal() {
    PageTypeReference pageTypeRef2 = new PageTypeReference(CONFIG_NAME, PROVIDER_HIND,
        Collections.<String>emptyList());
    assertTrue(pageTypeRef.hashCode() == pageTypeRef2.hashCode());
  }

  @Test
  public void testHashCode_notEqual() {
    PageTypeReference pageTypeRef2 = new PageTypeReference("someDifferentPageType",
        PROVIDER_HIND, CATEGORIES);
    assertFalse(pageTypeRef.hashCode() == pageTypeRef2.hashCode());
  }

  @Test
  public void testEqualsObject_differentClass() {
    assertFalse(pageTypeRef.equals(CONFIG_NAME));
  }

  @Test
  public void testEqualsObject_differentConfigName() {
    PageTypeReference pageTypeRef2 = new PageTypeReference("someDifferentPageType",
        PROVIDER_HIND, Collections.<String>emptyList());
    assertFalse(pageTypeRef.equals(pageTypeRef2));
  }

  @Test
  public void testEqualsObject_equal() {
    PageTypeReference pageTypeRef2 = new PageTypeReference(CONFIG_NAME,
        PROVIDER_HIND, Collections.<String>emptyList());
    assertTrue(pageTypeRef.equals(pageTypeRef2));
  }

}
