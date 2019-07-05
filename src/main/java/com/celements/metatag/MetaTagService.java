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
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

@Component
public class MetaTagService implements MetaTagServiceRole {

  @Requirement
  private Execution execution;

  @Requirement
  private List<MetaTagProviderRole> headerTagImpls;

  @Override
  public void addMetaTagToCollector(@NotNull MetaTag tag) {
    Object contextObj = getExecutionContext().getProperty(MetaTagServiceRole.META_CONTEXT_KEY);
    List<MetaTag> metaList = getMetaTags(contextObj);
    if (metaList == null) {
      metaList = new ArrayList<>();
      getExecutionContext().setProperty(MetaTagServiceRole.META_CONTEXT_KEY, metaList);
    }
    metaList.add(tag);
  }

  @Override
  public @NotNull String displayCollectedMetaTags() {
    StringBuilder sb = new StringBuilder();
    Object contextObj = getExecutionContext().getProperty(MetaTagServiceRole.META_CONTEXT_KEY);
    getExecutionContext().removeProperty(MetaTagServiceRole.META_CONTEXT_KEY);
    List<MetaTag> metaTags = getMetaTags(contextObj);
    if (metaTags != null) {
      for (MetaTag metaTag : metaTags) {
        sb.append(metaTag.display()).append("\n");
      }
    }
    return sb.toString();
  }

  @Override
  public void collectHeaderTags() {
    for (MetaTagProviderRole headerTagImpl : headerTagImpls) {
      for (MetaTag tag : headerTagImpl.getHeaderMetaTags()) {
        addMetaTagToCollector(tag);
      }
    }
  }

  @Override
  public void collectBodyTags() {
    for (MetaTagProviderRole headerTagImpl : headerTagImpls) {
      for (MetaTag tag : headerTagImpl.getBodyMetaTags()) {
        addMetaTagToCollector(tag);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Nullable
  List<MetaTag> getMetaTags(@Nullable Object contextObj) {
    if (contextObj instanceof List) {
      return (List<MetaTag>) contextObj;
    }
    return null;
  }

  private @NotNull ExecutionContext getExecutionContext() {
    return execution.getContext();
  }

}
