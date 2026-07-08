package com.celements.javascript;

import static java.nio.charset.StandardCharsets.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.celements.javascript.FrontendResourceResolver.FrontendResource;

public class FrontendResourceResolverTest {

  private ResourcePatternResolver resourceLoader;
  private FrontendResourceResolver resolver;

  @Before
  public void prepareTest() {
    resourceLoader = createMock(ResourcePatternResolver.class);
    resolver = new FrontendResourceResolver(resourceLoader);
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
    replay(resourceLoader);

    assertEquals(Optional.of(new FrontendResource(
        "dist/vue-poc.BOsmCSyo.mjs",
        Arrays.asList("dist/assets/vue-poc-ahBOTvOT.css", "dist/assets/shared.Df9z3kSS.css"))),
        resolver.get(":frontend/progon/vue-poc/main.ts"));

    verify(resourceLoader);
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
    replay(resourceLoader);

    assertEquals(Optional.of(new FrontendResource("dist/eventview.Cq6C1_9z.mjs",
        Collections.emptyList())), resolver.get(":frontend/progon/eventview/main.ts"));

    verify(resourceLoader);
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
    replay(resourceLoader);

    assertEquals(Optional.empty(), resolver.get(":frontend/progon/vue-poc/main.ts"));

    verify(resourceLoader);
  }

  private Resource manifestResource(String content) {
    return new ByteArrayResource(content.getBytes(UTF_8));
  }
}
