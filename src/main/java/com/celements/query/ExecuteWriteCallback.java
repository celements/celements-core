package com.celements.query;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.XWikiHibernateBaseStore.HibernateCallback;

public class ExecuteWriteCallback implements HibernateCallback<Integer> {

  private final String hql;
  private final Map<String, Object> binds;

  public ExecuteWriteCallback(String hql, Map<String, Object> binds) {
    this.hql = hql;
    this.binds = new HashMap<String, Object>(binds);
  }

  public String getHQL() {
    return hql;
  }

  public Map<String, Object> getBinds() {
    return new HashMap<String, Object>(binds);
  }

  @Override
  public Integer doInHibernate(Session session) throws HibernateException, XWikiException {
    Query query = session.createQuery(hql);
    for (String key : binds.keySet()) {
      query.setParameter(key, binds.get(key));
    }
    return query.executeUpdate();
  }

}
