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
    System.out.println(
        "<<<<<<<<<<<<<<<< AddDateIDXAsIndexToRecycleBin dependsOnMandatoryDocuments");
    return Collections.emptyList();
  }

  @Override
  public void checkDocuments() throws XWikiException {
    System.out.println("<<<<<<<<<<<<<<<< AddDateIDXAsIndexToRecycleBin checkDocuments");
    LOGGER.info("executing AddDateIDXAsIndexToRecycleBin");
    // FIXME make sure that these operations are ONLY executed ONCE on any DB.
    queryExecService.executeWriteSQLs(getIndexSQLs());
  }

  private List<String> getIndexSQLs() {
    System.out.println("<<<<<<<<<<<<<<<< AddDateIDXAsIndexToRecycleBin getIndexSQLs");
    List<String> ret = new ArrayList<String>();
    ret.add(getSQLIndexToRecycleBin());
    return ret;
  }

  String getSQLIndexToRecycleBin() {
    System.out.println("<<<<<<<<<<<<<<<< AddDateIDXAsIndexToRecycleBin getSQLIndexToRecycleBin");
    return "alter table xwikirecyclebin add index `dateIDX` (XDD_DATE, XDD_ID);";
  }

}
