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
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;

import com.celements.model.access.ContextExecutor;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.XWikiHibernateBaseStore.HibernateCallback;
import com.xpn.xwiki.store.XWikiHibernateStore;

@Component
public class QueryExecutionService implements IQueryExecutionServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryExecutionService.class);

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  @Override
  public int executeWriteSQL(String sql) throws XWikiException {
    return executeWriteSQLs(Arrays.asList(sql)).get(0);
  }

  @Override
  public List<Integer> executeWriteSQLs(List<String> sqls) throws XWikiException {
    List<Integer> results = new ArrayList<>();
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
    Session session = getHibStore().getSessionFactory().openSession();
    getHibStore().setDatabase(session, context.getXWikiContext());
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
    LOGGER.info("executing sql '{}' for db '{}' returned '{}'", sql, context.getWikiRef(), result);
    return result;
  }

  @Override
  public int executeWriteHQL(String hql, Map<String, Object> binds) throws XWikiException {
    return executeWriteHQL(hql, binds, null);
  }

  @Override
  public int executeWriteHQL(final String hql, final Map<String, Object> binds,
      WikiReference wikiRef) throws XWikiException {
    return new ContextExecutor<Integer, XWikiException>() {

      @Override
      protected Integer call() throws XWikiException {
        HibernateCallback<Integer> callback = new ExecuteWriteCallback(hql, binds);
        return getHibStore().executeWrite(context.getXWikiContext(), true, callback);
      }
    }.inWiki(Objects.firstNonNull(wikiRef, context.getWikiRef())).execute();
  }

  @Override
  public DocumentReference executeAndGetDocRef(Query query) throws QueryException {
    DocumentReference ret = null;
    List<DocumentReference> list = executeAndGetDocRefs(query);
    if (list.size() > 0) {
      ret = list.get(0);
    }
    return ret;
  }

  @Override
  public List<DocumentReference> executeAndGetDocRefs(Query query) throws QueryException {
    List<DocumentReference> ret = new ArrayList<>();
    WikiReference wikiRef = context.getWikiRef();
    if (!Strings.isNullOrEmpty(query.getWiki())) {
      wikiRef = modelUtils.resolveRef(query.getWiki(), WikiReference.class);
    }
    for (Object fullName : query.execute()) {
      if ((fullName instanceof String) && !Strings.isNullOrEmpty((String) fullName)) {
        ret.add(modelUtils.resolveRef((String) fullName, DocumentReference.class, wikiRef));
      } else {
        LOGGER.debug("executeAndGetDocRefs: received invalid fullName '{}'", fullName);
      }
    }
    LOGGER.info("executeAndGetDocRefs: {} results for query '{}' and wiki '{}'", ret.size(),
        query.getStatement(), wikiRef);
    return ret;
  }

  private XWikiHibernateStore getHibStore() {
    return context.getXWikiContext().getWiki().getHibernateStore();
  }

}
