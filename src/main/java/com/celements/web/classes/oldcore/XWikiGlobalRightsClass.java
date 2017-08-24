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
@Component(XWikiGlobalRightsClass.CLASS_DEF_HINT)
public class XWikiGlobalRightsClass extends AbstractClassDefinition implements IOldCoreClassDef {

  public static final String CLASS_NAME = "XWikiGlobalRights";
  public static final String CLASS_FN = CLASS_SPACE + "." + CLASS_NAME;
  public static final String CLASS_DEF_HINT = CLASS_FN;

  public static final ClassField<List<String>> FIELD_GROUPS = new ListOfGroupsField.Builder(
      CLASS_FN, FIELD_GROUPS_NAME).prettyName(FIELD_GROUPS_PRETTY_NAME).displayType(
          "select").multiSelect(true).size(5).usesList(true).build();

  public static final ClassField<List<EAccessLevel>> FIELD_LEVELS = new AccessRightLevelsField.Builder(
      CLASS_FN, FIELD_ACCESSLVL_NAME).prettyName(FIELD_ACCESSLVL_PRETTY_NAME).displayType(
          "select").multiSelect(true).size(3).build();

  public static final ClassField<List<XWikiUser>> FIELD_USERS = new ListOfUsersField.Builder(
      CLASS_FN, FIELD_USERS_NAME).prettyName(FIELD_USERS_PRETTY_NAME).displayType(
          "select").multiSelect(true).size(5).usesList(true).build();

  public static final ClassField<Boolean> FIELD_ALLOW = new BooleanField.Builder(CLASS_FN,
      FIELD_ALLOW_NAME).prettyName(FIELD_ALLOW_PRETTY_NAME).displayFormType("select").displayType(
          FIELD_ALLOW_NAME).defaultValue(1).build();

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
