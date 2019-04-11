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
package com.celements.web.contextmenu;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.velocity.VelocityContext;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.cache.CacheManager;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.google.common.base.Function;
import com.google.common.io.Resources;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDeletedDocument;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.plugin.XWikiPluginManager;
import com.xpn.xwiki.render.DefaultXWikiRenderingEngine;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.web.Utils;

public class XarLoaderTest extends AbstractComponentTest {

  private Map<String, XWikiDocument> docMap = new HashMap<>();

  @Before
  public void prepareTest() {
  }

  @Test
  public void testContextMenuItem_withPrefix() throws Exception {
    getContext().setDatabase("mydb");
    loadXars();
    getContext().setDoc(docMap.get("mydb:Content.WebHome"));

    VelocityContext vContext = (VelocityContext) Utils.getComponent(
        Execution.class).getContext().getProperty("velocityContext");
    getContext().put("vcontext", vContext);
    expect(getWikiMock().getRenderingEngine()).andAnswer(new IAnswer<XWikiRenderingEngine>() {

      @Override
      public XWikiRenderingEngine answer() throws Throwable {
        return new DefaultXWikiRenderingEngine(getWikiMock(), getContext());
      }
    }).anyTimes();
    expect(getWikiMock().getConfig()).andReturn(new XWikiConfig()).anyTimes();
    expect(getWikiMock().Param(anyObject(String.class))).andReturn("").anyTimes();
    expect(getWikiMock().Param(anyObject(String.class), anyObject(String.class))).andReturn(
        "").anyTimes();
    expect(getWikiMock().ParamAsLong(anyObject(String.class), anyLong())).andReturn(0L).anyTimes();
    expect(getWikiMock().getCacheFactory()).andReturn(Utils.getComponent(
        CacheManager.class).getCacheFactory()).anyTimes();
    expect(getWikiMock().getPlugin(anyObject(String.class), same(getContext()))).andReturn(
        null).anyTimes();
    expect(getWikiMock().getPluginManager()).andReturn(new TestXWikiPluginManager()).anyTimes();
    expect(getWikiMock().getSpacePreference(anyObject(String.class), same(getContext()))).andReturn(
        "").anyTimes();
    expect(getWikiMock().getXWikiPreference(anyObject(String.class), same(getContext()))).andReturn(
        "").anyTimes();

    expect(getWikiMock().getSkin(same(getContext()))).andReturn("").anyTimes();
    expect(getWikiMock().getSkinFile(eq("macros.vm"), eq(""), same(getContext()))).andReturn(
        null).anyTimes();
    expect(getWikiMock().getURL(anyObject(String.class), anyObject(String.class), same(
        getContext()))).andReturn("").anyTimes();
    expect(getWikiMock().getURL(anyObject(String.class), anyObject(String.class), anyObject(
        String.class), same(getContext()))).andReturn("").anyTimes();
    expect(getWikiMock().getDeletedDocuments(anyObject(String.class), anyObject(String.class), same(
        getContext()))).andReturn(new XWikiDeletedDocument[0]).anyTimes();

  }

  private void loadXars() throws IOException {
    XarLoader loader = new XarLoader().addFunction(new Function<XWikiDocument, Void>() {

      @Override
      public Void apply(XWikiDocument doc) {
        try {
          docMap.put(doc.getPrefixedFullName(), doc);
          expect(getWikiMock().exists(eq(doc.getDocumentReference()), same(
              getContext()))).andReturn(true).anyTimes();
          expect(getWikiMock().exists(eq(doc.getFullName()), same(getContext()))).andReturn(
              true).anyTimes();
          expect(getWikiMock().exists(eq(doc.getPrefixedFullName()), same(getContext()))).andReturn(
              true).anyTimes();
          expect(getWikiMock().getDocument(eq(doc.getDocumentReference()), same(
              getContext()))).andReturn(doc).anyTimes();
        } catch (XWikiException exc) {
          throw new RuntimeException(exc);
        }
        return null;
      }
    });
    loader.load("celements2web", "celements2web-legacy-3.4.xar");
    loader.load(getContext().getDatabase(), "mydb.xar");
    expect(getWikiMock().exists(anyObject(DocumentReference.class), same(getContext()))).andReturn(
        false).anyTimes();
    expect(getWikiMock().exists(anyObject(String.class), same(getContext()))).andReturn(
        false).anyTimes();
  }

  private Path getResourcePath(String file) {
    try {
      return Paths.get(getClass().getClassLoader().getResource(file).toURI());
    } catch (URISyntaxException exc) {
      throw new IllegalArgumentException(exc);
    }
  }

  private String loadFile(Path path) throws IOException {
    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
  }

  class XarLoader {

    private final List<Function<XWikiDocument, Void>> functions;

    public XarLoader() {
      functions = new ArrayList<>();
    }

    public XarLoader addFunction(Function<XWikiDocument, Void> func) {
      functions.add(func);
      return this;
    }

    public void load(String database, String resourceFile) throws IOException {
      try {
        Path path = Paths.get(Resources.getResource(resourceFile).toURI());
        try (InputStream is = Files.newInputStream(path)) {
          load(database, is);
        }
      } catch (URISyntaxException exc) {
        throw new IllegalArgumentException(exc);
      }
    }

    public void load(String database, InputStream is) throws IOException {
      String before = getContext().getDatabase();
      getContext().setDatabase(database);
      ZipInputStream zis = new ZipInputStream(is);
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (!entry.isDirectory() && !entry.getName().contains("META-INF")) {
          try {
            XWikiDocument doc = new XWikiDocument();
            doc.fromXML(new CloseShieldInputStream(zis));
            System.out.println("loaded: " + doc.getPrefixedFullName());
            for (Function<XWikiDocument, Void> func : functions) {
              func.apply(doc);
            }
          } catch (XWikiException ex) {
            System.err.println("skipped: " + entry.getName());
          }
        }
      }
      getContext().setDatabase(before);
    }

  }

  private class TestXWikiPluginManager extends XWikiPluginManager {

    @Override
    public Vector<String> getPlugins() {
      return new Vector<>();
    }

    @Override
    public Vector<XWikiPluginInterface> getPlugins(String functionName) {
      return new Vector<>();
    }
  }

}
