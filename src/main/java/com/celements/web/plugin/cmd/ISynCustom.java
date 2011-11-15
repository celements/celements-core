package com.celements.web.plugin.cmd;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;

/**
 * @deprecated use syncustom script service direcly instead
 */
@Deprecated
public interface ISynCustom {

  /**
   * @deprecated use syncustom script service direcly instead
   */
  @Deprecated
  public void processRegistrationsWithoutCallback(List<String> recipients);

  /**
   * @deprecated use syncustom script service direcly instead
   */
  @Deprecated
  public void sendCallbackNotificationMail(Map<String, String[]> data,
      List<String> recipients);

  /**
   * @deprecated use syncustom script service direcly instead
   */
  @Deprecated
  public void paymentCallback() throws XWikiException;

  /**
   * @deprecated use syncustom script service direcly instead
   */
  @Deprecated
  public float getBMI();

  /**
   * @deprecated use syncustom script service direcly instead
   */
  @Deprecated
  public long getMillisecsForEarlyBirdDate(Date date);

  /**
   * @deprecated use syncustom script service direcly instead
   */
  @Deprecated
  public String getFormatedEarlyBirdDate(Date date, String format);

  /**
   * @deprecated use syncustom script service direcly instead
   */
  @Deprecated
  public int countObjsWithField(String fullName, String className, String fieldName,
      String value, String valueEnd);

  /**
   * @deprecated use syncustom script service direcly instead
   */
  @Deprecated
  public Map<String, Integer> getRegistrationStatistics(Document mappingDoc,
      String congressName);

  /**
   * @deprecated use syncustom script service direcly instead
   */
  @Deprecated
  public Map<String, String> getExportMapping(String mappingStr, String congress);

  /**
   * @deprecated use syncustom script service direcly instead
   */
  @Deprecated
  public boolean congressRegistrationPlausibility(Document document);

  /**
   * @deprecated use syncustom script service direcly instead
   */
  @Deprecated
  public boolean congressRegistrationPlausibility();

}
