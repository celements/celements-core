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
package com.celements.web.plugin.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

public class ResetProgrammingRightsCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      ResetProgrammingRightsCommand.class);

  public boolean resetCelements2webRigths(XWikiContext context) {
    int result = 0;
    if (context.getUser().startsWith("xwiki:")) {
      String tenantDbName = context.getDatabase();
      try {
        context.setDatabase("celements2web");
        Session sess = getNewHibSession(context);
        Transaction transaction = sess.beginTransaction();
        Query query = sess.createQuery("update com.xpn.xwiki.doc.XWikiDocument"
            + " set contentAuthor = :username");
        query.setParameter("username", context.getUser());
        result = query.executeUpdate();
        mLogger.info("updated [" + result + "] documents. Set to content author ["
            + context.getUser() + "] in database [" + context.getDatabase() + "].");
        transaction.commit();
      } catch (XWikiException exp) {
        mLogger.error("resetCelements2webRights: failed to get a hibernate session: ",
            exp);
      } finally {
        context.setDatabase(tenantDbName);
      }
    }
    return (result > 0);
  }

  private Session getNewHibSession(XWikiContext context) throws XWikiException {
    Session session = context.getWiki().getHibernateStore().getSessionFactory(
        ).openSession();
    context.getWiki().getHibernateStore().setDatabase(session, context);
    return session;
  }

}
