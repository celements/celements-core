package com.celements.mandatory;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.query.IQueryExecutionServiceRole;
import com.xpn.xwiki.XWikiException;

@Component("celements.mandatory.addIndexToRecycleBin")
public class XWikiRecycleBinIndexes implements IMandatoryDocumentRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(XWikiRecycleBinIndexes.class);

  @Requirement
  IQueryExecutionServiceRole queryExecService;

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return Collections.emptyList();
  }

  @Override
  public void checkDocuments() throws XWikiException {
    LOGGER.info("executing XWikiRecycleBinIndexes");
    if (queryExecService.existsConstraint("prefix_db", "xwikirecyclebin", "dateIDX")) {
      queryExecService.executeWriteSQL(getSQLIndexToRecycleBin());
    }
  }

  String getSQLIndexToRecycleBin() {
    return "alter table xwikirecyclebin add index `dateIDX` (XDD_DATE, XDD_ID);";
  }

}
