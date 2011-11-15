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

/**
 * This entity is store subsystem data version in database. Used for migrations.
 * Data version is a number specific to the subsystem from which data need migration.
 * Immutable.
 * 
 * @author synventis, fabian
 */
public class SubSystemDBVersion {

  /** db version number. */
  private int version;
  /** subsystem unique name. */
  private String subSystemName;

  /** Default constructor. It is need for Hibernate. */
  public SubSystemDBVersion() {
  }

  public String getSubSystemName() {
    return subSystemName;
  }

  public void setSubSystemName(String subSystemName) {
    this.subSystemName = subSystemName;
  }

  /**
   * @param version
   *          - data version
   */
  public SubSystemDBVersion(String subSystemName, int version) {
    this.version = version;
    this.subSystemName = subSystemName;
  }

  /** @return data version */
  public int getVersion() {
    return version;
  }

  /**
   * @param version
   *          - data version
   */
  protected void setVersion(int version) {
    this.version = version;
  }

  /** {@inheritDoc} */
  public String toString() {
    return getSubSystemName() + "-" + String.valueOf(version);
  }

  /** @return next version */
  public SubSystemDBVersion increment() {
    return new SubSystemDBVersion(getSubSystemName(), getVersion() + 1);
  }
}
