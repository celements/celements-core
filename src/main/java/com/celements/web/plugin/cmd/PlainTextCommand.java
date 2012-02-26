package com.celements.web.plugin.cmd;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.web.utils.Html2Text;

public class PlainTextCommand {

  private static Log LOGGER = LogFactory.getFactory().getInstance(PlainTextCommand.class);

  public String convertToPlainText(String htmlContent) {
    try {
      Reader in = new StringReader(htmlContent);
      Html2Text parser = new Html2Text();
      parser.parse(in);
      in.close();
      return parser.getText();
    } catch (IOException ioExp) {
      LOGGER.error("Fail to convertToPlainText: ", ioExp);
    }
    return "";
  }

}
