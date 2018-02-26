package com.celements.mandatory;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.model.context.ModelContext;
import com.celements.query.IQueryExecutionServiceRole;
import com.xpn.xwiki.XWikiException;

@Component(XWikiRecycleBinIndexes.NAME)
public class XWikiRecycleBinIndexes implements IMandatoryDocumentRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(XWikiRecycleBinIndexes.class);

  public static final String NAME = "celements.mandatory.addIndexToRecycleBin";

  @Requirement
  IQueryExecutionServiceRole queryExecService;

  @Requirement
  private ModelContext modelContext;

  @Requirement
  private ConfigurationSource configSource;

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return Collections.emptyList();
  }

  @Override
  public void checkDocuments() throws XWikiException {
    LOGGER.info("executing XWikiRecycleBinIndexes");
    System.out.println("<<<<<<<<<<<<<<<<<<<< XWikiRecycleBinIndexes checkDocuments IN");
    String prefixAndDb = configSource.getProperty("xwiki.db.prefix", String.class)
        + modelContext.getMainWikiRef().getName();
    System.out.println("<<<<<<<<<<<<<<<<<<<< XWikiRecycleBinIndexes checkDocuments prefixAndDb: "
        + prefixAndDb);
    if (!queryExecService.existsConstraint("prefix_db", "xwikirecyclebin", "dateIDX")) {
      queryExecService.executeWriteSQL(getSQLIndexToRecycleBin());
    }
  }

  String getSQLIndexToRecycleBin() {
    return "alter table xwikirecyclebin add index `dateIDX` (XDD_DATE, XDD_ID);";
  }

}
