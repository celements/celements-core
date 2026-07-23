package com.celements.javascript;

import static java.nio.charset.StandardCharsets.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.celements.common.test.AbstractComponentTest;
import com.celements.javascript.FrontendResourceResolver.FrontendResource;

public class FrontendResourceResolverTest extends AbstractComponentTest {

  private ResourcePatternResolver resourceLoader;
  private Supplier<FrontendResourceResolver> resolver;

  @Before
  public void prepareTest() throws Exception {
    resourceLoader = registerComponentMock(ResourcePatternResolver.class);
    resolver = () -> getBeanFactory().getBean(FrontendResourceResolver.class);
  }

  @Test
  public void test_get() throws Exception {
    expect(resourceLoader.getResources("/resources/dist/.vite/manifest*.json"))
        .andReturn(new Resource[] { manifestResource("""
            {
              "src/main/frontend/progon/vue-poc/main.ts": {
                "file": "vue-poc.BOsmCSyo.mjs",
                "css": [
                  "assets/vue-poc-ahBOTvOT.css",
                  "assets/shared.Df9z3kSS.css"
                ]
              }
            }
            """) });
    replayDefault();

    assertEquals(Optional.of(new FrontendResource(
        "dist/vue-poc.BOsmCSyo.mjs",
        Arrays.asList("dist/assets/vue-poc-ahBOTvOT.css", "dist/assets/shared.Df9z3kSS.css"))),
        resolver.get().get(":frontend/progon/vue-poc/main.ts"));

    verifyDefault();
  }

  @Test
  public void test_get_missingCss() throws Exception {
    expect(resourceLoader.getResources("/resources/dist/.vite/manifest*.json"))
        .andReturn(new Resource[] { manifestResource("""
            {
              "src/main/frontend/progon/eventview/main.ts": {
                "file": "eventview.Cq6C1_9z.mjs"
              }
            }
            """) });
    replayDefault();

    assertEquals(Optional.of(new FrontendResource("dist/eventview.Cq6C1_9z.mjs",
        Collections.emptyList())), resolver.get().get(":frontend/progon/eventview/main.ts"));

    verifyDefault();
  }

  @Test
  public void test_get_importedCss() throws Exception {
    expect(resourceLoader.getResources("/resources/dist/.vite/manifest*.json"))
        .andReturn(new Resource[] { manifestResource("""
            {
              "_tailwind.BUC6EzWv.mjs": {
                "file": "tailwind.BUC6EzWv.mjs",
                "imports": [
                  "_reactivity.esm-bundler.DLdBTi3W.mjs"
                ],
                "css": [
                  "assets/tailwind-5_VRq81v.css"
                ]
              },
              "_reactivity.esm-bundler.DLdBTi3W.mjs": {
                "file": "reactivity.esm-bundler.DLdBTi3W.mjs"
              },
              "src/main/frontend/progon/vue-poc/main.ts": {
                "file": "vue-poc.CPN5BtMj.mjs",
                "imports": [
                  "_tailwind.BUC6EzWv.mjs",
                  "_reactivity.esm-bundler.DLdBTi3W.mjs"
                ]
              }
            }
            """) });
    replayDefault();

    assertEquals(Optional.of(new FrontendResource("dist/vue-poc.CPN5BtMj.mjs",
        List.of("dist/assets/tailwind-5_VRq81v.css"))),
        resolver.get().get(":frontend/progon/vue-poc/main.ts"));

    verifyDefault();
  }

  @Test
  public void test_get_notFound() throws Exception {
    expect(resourceLoader.getResources("/resources/dist/.vite/manifest*.json"))
        .andReturn(new Resource[] { manifestResource("""
            {
              "src/main/frontend/progon/eventview/main.ts": {
                "file": "eventview.Cq6C1_9z.mjs"
              }
            }
            """) });
    replayDefault();

    assertEquals(Optional.empty(), resolver.get().get(":frontend/progon/vue-poc/main.ts"));

    verifyDefault();
  }

  private Resource manifestResource(String content) {
    return new ByteArrayResource(content.getBytes(UTF_8));
  }
}
