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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.pagetype.xobject.event.XObjectPageTypeCreatedEvent;
import com.celements.pagetype.xobject.event.XObjectPageTypeDeletedEvent;
import com.celements.pagetype.xobject.event.XObjectPageTypeUpdatedEvent;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(XObjectPageTypeDocumentUpdatedListener.NAME)
public class XObjectPageTypeDocumentUpdatedListener extends AbstractXObjectPageTypeDocumentListener
    implements EventListener {

  public static final String NAME = "XObjectPageTypeDocumentUpdatedListener";

  private static Logger LOGGER = LoggerFactory.getLogger(
      XObjectPageTypeDocumentUpdatedListener.class);

  @Requirement
  RemoteObservationManagerContext remoteObservationManagerContext;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public String getName() {
    LOGGER.trace("XObjectPageTypeDocumentUpdatedListener getName");
    return NAME;
  }

  public List<Event> getEvents() {
    LOGGER.trace("XObjectPageTypeDocumentUpdatedListener getEvents");
    return Arrays.asList((Event) new DocumentUpdatedEvent());
  }

  public void onEvent(Event event, Object source, Object data) {
    LOGGER.trace("XObjectPageTypeDocumentUpdatedListener onEvent: start.");
    XWikiDocument document = (XWikiDocument) source;
    XWikiDocument origDoc = getOrginialDocument(source);
    if ((document != null) && (origDoc != null)
        && !remoteObservationManagerContext.isRemoteState()) {
      LOGGER.debug("XObjectPageTypeDocumentUpdatedListener onEvent: got event for ["
          + event.getClass() + "] on document [" + document.getDocumentReference() + "].");
      if (isPageTypePropertiesAdded(document, origDoc)) {
        LOGGER.debug("XObjectPageTypeDocumentUpdatedListener onEvent added to "
            + document.getDocumentReference() + "]");
        XObjectPageTypeCreatedEvent newXObjectPageTypeEvent = new XObjectPageTypeCreatedEvent(
            document.getDocumentReference());
        getObservationManager().notify(newXObjectPageTypeEvent, source, getContext());
      } else if (isPageTypePropertiesDeleted(document, origDoc)) {
        LOGGER.debug("XObjectPageTypeDocumentUpdatedListener onEvent" + " deleted from "
            + document.getDocumentReference() + "]");
        XObjectPageTypeDeletedEvent delXObjectPageTypeEvent = new XObjectPageTypeDeletedEvent(
            document.getDocumentReference());
        getObservationManager().notify(delXObjectPageTypeEvent, source, getContext());
      }
      if (isPageTypePropertiesUpdated(document, origDoc)) {
        LOGGER.debug("XObjectPageTypeDocumentUpdatedListener onEvent updated on "
            + document.getDocumentReference() + "]");
        XObjectPageTypeUpdatedEvent updXObjectPageTypeEvent = new XObjectPageTypeUpdatedEvent(
            document.getDocumentReference());
        getObservationManager().notify(updXObjectPageTypeEvent, source, getContext());
      }
    } else {
      LOGGER.trace("onEvent: got event for [" + event.getClass() + "] on source [" + source
          + "] and data [" + data + "], isLocalEvent ["
          + !remoteObservationManagerContext.isRemoteState() + "] -> skip.");
    }
  }

  boolean isPageTypePropertiesAdded(XWikiDocument document, XWikiDocument origDoc) {
    BaseObject pageTypePropObj = document.getXObject(getPageTypePropertiesClassRef(document));
    BaseObject pageTypePropOrigObj = origDoc.getXObject(getPageTypePropertiesClassRef(document));
    LOGGER.trace("isPageTypePropertiesAdded pageTypePropObj [" + pageTypePropObj
        + "], pageTypePropOrigObj [" + pageTypePropOrigObj + "]");
    return ((pageTypePropObj != null) && (pageTypePropOrigObj == null));
  }

  boolean isPageTypePropertiesDeleted(XWikiDocument document, XWikiDocument origDoc) {
    BaseObject pageTypePropObj = document.getXObject(getPageTypePropertiesClassRef(document));
    BaseObject pageTypePropOrigObj = origDoc.getXObject(getPageTypePropertiesClassRef(document));
    LOGGER.trace("isPageTypePropertiesDeleted pageTypePropObj [" + pageTypePropObj
        + "], pageTypePropOrigObj [" + pageTypePropOrigObj + "]");
    return ((pageTypePropObj == null) && (pageTypePropOrigObj != null));
  }

  boolean isPageTypePropertiesUpdated(XWikiDocument document, XWikiDocument origDoc) {
    BaseObject pageTypePropObj = document.getXObject(getPageTypePropertiesClassRef(document));
    BaseObject pageTypePropOrigObj = origDoc.getXObject(getPageTypePropertiesClassRef(document));
    LOGGER.trace("isPageTypePropertiesUpdated pageTypePropObj [" + pageTypePropObj
        + "], pageTypePropOrigObj [" + pageTypePropOrigObj + "]");
    boolean hasDiff = false;
    if ((pageTypePropObj != null) && (pageTypePropOrigObj != null)) {
      hasDiff |= hasStringDiff(pageTypePropObj, pageTypePropOrigObj,
          IPageTypeClassConfig.PAGETYPE_PROP_TYPE_NAME);
      hasDiff |= hasStringDiff(pageTypePropObj, pageTypePropOrigObj,
          IPageTypeClassConfig.PAGETYPE_PROP_CATEGORY);
      hasDiff |= hasStringDiff(pageTypePropObj, pageTypePropOrigObj,
          IPageTypeClassConfig.PAGETYPE_PROP_PAGE_EDIT);
      hasDiff |= hasStringDiff(pageTypePropObj, pageTypePropOrigObj,
          IPageTypeClassConfig.PAGETYPE_PROP_PAGE_VIEW);
      hasDiff |= hasBooleanDiff(pageTypePropObj, pageTypePropOrigObj,
          IPageTypeClassConfig.PAGETYPE_PROP_VISIBLE);
      hasDiff |= hasBooleanDiff(pageTypePropObj, pageTypePropOrigObj,
          IPageTypeClassConfig.PAGETYPE_PROP_SHOW_FRAME);
      hasDiff |= hasBooleanDiff(pageTypePropObj, pageTypePropOrigObj,
          IPageTypeClassConfig.PAGETYPE_PROP_LOAD_RICHTEXT);
      hasDiff |= hasIntegerDiff(pageTypePropObj, pageTypePropOrigObj,
          IPageTypeClassConfig.PAGETYPE_PROP_RTE_WIDTH);
      hasDiff |= hasIntegerDiff(pageTypePropObj, pageTypePropOrigObj,
          IPageTypeClassConfig.PAGETYPE_PROP_RTE_HEIGHT);
      hasDiff |= hasBooleanDiff(pageTypePropObj, pageTypePropOrigObj,
          IPageTypeClassConfig.PAGETYPE_PROP_HASPAGETITLE);
    }
    return hasDiff;
  }

  private boolean hasStringDiff(BaseObject pageTypePropObj, BaseObject pageTypePropOrigObj,
      String fieldName) {
    String newValue = pageTypePropObj.getStringValue(fieldName);
    String oldValue = pageTypePropOrigObj.getStringValue(fieldName);
    LOGGER.debug("isPageTypePropertiesUpdated diff check for field [" + fieldName + "] new value ["
        + newValue + "], old value [" + oldValue + "]");
    return (!StringUtils.equals(newValue, oldValue));
  }

  private boolean hasBooleanDiff(BaseObject pageTypePropObj, BaseObject pageTypePropOrigObj,
      String fieldName) {
    int newValue = pageTypePropObj.getIntValue(fieldName, -1);
    int oldValue = pageTypePropOrigObj.getIntValue(fieldName, -1);
    LOGGER.debug("isPageTypePropertiesUpdated diff check for field [" + fieldName + "] new value ["
        + newValue + "], old value [" + oldValue + "]");
    return (newValue != oldValue);
  }

  private boolean hasIntegerDiff(BaseObject pageTypePropObj, BaseObject pageTypePropOrigObj,
      String fieldName) {
    int newValue = pageTypePropObj.getIntValue(fieldName);
    int oldValue = pageTypePropOrigObj.getIntValue(fieldName);
    LOGGER.debug("isPageTypePropertiesUpdated diff check for field [" + fieldName + "] new value ["
        + newValue + "], old value [" + oldValue + "]");
    return (newValue != oldValue);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
