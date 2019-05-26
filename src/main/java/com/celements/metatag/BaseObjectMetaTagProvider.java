package com.celements.metatag;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
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
    SortedMap<String, MetaTag> tags = new TreeMap<>();
    addMetaTagsFromList(getMetaTagsForDoc(context.getOrCreateXWikiPreferenceDoc()), tags);
    addMetaTagsFromList(getMetaTagsForDoc(context.getOrCreateSpacePreferenceDoc()), tags);
    Optional<XWikiDocument> doc = context.getCurrentDoc();
    if (doc.isPresent()) {
      addMetaTagsFromList(getMetaTagsForDoc(doc.get()), tags);
    }
    return ImmutableList.copyOf(tags.values());
  }

  void addMetaTagsFromList(List<MetaTag> newTags, SortedMap<String, MetaTag> finalTags) {
    String lang = context.getXWikiContext().getLanguage();
    String defaultLang = context.getDefaultLanguage();
    for (MetaTag tag : newTags) {
      // spaeter gefundene ueberschreiben vorher gefundene - dabei ist das overridable und das
      // sprachfeld zu beachten
      // nicht ueberschreibbare werden comma separated gemerged
      // TODO
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
