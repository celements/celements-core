package com.celements.mandatory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.query.IQueryExecutionServiceRole;
import com.xpn.xwiki.XWikiException;

@Component("celements.mandatory.addIndexToRecycleBin")
public class AddDateIDXAsIndexToRecycleBin implements IMandatoryDocumentRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(AddDateIDXAsIndexToRecycleBin.class);

  @Requirement
  IQueryExecutionServiceRole queryExecService;

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return Collections.emptyList();
  }

  @Override
  public void checkDocuments() throws XWikiException {
    LOGGER.info("executing AddDateIDXAsIndexToRecycleBin");
    // FIXME make sure that these operations are ONLY executed ONCE on any DB.
    queryExecService.executeWriteSQLs(getIndexSQLs());
  }

  private List<String> getIndexSQLs() {
    List<String> ret = new ArrayList<String>();
    ret.add(getSQLIndexToRecycleBin());
    return ret;
  }

  String getSQLIndexToRecycleBin() {
    return "alter table xwikirecyclebin add index `dateIDX` (XDD_DATE, XDD_ID);";
  }

}
