package com.celements.web.classes.oldcore;

import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.list.AccessRightLevelsField;
import com.celements.model.classes.fields.list.ListOfGroupsField;
import com.celements.model.classes.fields.list.ListOfUsersField;
import com.celements.rights.access.EAccessLevel;
import com.xpn.xwiki.user.api.XWikiUser;

@Singleton
@Component(XWikiRightsClass.CLASS_DEF_HINT)
public class XWikiRightsClass extends AbstractClassDefinition implements IOldCoreClassDef {

  public static final String CLASS_SPACE = "XWiki";
  public static final String CLASS_NAME = "XWikiRights";
  public static final String CLASS_FN = CLASS_SPACE + "." + CLASS_NAME;
  public static final String CLASS_DEF_HINT = CLASS_FN;

  public static final String FIELD_NAME_ALLOW = "allow";
  public static final String FIELD_PRETTY_NAME_ALLOW = "Allow/Deny";
  public static final String FIELD_NAME_ACCESSLVL = "levels";
  public static final String FIELD_PRETTY_NAME_ACCESSLVL = "Levels";
  public static final String FIELD_NAME_USERS = "users";
  public static final String FIELD_PRETTY_NAME_USERS = "Users";
  public static final String FIELD_NAME_GROUPS = "groups";
  public static final String FIELD_PRETTY_NAME_GROUPS = "Groups";

  public static final ClassField<Boolean> FIELD_ALLOW = new BooleanField.Builder(CLASS_FN,
      FIELD_NAME_ALLOW).prettyName(FIELD_PRETTY_NAME_ALLOW).displayFormType("select").displayType(
          FIELD_NAME_ALLOW).defaultValue(1).build();
  public static final ClassField<List<String>> FIELD_GROUPS = new ListOfGroupsField.Builder(
      CLASS_FN, FIELD_NAME_GROUPS).prettyName(FIELD_PRETTY_NAME_GROUPS).displayType(
          "select").multiSelect(true).size(5).usesList(true).build();
  public static final ClassField<List<EAccessLevel>> FIELD_LEVELS = new AccessRightLevelsField.Builder(
      CLASS_FN, FIELD_NAME_ACCESSLVL).prettyName(FIELD_PRETTY_NAME_ACCESSLVL).displayType(
          "select").multiSelect(true).size(3).build();
  public static final ClassField<List<XWikiUser>> FIELD_USERS = new ListOfUsersField.Builder(
      CLASS_FN, FIELD_NAME_USERS).prettyName(FIELD_PRETTY_NAME_USERS).displayType(
          "select").multiSelect(true).size(5).usesList(true).build();

  @Override
  public String getName() {
    return CLASS_FN;
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

  @Override
  protected String getClassSpaceName() {
    return CLASS_SPACE;
  }

  @Override
  protected String getClassDocName() {
    return CLASS_NAME;
  }

}
