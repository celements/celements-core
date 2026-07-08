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
package com.celements.web.css;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.celements.javascript.FrontendResourceResolver;
import com.google.common.base.Splitter;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class CSSEngine implements ICSSEngine {

  static final Logger LOGGER = LoggerFactory.getLogger(CSSEngine.class);

  private final FrontendResourceResolver resolver;

  @Inject
  public CSSEngine(FrontendResourceResolver resolver) {
    this.resolver = resolver;
  }

  /**
   * @param css
   * @param field
   * @param baseCSSList
   * @param context
   * @return the returned list is XWikiContext dependent and therefore may not be cached
   *         or similar. The list is as a consequence too not thread safe. TODO: Fix mix
   *         of API and backend. Extract business objects (controller) from CSS classes
   *         and use them here.
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<CSS> includeCSS(String css, String field, List<BaseObject> baseCSSList,
      XWikiContext context) {
    LOGGER.debug("includeCSS: adding '{}' to {}. List contains already {} items.", css, field,
        ((baseCSSList != null) ? baseCSSList.size() : "0"));
    VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
    List<CSS> cssList;
    if (vcontext == null) {
      return Collections.emptyList();
    } else if (vcontext.containsKey(field)) {
      cssList = (List<CSS>) vcontext.get(field);
    } else {
      cssList = Stream.ofNullable(baseCSSList).flatMap(List::stream)
          .filter(Objects::nonNull)
          .<CSS>map(CSSBaseObject::new)
          .collect(toList());
    }
    Splitter.on(" ").trimResults().omitEmptyStrings()
        .splitToStream(requireNonNullElse(css, ""))
        .flatMap(this::collectCssPaths)
        .forEach(cssList::add);
    vcontext.put(field, cssList);
    return cssList;
  }

  private Stream<CSS> collectCssPaths(String path) {
    if (resolver.isFrontendSource(path)) {
      return resolver.get(path).stream()
          .flatMap(resource -> resource.cssPaths().stream())
          .map(CSSFrontendResource::new);
    } else {
      return Stream.of(new CSSString(path));
    }
  }
}
