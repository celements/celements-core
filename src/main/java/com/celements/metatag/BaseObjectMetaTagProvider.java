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
package com.celements.metatag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

import com.celements.component.ComponentInstanceSupplier;
import com.celements.convert.bean.BeanClassDefConverter;
import com.celements.convert.bean.XObjectBeanConverter;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.web.classes.MetaTagClass;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(BaseObjectMetaTagProvider.COMPONENT_NAME)
public class BaseObjectMetaTagProvider implements MetaTagProviderRole, Initializable {

  public static final String COMPONENT_NAME = "BaseObjectMetaTagProvider";

  @Requirement(XObjectBeanConverter.NAME)
  private BeanClassDefConverter<BaseObject, MetaTagRole> metaTagConverter;

  @Requirement(MetaTagClass.CLASS_DEF_HINT)
  private ClassDefinition metaTagClass;

  @Requirement
  private ModelContext context;

  @Override
  public void initialize() throws InitializationException {
    metaTagConverter.initialize(metaTagClass);
    metaTagConverter.initialize(new ComponentInstanceSupplier<>(MetaTagRole.class));
  }

  @Override
  public List<MetaTag> getHeaderMetaTags() {
    SortedMap<String, List<MetaTag>> tags = new TreeMap<>();
    addMetaTagsFromList(getMetaTagsForDoc(context.getOrCreateXWikiPreferenceDoc()), tags);
    addMetaTagsFromList(getMetaTagsForDoc(context.getOrCreateSpacePreferenceDoc(context
        .getCurrentSpaceRefOrDefault())), tags);
    Optional<XWikiDocument> doc = context.getCurrentDoc().toJavaUtil();
    if (doc.isPresent()) {
      addMetaTagsFromList(getMetaTagsForDoc(doc.get()), tags);
    }
    return ImmutableList.copyOf(tags.values().parallelStream()
        .flatMap(applyOverride())
        .filter(Objects::nonNull)
        .collect(Collectors.<MetaTag>toList()));
  }

  Function<List<MetaTag>, Stream<MetaTag>> applyOverride() {
    return new Function<List<MetaTag>, Stream<MetaTag>>() {

      @Override
      public Stream<MetaTag> apply(List<MetaTag> tags) {
        return tags.stream().sequential().collect(applyOverrideLogic());
      }

      Collector<MetaTag, List<MetaTag>, Stream<MetaTag>> applyOverrideLogic() {
        return new Collector<MetaTag, List<MetaTag>, Stream<MetaTag>>() {

          @Override
          public Supplier<List<MetaTag>> supplier() {
            return new Supplier<List<MetaTag>>() {

              @Override
              public List<MetaTag> get() {
                return new ArrayList<>();
              }
            };
          }

          @Override
          public BiConsumer<List<MetaTag>, MetaTag> accumulator() {

            return new BiConsumer<List<MetaTag>, MetaTag>() {

              @Override
              public void accept(List<MetaTag> accu, MetaTag tag) {
                MetaTag reductor = (accu.size() > 0) ? accu.get(accu.size() - 1) : null;
                if (reductor == null) {
                  accu.add(tag);
                } else if (reductor.getOverridable()) {
                  Collections.replaceAll(accu, reductor, tag);
                } else {
                  reductor.setValue(reductor.getValueOpt().orElse("") + "," + tag.getValueOpt()
                      .orElse(
                          ""));
                }
              }
            };
          }

          @Override
          public BinaryOperator<List<MetaTag>> combiner() {
            return new BinaryOperator<List<MetaTag>>() {

              @Override
              public List<MetaTag> apply(List<MetaTag> list1, List<MetaTag> list2) {
                return Stream.of(list1, list2).flatMap(Collection::stream).collect(Collectors
                    .toList());
              }
            };
          }

          @Override
          public Function<List<MetaTag>, Stream<MetaTag>> finisher() {
            return new Function<List<MetaTag>, Stream<MetaTag>>() {

              @Override
              public Stream<MetaTag> apply(List<MetaTag> tags) {
                return tags.stream();
              }

            };
          }

          @Override
          public Set<Characteristics> characteristics() {
            return Collections.emptySet();
          }
        };
      }
    };
  }

  void addMetaTagsFromList(List<MetaTag> newTags, SortedMap<String, List<MetaTag>> finalTags) {
    for (MetaTag tag : newTags) {
      Optional<String> lang = tag.getLangOpt();
      if (lang.isEmpty() || lang.get().equals(context.getLanguage().orElse(null)) || lang.get()
          .equals(context.getDefaultLanguage())) {
        String key = tag.getKeyOpt().orElse(null);
        if (!finalTags.containsKey(key)) {
          finalTags.put(key, new ArrayList<MetaTag>());
        }
        finalTags.get(key).add(tag);
      }
    }
  }

  List<MetaTag> getMetaTagsForDoc(XWikiDocument doc) {
    return XWikiObjectFetcher.on(doc).filter(metaTagClass).list().stream()
        .parallel().map(
            new Function<BaseObject, MetaTag>() {

              @Override
              public MetaTag apply(BaseObject obj) {
                return (MetaTag) metaTagConverter.apply(obj);
              }

            }).collect(Collectors.<MetaTag>toList());
  }

  @Override
  public List<MetaTag> getBodyMetaTags() {
    return Collections.emptyList();
  }

}
