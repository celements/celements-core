/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.migrations;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.xwiki.component.manager.ComponentLookupException;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.XWikiHibernateBaseStore;
import com.xpn.xwiki.store.XWikiHibernateBaseStore.HibernateCallback;
import com.xpn.xwiki.store.migration.AbstractXWikiMigrationManager;
import com.xpn.xwiki.store.migration.XWikiDBVersion;
import com.xpn.xwiki.store.migration.XWikiMigratorInterface;
import com.xpn.xwiki.web.Utils;

public class SubSystemHibernateMigrationManager extends AbstractXWikiMigrationManager {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      SubSystemHibernateMigrationManager.class);

  private String subSystemName;

  private Class<? extends XWikiMigratorInterface> subMigratorInterface;

  public SubSystemHibernateMigrationManager(XWikiContext context, String subSystemName,
      Class<? extends XWikiMigratorInterface> subMigratorInterface
      ) throws XWikiException {
    super(context);
    this.subSystemName = subSystemName;
    this.subMigratorInterface = subMigratorInterface;
  }

  /**
   * @return store system for execute store-specific actions.
   * @param context
   *          - used everywhere
   */
  public XWikiHibernateBaseStore getStore(XWikiContext context) {
    return context.getWiki().getHibernateStore();
  }

  public void updateSchema(XWikiContext context) {
    XWikiHibernateBaseStore store = getStore(context);
    if (store != null) {
        store.updateSchema(context, true);
    }
  }

  /** {@inheritDoc} */
  @Override
  public XWikiDBVersion getDBVersion(XWikiContext context) throws XWikiException {
    return getStore(context).executeRead(context, true,
        new HibernateCallback<XWikiDBVersion>() {
          public XWikiDBVersion doInHibernate(Session session) throws HibernateException {
            SubSystemDBVersion result = (SubSystemDBVersion) session.createCriteria(
                SubSystemDBVersion.class).add(
                Restrictions.eq("subSystemName", getSubSystemName())).uniqueResult();
            if (result == null) {
              return new XWikiDBVersion(0);
            } else {
              return new XWikiDBVersion(result.getVersion());
            }
          }
        });
  }

  /** {@inheritDoc} */
  @Override
  protected void setDBVersion(final XWikiDBVersion version, XWikiContext context
      ) throws XWikiException {
    getStore(context).executeWrite(context, true, new HibernateCallback<Object>() {
      public Object doInHibernate(Session session) throws HibernateException {
        session.createQuery("delete from " + SubSystemDBVersion.class.getName()
            + " where subSystemName='" + getSubSystemName() + "'").executeUpdate();
        session.save(new SubSystemDBVersion(getSubSystemName(), version.getVersion()));
        return null;
      }
    });
  }

  public String getSubSystemName() {
    return subSystemName;
  }

  @Override
  protected List<? extends XWikiMigratorInterface> getAllMigrations(XWikiContext context
      ) throws XWikiException {
    List<? extends XWikiMigratorInterface> result = null;
    try {
      result = Utils.getComponentManager().lookupList(subMigratorInterface);
    } catch (ComponentLookupException exp) {
      mLogger.error("Failed to get Migrator for subsystem [" + getSubSystemName() + "].",
          exp);
    }
    if (result != null) {
      mLogger.debug("lookup for [" + subMigratorInterface + "] returned [" + result.size()
          + "] migrator.");
      return result;
    }
    mLogger.debug("lookup for [" + subMigratorInterface + "] returned empty list.");
    return Collections.emptyList();
  }

}
