package com.celements.web.classes.oldcore;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.model.classes.ClassDefinition;

/**
 * Do NOT add this to a collection. Components for this role are solely for the purpose of having a
 * class and constants where used in Java. Loading this class would interfere with classes handled
 * by XWiki.
 *
 * @author ebeutler
 * @version $Revision: 1.1 $
 */
@ComponentRole
public interface IOldCoreClassDef extends ClassDefinition {

  static final String CLASS_SPACE = "XWiki";

  static final String FIELD_GROUPS_NAME = "groups";
  static final String FIELD_GROUPS_PRETTY_NAME = "Groups";
  static final String FIELD_ACCESSLVL_NAME = "levels";
  static final String FIELD_ACCESSLVL_PRETTY_NAME = "Levels";
  static final String FIELD_USERS_NAME = "users";
  static final String FIELD_USERS_PRETTY_NAME = "Users";
  static final String FIELD_ALLOW_NAME = "allow";
  static final String FIELD_ALLOW_PRETTY_NAME = "Allow/Deny";

}
