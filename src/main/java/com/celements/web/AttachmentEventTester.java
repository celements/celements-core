package com.celements.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.AttachmentDiff;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * remove me after testing AttachmentDiff
 * 
 * @author fabian
 *
 */
@Component("AttachmentEventTester")
@Deprecated
public class AttachmentEventTester implements EventListener {

  /** Logging helper. */
  private static final Log LOGGER = LogFactory.getLog(AttachmentEventTester.class);

  @Requirement
  EntityReferenceSerializer<String> defaultEntityReferenceSerializer;

  public void onEvent(Event event, Object source, Object data) {
    XWikiDocument doc = (XWikiDocument) source;
    XWikiDocument originalDoc = doc.getOriginalDocument();
    XWikiContext context = (XWikiContext) data;

    String reference = this.defaultEntityReferenceSerializer.serialize(
        doc.getDocumentReference());

    LOGGER.debug("AttachmentEventTester: onEvent for [" + event.getClass() + "] on ["
        + reference + "] same test [" + (originalDoc == doc) + "].");
    for (XWikiAttachment origAttach : originalDoc.getAttachmentList()) {
      LOGGER.debug("origialDoc: " + origAttach.getFilename() + ", "
          + origAttach.getVersion());
    }
    for (XWikiAttachment newAttach : doc.getAttachmentList()) {
      LOGGER.debug("doc: " + newAttach.getFilename() + ", " + newAttach.getVersion());
    }

    try {
      for (AttachmentDiff diff : doc.getAttachmentDiff(originalDoc, doc, context)) {
        if (StringUtils.isEmpty(diff.getOrigVersion())) {
          LOGGER.debug("AttachmentEventTester: attachment diff for [" + diff.getFileName()
              + "] on [" + reference + "] added.");
        } else if (StringUtils.isEmpty(diff.getNewVersion())) {
          LOGGER.debug("AttachmentEventTester: attachment diff for [" + diff.getFileName()
              + "] on [" + reference + "] deleted.");
        } else {
          LOGGER.debug("AttachmentEventTester: attachment diff for [" + diff.getFileName()
              + "] on [" + reference + "] updated.");
        }
      }
    } catch (XWikiException ex) {
      LOGGER.warn("Failed to refine events: " + ex.getMessage());
    }
  }

  private static final List<Event> LISTENER_EVENTS = new ArrayList<Event>()
  {
      /**
     * 
     */
    private static final long serialVersionUID = 1L;

      {
          add(new DocumentCreatedEvent());
          add(new DocumentUpdatedEvent());
          add(new DocumentDeletedEvent());
      }
  };

  @Override
  public List<Event> getEvents() {
    return LISTENER_EVENTS;
  }

  @Override
  public String getName() {
    return "AttachmentEventTester";
  }

}
