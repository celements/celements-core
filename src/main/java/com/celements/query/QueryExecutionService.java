package com.celements.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.XWikiHibernateBaseStore.HibernateCallback;
import com.xpn.xwiki.store.XWikiHibernateStore;

@Component
public class QueryExecutionService implements IQueryExecutionServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      QueryExecutionService.class);

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public int executeWriteSQL(String sql) throws XWikiException {
    return executeWriteSQLs(Arrays.asList(sql)).get(0);
  }

  @Override
  public List<Integer> executeWriteSQLs(List<String> sqls) throws XWikiException {
    List<Integer> results = new ArrayList<Integer>();
    Session session = null;
    try {
      session = getNewHibSession();
      for (String sql : sqls) {
        results.add(executeWriteSQL(session, sql));
      }
    } finally {
      if (session != null) {
        session.close();
      }
    }
    return Collections.unmodifiableList(results);
  }

  private Session getNewHibSession() throws XWikiException {
    Session session = getStore().getSessionFactory().openSession();
    getStore().setDatabase(session, getContext());
    return session;
  }

  private int executeWriteSQL(Session session, String sql) {
    int result = -1;
    Transaction transaction = session.beginTransaction();
    try {
      result = session.createSQLQuery(sql).executeUpdate();
    } catch (HibernateException hibExc) {
      LOGGER.debug("error while executing sql '{}'", sql, hibExc);
    } finally {
      if (result > -1) {
        transaction.commit();
      } else {
        transaction.rollback();
      }
    }
    LOGGER.info("executing sql '{}' for db '{}' returned '{}'", sql, getContext(
        ).getDatabase(),  result);
    return result;
  }

  @Override
  public int executeWriteHQL(final String hql, final Map<String, Object> binds
      ) throws XWikiException {
    // TODO set wikiRef/database
    return getStore().executeWrite(getContext(), true, new HibernateCallback<Integer>() {
      @Override
      public Integer doInHibernate(Session session) throws HibernateException {
        org.hibernate.Query query = session.createQuery(hql);
        for (String key : binds.keySet()) {
          query.setParameter(key, binds.get(key));
        }
        return query.executeUpdate();
      }
    });
  }

  private XWikiHibernateStore getStore() {
    return getContext().getWiki().getHibernateStore();
  }

}
