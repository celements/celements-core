package com.celements.web.service;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;

@Component("webUtils")
public class WebUtilsScriptService implements ScriptService {

  @Requirement
  IWebUtilsService webUtilsService;

  public String getDefaultLanguage() {
    return webUtilsService.getDefaultLanguage();
  }

  public String getDefaultLanguage(String spaceName) {
    return webUtilsService.getDefaultLanguage(spaceName);
  }

  public List<Attachment> getAttachmentListSorted(Document doc, String comparator
      ) throws ClassNotFoundException{
    return webUtilsService.getAttachmentListSorted(doc, comparator);
  }

  public List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException{
    return webUtilsService.getAttachmentListSorted(doc, comparator, imagesOnly, start,
        nb);
  }

  public String getAttachmentListSortedAsJSON(Document doc, String comparator,
      boolean imagesOnly) throws ClassNotFoundException{
    return getAttachmentListSortedAsJSON(doc, comparator, imagesOnly, 0, 0);
  }

  public String getAttachmentListSortedAsJSON(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException{
    return webUtilsService.getAttachmentListSortedAsJSON(doc, comparator, imagesOnly,
        start, nb);
  }


}
