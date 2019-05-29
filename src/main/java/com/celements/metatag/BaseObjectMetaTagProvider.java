package com.celements.metatag;

import static com.google.common.base.Strings.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(BaseObjectMetaTagProvider.COMPONENT_NAME)
public class BaseObjectMetaTagProvider implements MetaTagProviderRole, Initializable {

  public static final String COMPONENT_NAME = "BaseObjectMetaTagProvider";

  @Requirement(XObjectBeanConverter.NAME)
  private BeanClassDefConverter<BaseObject, MetaTag> metaTagConverter;

  @Requirement(MetaTagClass.CLASS_DEF_HINT)
  private ClassDefinition metaTagClass;

  @Requirement
  private ModelContext context;

  @Override
  public void initialize() throws InitializationException {
    metaTagConverter.initialize(metaTagClass);
    metaTagConverter.initialize(new ComponentInstanceSupplier<>(MetaTag.class));
  }

  @Override
  public List<MetaTag> getHeaderMetaTags() {
    SortedMap<String, List<MetaTag>> tags = new TreeMap<>();
    addMetaTagsFromList(getMetaTagsForDoc(context.getOrCreateXWikiPreferenceDoc()), tags);
    addMetaTagsFromList(getMetaTagsForDoc(context.getOrCreateSpacePreferenceDoc()), tags);
    Optional<XWikiDocument> doc = context.getCurrentDoc();
    if (doc.isPresent()) {
      addMetaTagsFromList(getMetaTagsForDoc(doc.get()), tags);
    }
    return ImmutableList.copyOf(tags.values().parallelStream().map(applyOverride()).collect(
        Collectors.<MetaTag>toList()));
  }

  Function<List<MetaTag>, MetaTag> applyOverride() {
    return new Function<List<MetaTag>, MetaTag>() {

      @Override
      public MetaTag apply(List<MetaTag> tags) {
        MetaTag tag = null;
        tags.stream().sequential().reduce(tag, applyOverrideLogic());
        return tag;
      }

      BinaryOperator<MetaTag> applyOverrideLogic() {
        return new BinaryOperator<MetaTag>() {

          @Override
          public MetaTag apply(MetaTag reduction, MetaTag tag) {
            if ((reduction != null) && !reduction.getOverridable()) {
              reduction.setValue(reduction.getValue() + ", " + tag.getValue());
              return reduction;
            }
            return tag;
          }
        };
      }
    };
  }

  void addMetaTagsFromList(List<MetaTag> newTags, SortedMap<String, List<MetaTag>> finalTags) {
    for (MetaTag tag : newTags) {
      String lang = tag.getLang();
      if (isNullOrEmpty(lang) || lang.equals(context.getXWikiContext().getLanguage()) || lang
          .equals(context.getDefaultLanguage())) {
        if (!finalTags.containsKey(tag.getKey())) {
          finalTags.put(tag.getKey(), new ArrayList<MetaTag>());
        }
        finalTags.get(tag.getKey()).add(tag);
      }
    }
  }

  @Override
  public List<MetaTag> getBodyMetaTags() {
    return Collections.emptyList();
  }

  List<MetaTag> getMetaTagsForDoc(XWikiDocument doc) {
    return XWikiObjectFetcher.on(doc).filter(metaTagClass).list().stream()
        .parallel().map(
            new Function<BaseObject, MetaTag>() {

              @Override
              public MetaTag apply(BaseObject obj) {
                return metaTagConverter.apply(obj);
              }

            }).collect(Collectors.<MetaTag>toList());
  }

}
